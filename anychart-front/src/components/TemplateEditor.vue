<template>
  <div class="template-editor">
    <header class="editor-header">
      <h2>图表编辑器</h2>
      <div class="header-actions">
        <button class="btn-back" @click="$emit('back')">← 返回</button>
        <button class="btn-save" @click="saveConfig" :disabled="saving">
          {{ saving ? '保存中...' : '💾 保存' }}
        </button>
      </div>
    </header>
    
    <div class="editor-body">
      <ComponentPalette @drag-start="onDragStart" />
      <EditorCanvas
        :components="components"
        :selectedId="selectedId"
        :graphqlUrl="graphqlUrl"
        @add-component="addComponent"
        @select-component="selectComponent"
        @delete-component="deleteComponent"
        @update-component="updateComponent"
      />
      <div class="right-panels">
        <PropertiesPanel
          :component="selectedComponent"
          @update="updateSelected"
          @save="saveConfig"
          @preview="togglePreview"
        />
        <BindingPanel
          :component="selectedComponent"
          @update-binding="updateBinding"
          @add-binding="addBinding"
          @remove-binding="removeBinding"
        />
      </div>
    </div>
    
    <div v-if="message" class="editor-message" :class="message.type">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import ComponentPalette from './ComponentPalette.vue';
import EditorCanvas from './EditorCanvas.vue';
import PropertiesPanel from './PropertiesPanel.vue';
import BindingPanel from './BindingPanel.vue';

const props = defineProps({
  editId: { type: String, default: null },
  graphqlUrl: { type: String, default: '/graphql' }
});

const emit = defineEmits(['back', 'saved']);

const components = ref([]);
const selectedId = ref(null);
const saving = ref(false);
const message = ref(null);

const selectedComponent = computed(() => {
  return components.value.find(c => c.id === selectedId.value) || null;
});

let nextId = 1;

function showMessage(text, type = 'info') {
  message.value = { text, type };
  setTimeout(() => message.value = null, 3000);
}

function generateId() {
  return `chart-${Date.now()}-${nextId++}`;
}

function onDragStart(item) {
  console.log('Drag started:', item);
}

function addComponent(item) {
  const id = generateId();
  const defaultTemplates = {
    bar: {
      title: '新柱状图',
      chartType: 'bar',
      optionTemplate: {
        title: { text: '图表标题' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: '{{binding:categories}}' },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: '{{binding:data1}}' }]
      },
      bindings: [
        { name: 'ds1', datasourceId: 'mock:sales', query: 'mock:sales', mappingPath: '$.items[*].date', bindingKey: 'categories', stream: false },
        { name: 'ds2', datasourceId: 'mock:sales', query: 'mock:sales', mappingPath: '$.items[*].value', bindingKey: 'data1', stream: false }
      ]
    },
    line: {
      title: '新折线图',
      chartType: 'line',
      optionTemplate: {
        title: { text: '趋势图' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', boundaryGap: false, data: '{{binding:categories}}' },
        yAxis: { type: 'value' },
        series: [{ type: 'line', data: '{{binding:data1}}', smooth: true }]
      },
      bindings: [
        { name: 'ds1', datasourceId: 'mock:trend', query: 'mock:trend', mappingPath: '$.items[*].date', bindingKey: 'categories', stream: false },
        { name: 'ds2', datasourceId: 'mock:trend', query: 'mock:trend', mappingPath: '$.items[*].value', bindingKey: 'data1', stream: false }
      ]
    },
    pie: {
      title: '新饼图',
      chartType: 'pie',
      optionTemplate: {
        title: { text: '占比分布', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [{ type: 'pie', radius: '50%', data: '{{binding:pieData}}' }]
      },
      bindings: [
        { name: 'ds1', datasourceId: 'mock:distribution', query: 'mock:distribution', mappingPath: '$.items[*]', bindingKey: 'pieData', stream: false }
      ]
    },
    scatter: {
      title: '新散点图',
      chartType: 'scatter',
      optionTemplate: {
        title: { text: '相关性分析' },
        tooltip: {},
        xAxis: { name: 'X', type: 'value' },
        yAxis: { name: 'Y', type: 'value' },
        series: [{ type: 'scatter', symbolSize: 20, data: '{{binding:scatterData}}' }]
      },
      bindings: [
        { name: 'ds1', datasourceId: 'mock:correlation', query: 'mock:correlation', mappingPath: '$.items[*]', bindingKey: 'scatterData', stream: false }
      ]
    },
    area: {
      title: '新面积图',
      chartType: 'area',
      optionTemplate: {
        title: { text: '累计增长' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', boundaryGap: false, data: '{{binding:categories}}' },
        yAxis: { type: 'value' },
        series: [{ type: 'line', areaStyle: {}, data: '{{binding:data1}}' }]
      },
      bindings: [
        { name: 'ds1', datasourceId: 'mock:growth', query: 'mock:growth', mappingPath: '$.items[*].date', bindingKey: 'categories', stream: false },
        { name: 'ds2', datasourceId: 'mock:growth', query: 'mock:growth', mappingPath: '$.items[*].value', bindingKey: 'data1', stream: false }
      ]
    }
  };
  
  const template = defaultTemplates[item.id];
  if (template) {
    components.value.push({
      id,
      ...template,
      x: item.x || 100,
      y: item.y || 100,
      width: 400,
      height: 300,
      preview: false
    });
    selectedId.value = id;
    showMessage(`已添加 ${template.title}`, 'success');
  }
}

function selectComponent(id) {
  selectedId.value = id;
}

function deleteComponent(id) {
  const index = components.value.findIndex(c => c.id === id);
  if (index !== -1) {
    components.value.splice(index, 1);
    if (selectedId.value === id) {
      selectedId.value = null;
    }
    showMessage('组件已删除', 'info');
  }
}

function updateComponent(id, updates) {
  const comp = components.value.find(c => c.id === id);
  if (comp) {
    Object.assign(comp, updates);
  }
}

function updateSelected(updates) {
  if (selectedId.value) {
    updateComponent(selectedId.value, updates);
  }
}

function updateBinding({ index, field, value }) {
  const comp = selectedComponent.value;
  if (comp && comp.bindings[index]) {
    comp.bindings[index] = { ...comp.bindings[index], [field]: value };
  }
}

function addBinding(binding) {
  const comp = selectedComponent.value;
  if (comp) {
    comp.bindings.push(binding);
  }
}

function removeBinding(index) {
  const comp = selectedComponent.value;
  if (comp) {
    comp.bindings.splice(index, 1);
  }
}

function togglePreview() {
  if (selectedId.value) {
    const comp = components.value.find(c => c.id === selectedId.value);
    if (comp) {
      comp.preview = !comp.preview;
    }
  }
}

async function saveConfig() {
  if (components.value.length === 0) {
    showMessage('请先添加至少一个图表组件', 'error');
    return;
  }
  
  saving.value = true;
  try {
    for (const comp of components.value) {
      const query = `
        mutation Save($input: ChartConfigInput!) {
          saveChartConfig(input: $input) {
            id
          }
        }
      `;
      
      const response = await fetch(props.graphqlUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query,
          variables: {
            input: {
              id: comp.id,
              title: comp.title,
              chartType: comp.chartType,
              optionTemplate: comp.optionTemplate,
              bindings: comp.bindings.map(b => ({
                name: b.name,
                datasourceId: b.datasourceId,
                query: b.query,
                mappingPath: b.mappingPath,
                bindingKey: b.bindingKey,
                stream: b.stream
              }))
            }
          }
        })
      });
      
      const result = await response.json();
      if (result.errors) {
        throw new Error(JSON.stringify(result.errors));
      }
    }
    
    showMessage(`已保存 ${components.value.length} 个图表配置`, 'success');
    emit('saved');
  } catch (e) {
    console.error('Save failed:', e);
    showMessage('保存失败: ' + e.message, 'error');
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.template-editor {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f0f0;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
}

.editor-header h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-back,
.btn-save {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back {
  background: #f5f5f5;
  color: #333;
}

.btn-back:hover {
  background: #eee;
}

.btn-save {
  background: #2196f3;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #1976d2;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.editor-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.right-panels {
  display: flex;
  flex-direction: column;
  width: 540px;
  overflow-y: auto;
}

.editor-message {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  animation: slideUp 0.3s ease;
}

.editor-message.info {
  background: #2196f3;
  color: white;
}

.editor-message.success {
  background: #4caf50;
  color: white;
}

.editor-message.error {
  background: #f44336;
  color: white;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translate(-50%, 20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
}
</style>
