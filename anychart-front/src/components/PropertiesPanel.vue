<template>
  <div class="properties-panel">
    <h3>属性面板</h3>
    
    <div v-if="!component" class="no-selection">
      请在画布中选择一个组件
    </div>
    
    <div v-else class="props-form">
      <div class="form-group">
        <label>图表 ID</label>
        <input type="text" :value="component.id" disabled />
      </div>
      
      <div class="form-group">
        <label>图表标题</label>
        <input 
          type="text" 
          :value="component.title" 
          @input="update('title', $event.target.value)"
          placeholder="输入图表标题"
        />
      </div>
      
      <div class="form-group">
        <label>图表类型</label>
        <select :value="component.chartType" @change="update('chartType', $event.target.value)">
          <option value="bar">📊 柱状图</option>
          <option value="line">📈 折线图</option>
          <option value="pie">🥧 饼图</option>
          <option value="scatter">⚬ 散点图</option>
          <option value="area">📉 面积图</option>
        </select>
      </div>
      
      <div class="form-group">
        <label>预览模式</label>
        <label class="checkbox-label">
          <input 
            type="checkbox" 
            :checked="component.preview"
            @change="update('preview', $event.target.checked)"
          />
          <span>显示实时预览</span>
        </label>
      </div>
      
      <div class="form-group">
        <label>尺寸 (宽 x 高)</label>
        <div class="size-inputs">
          <input 
            type="number" 
            :value="component.width || 400"
            @input="update('width', parseInt($event.target.value) || 400)"
            min="200"
          />
          <span>×</span>
          <input 
            type="number" 
            :value="component.height || 300"
            @input="update('height', parseInt($event.target.value) || 300)"
            min="150"
          />
        </div>
      </div>
      
      <div class="form-actions">
        <button class="btn-primary" @click="$emit('save')">💾 保存</button>
        <button class="btn-secondary" @click="$emit('preview')">👁️ 预览</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  component: { type: Object, default: null }
});

const emit = defineEmits(['update', 'save', 'preview']);

function update(field, value) {
  emit('update', { [field]: value });
}
</script>

<style scoped>
.properties-panel {
  width: 260px;
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

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
}

.form-group input:disabled {
  background: #f5f5f5;
  color: #999;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input {
  width: auto !important;
}

.size-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-inputs input {
  flex: 1;
}

.size-inputs span {
  color: #999;
}

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #2196f3;
  color: white;
}

.btn-primary:hover {
  background: #1976d2;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background: #eee;
}
</style>
