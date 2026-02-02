<template>
  <div
    class="editor-canvas"
    @dragover.prevent="onDragOver"
    @drop="onDrop"
    @dragenter.prevent="onDragEnter"
    @dragleave="onDragLeave"
    :class="{ 'drag-over': isDragOver }"
  >
    <div v-if="components.length === 0" class="empty-state">
      <span class="hint-icon">📋</span>
      <p>拖拽组件到这里开始创建图表</p>
    </div>
    
    <div
      v-for="(comp, index) in components"
      :key="comp.id"
      class="canvas-item"
      :class="{ selected: selectedId === comp.id }"
      :style="{ left: comp.x + 'px', top: comp.y + 'px' }"
      @click.stop="selectComponent(comp.id)"
    >
      <div class="item-header">
        <span class="item-icon">{{ getIcon(comp.chartType) }}</span>
        <span class="item-title">{{ comp.title || '未命名图表' }}</span>
        <button class="delete-btn" @click.stop="deleteComponent(comp.id)">×</button>
      </div>
      <div class="item-body">
        <ChartRenderer
          v-if="comp.preview && comp.chartType"
          :chartId="comp.id"
          :graphqlUrl="graphqlUrl"
        />
        <div v-else class="preview-placeholder">
          <span>{{ getTypeName(comp.chartType) }}</span>
        </div>
      </div>
      <div class="resize-handle" @mousedown.stop="startResize($event, comp)"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import ChartRenderer from './ChartRenderer.vue';

const props = defineProps({
  components: { type: Array, default: () => [] },
  selectedId: { type: String, default: null },
  graphqlUrl: { type: String, default: '/graphql' }
});

const emit = defineEmits(['add-component', 'select-component', 'delete-component', 'update-component']);

const isDragOver = ref(false);

const chartTypeNames = {
  bar: '柱状图',
  line: '折线图',
  pie: '饼图',
  scatter: '散点图',
  area: '面积图',
};

const chartTypeIcons = {
  bar: '📊',
  line: '📈',
  pie: '🥧',
  scatter: '⚬',
  area: '📉',
};

function getTypeName(type) {
  return chartTypeNames[type] || '未知类型';
}

function getIcon(type) {
  return chartTypeIcons[type] || '📊';
}

function onDragOver(event) {
  event.dataTransfer.dropEffect = 'copy';
}

function onDragEnter() {
  isDragOver.value = true;
}

function onDragLeave() {
  isDragOver.value = false;
}

function onDrop(event) {
  isDragOver.value = false;
  try {
    const data = JSON.parse(event.dataTransfer.getData('application/json'));
    if (data.type === 'datasource') return; // 数据源拖到绑定面板处理
    emit('add-component', {
      ...data,
      x: event.offsetX - 100,
      y: event.offsetY - 100,
    });
  } catch (e) {
    console.error('Invalid drop data', e);
  }
}

function selectComponent(id) {
  emit('select-component', id);
}

function deleteComponent(id) {
  emit('delete-component', id);
}

function startResize(event, comp) {
  const startX = event.clientX;
  const startY = event.clientY;
  const startWidth = 400;
  const startHeight = 300;

  const onMouseMove = (e) => {
    const dx = e.clientX - startX;
    const dy = e.clientY - startY;
    emit('update-component', comp.id, {
      width: Math.max(200, startWidth + dx),
      height: Math.max(150, startHeight + dy),
    });
  };

  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
  };

  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}
</script>

<style scoped>
.editor-canvas {
  flex: 1;
  background: #fafafa;
  position: relative;
  min-height: 600px;
  overflow: auto;
}

.editor-canvas.drag-over {
  background: #e3f2fd;
  border: 2px dashed #2196f3;
}

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #999;
}

.hint-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}

.canvas-item {
  position: absolute;
  width: 400px;
  min-height: 300px;
  background: white;
  border: 2px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: move;
  display: flex;
  flex-direction: column;
}

.canvas-item.selected {
  border-color: #2196f3;
  box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.2);
}

.item-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-bottom: 1px solid #eee;
  border-radius: 6px 6px 0 0;
}

.item-icon {
  margin-right: 8px;
}

.item-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.delete-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  border-radius: 4px;
}

.delete-btn:hover {
  background: #ff5252;
  color: white;
}

.item-body {
  flex: 1;
  padding: 8px;
  min-height: 250px;
}

.preview-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: #f9f9f9;
  border: 2px dashed #ddd;
  border-radius: 4px;
  color: #999;
}

.resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 16px;
  height: 16px;
  cursor: se-resize;
  background: linear-gradient(135deg, transparent 50%, #2196f3 50%);
  border-radius: 0 0 8px 0;
}
</style>
