<template>
  <div>
    <div v-if="loading" class="loading">加载中...</div>
    <div ref="chartEl" class="chart-container"></div>
    <div v-if="error" class="error">错误：{{ error }}</div>
  </div>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
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
let pendingOption = null;

const loading = ref(true);
const error = ref(null);

function updateChart(option) {
  if (!chartInstance) return;
  pendingOption = option;
  // 延迟到下一个渲染周期，避免在 Vue 渲染过程中调用 setOption
  nextTick(() => {
    if (pendingOption === option && chartInstance) {
      chartInstance.setOption(option, true);
      pendingOption = null;
    }
  });
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
    
    const renderResult = json.data?.renderChart;
    const chartOption = renderResult?.option?.option || renderResult?.option;
    
    if (chartOption) {
      updateChart(chartOption);
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
  
  // 延迟到下一个宏任务，确保不在 Vue 渲染过程中调用 setOption
  setTimeout(() => {
    loadOnce();
    // 轮询
    if (props.pollInterval > 0) {
      pollHandle = setInterval(loadOnce, props.pollInterval);
    }
  }, 0);
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
  () => { if (chartInstance) nextTick(() => loadOnce()); }
);

watch(
  () => props.variables,
  () => { if (chartInstance) nextTick(() => loadOnce()); },
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