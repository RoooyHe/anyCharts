<template>
  <div ref="chartEl" class="chart-container"></div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref, watch} from 'vue';
import * as echarts from 'echarts/core';
import {BarChart} from 'echarts/charts';
import {GridComponent, LegendComponent, TitleComponent, TooltipComponent} from 'echarts/components';
import {CanvasRenderer} from 'echarts/renderers';

echarts.use([BarChart, GridComponent, TooltipComponent, TitleComponent, LegendComponent, CanvasRenderer]);

const props = defineProps({
  chartId: {type: String, required: true},
  variables: {type: Object, default: () => ({})},
  graphqlUrl: {type: String, default: '/graphql'},
  pollInterval: {type: Number, default: 0} // 0 不轮询，示例用轮询替代 subscription
});

const chartEl = ref(null);
let chartInstance = null;
let pollHandle = null;

async function fetchRenderedOption(chartId, variables, graphqlUrl) {
  const q = `
    query Render($id: ID!, $vars: JSON) {
      renderChart(id: $id, variables: $vars) {
        id
        option
      }
    }`;
  const resp = await fetch(graphqlUrl, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({query: q, variables: {id: chartId, vars: variables}})
  });
  const json = await resp.json();
  return json.data?.renderChart?.option || null;
}

onMounted(async () => {
  chartInstance = echarts.init(chartEl.value);
  const load = async () => {
    try {
      const option = await fetchRenderedOption(props.chartId, props.variables, props.graphqlUrl);
      if (option) {
        chartInstance.setOption(option, true);
      }
    } catch (e) {
      console.error('render error', e);
    }
  };
  await load();
  if (props.pollInterval > 0) {
    pollHandle = setInterval(load, props.pollInterval);
  }
});

onBeforeUnmount(() => {
  if (chartInstance) chartInstance.dispose();
  if (pollHandle) clearInterval(pollHandle);
});

// 当 variables 变更时刷新
watch(() => props.variables, async () => {
  if (!chartInstance) return;
  const option = await fetchRenderedOption(props.chartId, props.variables, props.graphqlUrl);
  if (option) chartInstance.setOption(option, true);
}, {deep: true});
</script>

<style>
.chart-container {
  width: 100%;
  height: 400px;
}
</style>