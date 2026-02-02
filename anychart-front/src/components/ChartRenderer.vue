<template>
  <div>
    <div v-if="loading" class="loading">加载中...</div>
    <div ref="chartEl" class="chart-container"></div>
    <div v-if="error" class="error">错误：{{ error }}</div>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref, watch} from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  chartId: {type: String, required: true},
  variables: {type: Object, default: () => ({})},
  graphqlUrl: {type: String, default: '/graphql'},
  pollInterval: {type: Number, default: 0}
});

const chartEl = ref(null);
let chartInstance = null;
let pollHandle = null;

const loading = ref(true);
const error = ref(null);

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
  if (!resp.ok) {
    throw new Error('Network error: ' + resp.status);
  }
  const json = await resp.json();
  if (json.errors) {
    throw new Error(JSON.stringify(json.errors));
  }
  return json.data?.renderChart?.option || null;
}

async function loadOnce() {
  loading.value = true;
  error.value = null;
  try {
    const resp = await fetch(props.graphqlUrl, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        query: `query Render($id: ID!, $vars: JSON) {
          renderChart(id: $id, variables: $vars) { id option }
        }`,
        variables: {id: props.chartId, vars: props.variables}
      })
    });
    if (!resp.ok) throw new Error('Network error: ' + resp.status);
    const json = await resp.json();
    if (json.errors) throw new Error(JSON.stringify(json.errors));
    
    // API 返回的是 { id, option: { id, option: {...} } }，需要提取内层的 option
    const renderResult = json.data?.renderChart;
    const chartOption = renderResult?.option?.option || renderResult?.option;
    
    if (chartOption && chartInstance) {
      chartInstance.setOption(chartOption, true);
    }
  } catch (e) {
    console.error(e);
    error.value = e.message || String(e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  // 立即初始化 ECharts（DOM 在模板中始终存在）
  chartInstance = echarts.init(chartEl.value);
  window.addEventListener('resize', () => chartInstance && chartInstance.resize());
  // 加载数据
  loadOnce();
  // 轮询
  if (props.pollInterval > 0) {
    pollHandle = setInterval(loadOnce, props.pollInterval);
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => chartInstance && chartInstance.resize());
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  if (pollHandle) {
    clearInterval(pollHandle);
    pollHandle = null;
  }
});

// 刷新
watch(
  () => [props.chartId, props.graphqlUrl],
  () => chartInstance && loadOnce()
);

watch(
  () => props.variables,
  () => chartInstance && loadOnce(),
  {deep: true}
);
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 480px;
}

.loading {
  padding: 12px;
  color: #666;
}

.error {
  margin-top: 8px;
  color: #c00;
}
</style>