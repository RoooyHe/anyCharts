<template>
  <div class="app">
    <header>
      <h1>anyCharts - Demo</h1>
      <p>从后端 GraphQL 获取图表配置并渲染</p>
    </header>

    <section class="chart-selector">
      <h3>选择图表类型</h3>
      <div class="chart-grid">
        <div
            v-for="chart in charts"
            :key="chart.id"
            class="chart-card"
            :class="{ active: chartId === chart.id }"
            @click="selectChart(chart)"
        >
          <div class="chart-icon">{{ getChartIcon(chart.chartType) }}</div>
          <div class="chart-name">{{ chart.title }}</div>
          <div class="chart-type">{{ chart.chartType }}</div>
        </div>
      </div>
    </section>

    <section class="controls">
      <label>
        当前图表:
        <select v-model="chartId">
          <option v-for="c in charts" :key="c.id" :value="c.id">
            {{ c.title }} ({{ c.chartType }})
          </option>
        </select>
      </label>
      <label>
        轮询间隔 (ms):
        <input type="number" v-model.number="pollInterval" min="0" step="500"/>
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
import {onMounted, ref} from 'vue';
import ChartRenderer from './components/ChartRenderer.vue';

const charts = ref([]);
const chartId = ref('sales-bar');
const pollInterval = ref(0);
const graphqlUrl = '/graphql';
const variables = ref({});

const CHART_TYPE_ICONS = {
  bar: '📊',
  line: '📈',
  pie: '🥧',
  scatter: '⚬',
  area: '📉'
};

function getChartIcon(type) {
  return CHART_TYPE_ICONS[type] || '📊';
}

function selectChart(chart) {
  chartId.value = chart.id;
  variables.value = {...variables.value, _t: Date.now()};
}

function refresh() {
  variables.value = {...variables.value, _t: Date.now()};
}

async function fetchCharts() {
  const query = `query { allCharts { id title chartType } }`;
  try {
    const resp = await fetch(graphqlUrl, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({query})
    });
    const json = await resp.json();
    if (json.data?.allCharts) {
      charts.value = json.data.allCharts;
      if (charts.value.length > 0 && !charts.value.find(c => c.id === chartId.value)) {
        chartId.value = charts.value[0].id;
      }
    }
  } catch (e) {
    console.error('获取图表列表失败:', e);
  }
}

onMounted(() => {
  fetchCharts();
});
</script>

<style scoped>
.app {
  max-width: 1200px;
  margin: 24px auto;
  font-family: Arial, Helvetica, sans-serif;
}

header h1 {
  margin: 0 0 8px 0;
}

header p {
  color: #666;
  margin-bottom: 20px;
}

.chart-selector {
  margin-bottom: 20px;
}

.chart-selector h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.chart-card {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px 12px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  background: #fff;
}

.chart-card:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.chart-card.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.chart-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.chart-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.chart-type {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
}

.controls {
  display: flex;
  gap: 16px;
  align-items: center;
  margin: 16px 0;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
}

.controls label {
  display: flex;
  gap: 8px;
  align-items: center;
}

.controls select, .controls input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.controls button {
  padding: 6px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.controls button:hover {
  background: #66b1ff;
}

.chart-area {
  border: 1px solid #eee;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
</style>