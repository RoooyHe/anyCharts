<template>
  <div class="app">
    <header>
      <h1>anyCharts - Demo</h1>
      <p>演示：从后端 GraphQL 获取已渲染的 ECharts option 并在前端渲染</p>
    </header>

    <section class="controls">
      <label>
        Chart ID:
        <input v-model="chartId" />
      </label>
      <label>
        Poll interval (ms, 0 = off):
        <input type="number" v-model.number="pollInterval" />
      </label>
      <button @click="refresh">刷新</button>
    </section>

    <section class="chart-area">
      <ChartRenderer
          :chartId="chartId"
          :variables="variables"
          :graphqlUrl="graphqlUrl"
          :pollInterval="pollInterval"
      />
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import ChartRenderer from './components/ChartRenderer.vue';

const chartId = ref('sales-2026');
const pollInterval = ref(0); // 0 = 不轮询（也可设 2000 等）
const graphqlUrl = '/graphql'; // Vite dev proxy -> 后端
const variables = ref({}); // 可扩展为时间范围等

function refresh() {
  // 触发 ChartRenderer 内部 watch 自动刷新
  variables.value = { ...variables.value, _t: Date.now() };
}
</script>

<style scoped>
.app {
  max-width: 1000px;
  margin: 24px auto;
  font-family: Arial, Helvetica, sans-serif;
}
header h1 { margin: 0 0 8px 0; }
.controls { display:flex; gap:12px; align-items:center; margin:12px 0; }
.controls label { display:flex; gap:6px; align-items:center; }
.chart-area { border: 1px solid #eee; padding:12px; background:#fafafa; }
</style>