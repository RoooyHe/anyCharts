<template>
  <div class="binding-panel">
    <h3>数据绑定</h3>
    
    <div v-if="!component" class="no-selection">
      请先在画布中选择图表组件
    </div>
    
    <div v-else class="binding-content">
      <div class="binding-header">
        <span class="chart-name">{{ component.title || '未命名图表' }}</span>
        <span class="chart-type">{{ getTypeName(component.chartType) }}</span>
      </div>
      
      <div class="bindings-list">
        <div 
          v-for="(binding, index) in component.bindings" 
          :key="index"
          class="binding-item"
        >
          <div class="binding-header-row">
            <span class="binding-name">{{ binding.bindingKey }}</span>
            <button class="remove-binding" @click="removeBinding(index)">×</button>
          </div>
          
          <div class="binding-fields">
            <div class="field">
              <label>数据源</label>
              <select :value="binding.datasourceId" @change="updateBinding(index, 'datasourceId', $event.target.value)">
                <option v-for="ds in datasources" :key="ds.id" :value="ds.id">
                  {{ ds.name }}
                </option>
              </select>
            </div>
            
            <div class="field">
              <label>Query</label>
              <input 
                type="text" 
                :value="binding.query"
                @input="updateBinding(index, 'query', $event.target.value)"
                placeholder="mock:sales"
              />
            </div>
            
            <div class="field">
              <label>JSONPath</label>
              <input 
                type="text" 
                :value="binding.mappingPath"
                @input="updateBinding(index, 'mappingPath', $event.target.value)"
                placeholder="$.items[*].date"
              />
              <button class="test-btn" @click="testJsonPath(binding)">测试</button>
            </div>
            
            <div v-if="testResult" class="test-result">
              <pre>{{ testResult }}</pre>
            </div>
          </div>
        </div>
      </div>
      
      <button class="add-binding" @click="addBinding">
        + 添加绑定
      </button>
      
      <div class="jsonpath-help">
        <details>
          <summary>JSONPath 语法帮助</summary>
          <ul>
            <li><code>$.items</code> - 访问 items 属性</li>
            <li><code>$.items[*]</code> - 遍历所有 items</li>
            <li><code>$.items[*].date</code> - 提取每个 item 的 date</li>
            <li><code>$..value</code> - 递归搜索所有 value</li>
          </ul>
        </details>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  component: { type: Object, default: null }
});

const emit = defineEmits(['update-binding', 'add-binding', 'remove-binding']);

const testResult = ref(null);

const datasources = [
  { id: 'mock:sales', name: '销售数据' },
  { id: 'mock:trend', name: '趋势数据' },
  { id: 'mock:distribution', name: '分布数据' },
  { id: 'mock:correlation', name: '相关性数据' },
  { id: 'mock:growth', name: '增长数据' },
];

const chartTypeNames = {
  bar: '柱状图',
  line: '折线图',
  pie: '饼图',
  scatter: '散点图',
  area: '面积图',
};

function getTypeName(type) {
  return chartTypeNames[type] || '未知类型';
}

function updateBinding(index, field, value) {
  emit('update-binding', { index, field, value });
}

function addBinding() {
  emit('add-binding', {
    name: `binding-${Date.now()}`,
    datasourceId: 'mock:sales',
    query: 'mock:sales',
    mappingPath: '',
    bindingKey: `data${props.component.bindings.length + 1}`,
    stream: false
  });
}

function removeBinding(index) {
  emit('remove-binding', index);
}

async function testJsonPath(binding) {
  try {
    const resp = await fetch('/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: `query Render($id: ID!) { renderChart(id: $id) { id } }`,
        variables: { id: props.component.id }
      })
    });
    // 实际应该调用数据源的测试接口，这里只做演示
    testResult.value = '测试需要后端支持数据源测试接口';
    setTimeout(() => testResult.value = null, 3000);
  } catch (e) {
    testResult.value = 'Error: ' + e.message;
  }
}
</script>

<style scoped>
.binding-panel {
  width: 280px;
  background: white;
  border-left: 1px solid #e0e0e0;
  padding: 12px;
  overflow-y: auto;
}

h3 {
  font-size: 14px;
  margin: 0 0 16px;
  color: #666;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.no-selection {
  color: #999;
  text-align: center;
  padding: 24px;
  font-size: 13px;
}

.binding-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
}

.chart-name {
  font-weight: 500;
  font-size: 13px;
}

.chart-type {
  font-size: 12px;
  color: #666;
}

.bindings-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.binding-item {
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 12px;
}

.binding-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.binding-name {
  font-weight: 500;
  font-size: 13px;
  color: #2196f3;
}

.remove-binding {
  width: 20px;
  height: 20px;
  border: none;
  background: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
}

.remove-binding:hover {
  color: #f44336;
}

.binding-fields .field {
  margin-bottom: 8px;
}

.binding-fields label {
  display: block;
  font-size: 11px;
  color: #999;
  margin-bottom: 2px;
}

.binding-fields input,
.binding-fields select {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
}

.binding-fields input:focus,
.binding-fields select:focus {
  outline: none;
  border-color: #2196f3;
}

.test-btn {
  margin-top: 4px;
  padding: 4px 8px;
  font-size: 11px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.test-btn:hover {
  background: #f5f5f5;
}

.test-result {
  margin-top: 8px;
  padding: 8px;
  background: #e8f5e9;
  border-radius: 4px;
  font-size: 11px;
}

.test-result pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.add-binding {
  width: 100%;
  padding: 10px;
  border: 2px dashed #ddd;
  background: none;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  margin-top: 12px;
}

.add-binding:hover {
  border-color: #2196f3;
  color: #2196f3;
}

.jsonpath-help {
  margin-top: 16px;
  font-size: 12px;
}

.jsonpath-help summary {
  cursor: pointer;
  color: #666;
}

.jsonpath-help ul {
  margin: 8px 0 0;
  padding-left: 20px;
}

.jsonpath-help li {
  margin-bottom: 4px;
  color: #666;
}

.jsonpath-help code {
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 11px;
}
</style>
