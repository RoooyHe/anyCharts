<template>
  <div class="component-palette">
    <h3>组件</h3>
    <div class="palette-grid">
      <div
        v-for="type in chartTypes"
        :key="type.id"
        class="palette-item"
        draggable="true"
        @dragstart="onDragStart($event, type)"
      >
        <span class="icon">{{ type.icon }}</span>
        <span class="name">{{ type.name }}</span>
      </div>
    </div>
    
    <h3>数据源</h3>
    <div class="datasource-list">
      <div
        v-for="ds in datasources"
        :key="ds.id"
        class="datasource-item"
        draggable="true"
        @dragstart="onDragStart($event, ds)"
      >
        <span class="ds-icon">📊</span>
        <span class="name">{{ ds.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
const emit = defineEmits(['drag-start']);

const chartTypes = [
  { id: 'bar', name: '柱状图', icon: '📊' },
  { id: 'line', name: '折线图', icon: '📈' },
  { id: 'pie', name: '饼图', icon: '🥧' },
  { id: 'scatter', name: '散点图', icon: '⚬' },
  { id: 'area', name: '面积图', icon: '📉' },
];

const datasources = [
  { id: 'mock:sales', name: '销售数据', type: 'datasource' },
  { id: 'mock:trend', name: '趋势数据', type: 'datasource' },
  { id: 'mock:distribution', name: '分布数据', type: 'datasource' },
  { id: 'mock:correlation', name: '相关性数据', type: 'datasource' },
  { id: 'mock:growth', name: '增长数据', type: 'datasource' },
];

function onDragStart(event, item) {
  event.dataTransfer.setData('application/json', JSON.stringify(item));
  event.dataTransfer.effectAllowed = 'copy';
  emit('drag-start', item);
}
</script>

<style scoped>
.component-palette {
  width: 200px;
  background: #f5f5f5;
  padding: 12px;
  border-right: 1px solid #e0e0e0;
}

h3 {
  font-size: 14px;
  margin: 16px 0 8px;
  color: #666;
}

.palette-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.palette-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: grab;
  transition: all 0.2s;
}

.palette-item:hover {
  border-color: #2196f3;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.2);
}

.palette-item:active {
  cursor: grabbing;
}

.icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.name {
  font-size: 12px;
  color: #333;
}

.datasource-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.datasource-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s;
}

.datasource-item:hover {
  border-color: #4caf50;
  background: #f0f7f0;
}

.ds-icon {
  font-size: 16px;
}
</style>
