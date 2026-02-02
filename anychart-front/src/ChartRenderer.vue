<template>
  <div>
    <div v-if="loading" class="loading">加载中...</div>
    <div ref="chartEl" class="chart-container" v-show="!loading"></div>
    <div v-if="error" class="error">错误：{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  chartId: { type: String, required: true },
  variables: { type: Object, default: () => ({}) },
  graphqlUrl: { type: String, default: '/graphql' },
  pollInterval: { type: Number, default: 0 } // ms，0 表示不轮询
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
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query: q, variables: { id: chartId, vars: variables } })
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
    const option = await fetchRenderedOption(props.chartId, props.variables, props.graphqlUrl);
    if (option && chartInstance) {
      // echarts expects an object; backend PoC returns plain JSON object
      chartInstance.setOption(option, true);
    }
  } catch (e) {
    console.error(e);
    error.value = e.message || String(e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  chartInstance = echarts.init(chartEl.value);
  // 初次加载
  loadOnce();
  // 轮询逻辑（用于简单演示），生产请用 GraphQL subscription / websocket
  if (props.pollInterval > 0) {
    pollHandle = setInterval(loadOnce, props.pollInterval);
  }
});

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  if (pollHandle) {
    clearInterval(pollHandle);
    pollHandle = null;
  }
});

// 当 props 改变时刷新
watch(
    () => [props.chartId, props.graphqlUrl],
    () => {
      if (chartInstance) loadOnce();
    }
);

watch(
    () => props.variables,
    () => {
      if (chartInstance) loadOnce();
    },
    { deep: true }
);
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 480px;
}
.loading { padding: 12px; color: #666; }
.error { margin-top: 8px; color: #c00; }
</style>