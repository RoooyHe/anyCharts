import React, { useState, useEffect, useRef } from 'react';
import * as echarts from 'echarts';
import ChartRenderer from './ChartRenderer';

// 数据源适配器列表
const DATA_SOURCES = [
  { id: 'mock-adapter', name: 'Mock', icon: '🎲', color: '#4caf50' },
  { id: 'rest-adapter', name: 'REST', icon: '🌐', color: '#2196f3' },
  { id: 'database-adapter', name: '数据库', icon: '💾', color: '#ff9800' }
];

// Mock 数据查询列表
const MOCK_QUERIES = [
  { id: 'mock:sales', name: '销售数据' },
  { id: 'mock:trend', name: '趋势数据' },
  { id: 'mock:distribution', name: '分布数据' },
  { id: 'mock:correlation', name: '相关性' },
  { id: 'mock:growth', name: '增长数据' }
];

// JsonPath 常用模板
const JSONPATH_TEMPLATES = [
  '$.items[*].date',
  '$.items[*].value',
  '$.items[*]',
  '$.data',
  '$[*]'
];

// 图表类型配置
const CHART_TYPES = [
  { 
    id: 'bar', 
    name: '柱状图', 
    icon: '📊',
    defaultConfig: {
      title: '柱状图',
      xAxisName: '类别',
      yAxisName: '数值',
      showLegend: true,
      showTooltip: true
    }
  },
  { 
    id: 'line', 
    name: '折线图', 
    icon: '📈',
    defaultConfig: {
      title: '折线图',
      xAxisName: '时间',
      yAxisName: '数值',
      showLegend: true,
      showTooltip: true,
      smooth: false
    }
  },
  { 
    id: 'pie', 
    name: '饼图', 
    icon: '🥧',
    defaultConfig: {
      title: '饼图',
      showLegend: true,
      showTooltip: true,
      showLabel: true
    }
  },
  { 
    id: 'scatter', 
    name: '散点图', 
    icon: '⚬',
    defaultConfig: {
      title: '散点图',
      xAxisName: 'X轴',
      yAxisName: 'Y轴',
      showLegend: true,
      showTooltip: true
    }
  },
  { 
    id: 'area', 
    name: '面积图', 
    icon: '📉',
    defaultConfig: {
      title: '面积图',
      xAxisName: '时间',
      yAxisName: '数值',
      showLegend: true,
      showTooltip: true
    }
  }
];

// 根据图表类型和配置生成 ECharts option
function generateChartOption(chartType, config, bindings) {
  const baseOption = {
    title: { text: config.title || '图表标题' },
    tooltip: config.showTooltip ? { trigger: chartType === 'pie' ? 'item' : 'axis' } : undefined,
    legend: config.showLegend ? {} : undefined
  };

  switch (chartType) {
    case 'bar':
      return {
        ...baseOption,
        xAxis: { 
          type: 'category', 
          name: config.xAxisName,
          data: '{{binding:categories}}' 
        },
        yAxis: { 
          type: 'value',
          name: config.yAxisName
        },
        series: [{ 
          type: 'bar', 
          data: '{{binding:data1}}',
          name: '系列1'
        }]
      };

    case 'line':
      return {
        ...baseOption,
        xAxis: { 
          type: 'category',
          name: config.xAxisName,
          data: '{{binding:categories}}' 
        },
        yAxis: { 
          type: 'value',
          name: config.yAxisName
        },
        series: [{ 
          type: 'line',
          smooth: config.smooth || false,
          data: '{{binding:data1}}',
          name: '系列1'
        }]
      };

    case 'pie':
      return {
        ...baseOption,
        series: [{
          type: 'pie',
          radius: '60%',
          data: '{{binding:pieData}}',
          label: config.showLabel ? { show: true } : { show: false }
        }]
      };

    case 'scatter':
      return {
        ...baseOption,
        xAxis: { 
          type: 'value',
          name: config.xAxisName
        },
        yAxis: { 
          type: 'value',
          name: config.yAxisName
        },
        series: [{
          type: 'scatter',
          data: '{{binding:scatterData}}'
        }]
      };

    case 'area':
      return {
        ...baseOption,
        xAxis: { 
          type: 'category',
          name: config.xAxisName,
          data: '{{binding:categories}}' 
        },
        yAxis: { 
          type: 'value',
          name: config.yAxisName
        },
        series: [{
          type: 'line',
          areaStyle: {},
          data: '{{binding:data1}}',
          name: '系列1'
        }]
      };

    default:
      return baseOption;
  }
}

function TemplateEditor({ chart, onBack, onSaved }) {
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [previewKey, setPreviewKey] = useState(0);
  const [isInitialized, setIsInitialized] = useState(false);
  
  // 图表配置（用户友好的字段）
  const [chartConfig, setChartConfig] = useState({
    title: '新图表',
    xAxisName: '类别',
    yAxisName: '数值',
    showLegend: true,
    showTooltip: true,
    smooth: false,
    showLabel: true
  });

  // 使用 chart?.id 或生成新ID，确保编辑模式下使用正确的ID
  const initialId = chart?.id || `chart-${Date.now()}`;
  
  const [formData, setFormData] = useState({
    id: initialId,
    title: chart?.title || '新图表',
    chartType: chart?.chartType || 'bar',
    bindings: [
      { name: 'ds1', datasourceId: 'mock-adapter', query: 'mock:sales', mappingPath: '$.items[*].date', bindingKey: 'categories', stream: false },
      { name: 'ds2', datasourceId: 'mock-adapter', query: 'mock:sales', mappingPath: '$.items[*].value', bindingKey: 'data1', stream: false }
    ]
  });

  // 加载已有图表数据
  useEffect(() => {
    if (chart) {
      loadChartData(chart.id);
    } else {
      setIsInitialized(true);
    }
  }, [chart]);

  async function loadChartData(chartId) {
    const query = `query GetChart($id: ID!) { 
      chartConfig(id: $id) { 
        id title chartType optionTemplate 
        bindings { name datasourceId query mappingPath bindingKey stream }
      } 
    }`;
    try {
      const resp = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, variables: { id: chartId } })
      });
      const json = await resp.json();
      if (json.data?.chartConfig) {
        const cfg = json.data.chartConfig;
        setFormData({
          id: cfg.id,
          title: cfg.title,
          chartType: cfg.chartType,
          bindings: cfg.bindings || []
        });
        // 从optionTemplate提取配置
        if (cfg.optionTemplate?.title?.text) {
          setChartConfig(prev => ({ ...prev, title: cfg.optionTemplate.title.text }));
        }
        setIsInitialized(true);
      }
    } catch (e) {
      console.error('加载图表失败:', e);
      setIsInitialized(true);
    }
  }

  // 当图表类型改变时，更新配置和绑定（仅在新建模式且已初始化后）
  useEffect(() => {
    if (!chart && isInitialized) {
      const chartType = CHART_TYPES.find(t => t.id === formData.chartType);
      if (chartType) {
        setChartConfig(prev => ({ ...prev, ...chartType.defaultConfig }));
        
        // 根据图表类型设置默认绑定
        if (formData.chartType === 'pie') {
          setFormData(prev => ({
            ...prev,
            bindings: [
              { name: 'ds1', datasourceId: 'mock-adapter', query: 'mock:sales', mappingPath: '$.items[*]', bindingKey: 'pieData', stream: false }
            ]
          }));
        } else if (formData.chartType === 'scatter') {
          setFormData(prev => ({
            ...prev,
            bindings: [
              { name: 'ds1', datasourceId: 'mock-adapter', query: 'mock:correlation', mappingPath: '$.items[*]', bindingKey: 'scatterData', stream: false }
            ]
          }));
        } else {
          setFormData(prev => ({
            ...prev,
            bindings: [
              { name: 'ds1', datasourceId: 'mock-adapter', query: 'mock:sales', mappingPath: '$.items[*].date', bindingKey: 'categories', stream: false },
              { name: 'ds2', datasourceId: 'mock-adapter', query: 'mock:sales', mappingPath: '$.items[*].value', bindingKey: 'data1', stream: false }
            ]
          }));
        }
      }
    }
  }, [formData.chartType, chart, isInitialized]);

  function showMessage(text, type = 'info') {
    setMessage({ text, type });
    setTimeout(() => setMessage(null), 3000);
  }

  function updateField(field, value) {
    setFormData({ ...formData, [field]: value });
  }

  function updateChartConfig(field, value) {
    setChartConfig({ ...chartConfig, [field]: value });
  }

  function updateBinding(index, field, value) {
    const newBindings = [...formData.bindings];
    newBindings[index] = { ...newBindings[index], [field]: value };
    setFormData({ ...formData, bindings: newBindings });
  }

  function addBinding() {
    setFormData({
      ...formData,
      bindings: [...formData.bindings, {
        name: `ds${formData.bindings.length + 1}`,
        datasourceId: 'mock-adapter',
        query: 'mock:sales',
        mappingPath: '$.items[*]',
        bindingKey: `data${formData.bindings.length + 1}`,
        stream: false
      }]
    });
  }

  function removeBinding(index) {
    const newBindings = formData.bindings.filter((_, i) => i !== index);
    setFormData({ ...formData, bindings: newBindings });
  }

  // 刷新预览
  function refreshPreview() {
    setPreviewKey(prev => prev + 1);
  }

  async function saveConfig() {
    setSaving(true);
    try {
      // 根据用户配置生成 ECharts option
      const optionTemplate = generateChartOption(formData.chartType, chartConfig, formData.bindings);
      
      console.log('=== 准备保存图表 ===');
      console.log('formData:', formData);
      console.log('chartConfig:', chartConfig);
      console.log('optionTemplate:', optionTemplate);
      
      // 处理数据绑定（query 字段已经在 DatabaseConfig 中构建好了）
      const processedBindings = formData.bindings.map(binding => ({
        name: binding.name,
        datasourceId: binding.datasourceId,
        query: binding.query,
        mappingPath: binding.mappingPath,
        bindingKey: binding.bindingKey,
        stream: binding.stream || false
      }));
      
      console.log('processedBindings:', processedBindings);
      
      const query = `
        mutation Save($input: ChartConfigInput!) {
          saveChartConfig(input: $input) {
            id
            title
          }
        }
      `;

      const variables = {
        input: {
          id: formData.id,
          title: chartConfig.title,
          chartType: formData.chartType,
          optionTemplate,
          bindings: processedBindings
        }
      };
      
      console.log('GraphQL variables:', JSON.stringify(variables, null, 2));

      const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, variables })
      });

      const result = await response.json();
      console.log('GraphQL response:', result);
      
      if (result.errors) {
        throw new Error(JSON.stringify(result.errors));
      }

      showMessage('保存成功！', 'success');
      onSaved();
      setTimeout(() => onBack(), 1500);
    } catch (e) {
      console.error('Save failed:', e);
      showMessage('保存失败: ' + e.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="template-editor">
      <header className="editor-header">
        <h2>✏️ 图表编辑器</h2>
        <div className="header-actions">
          <button className="btn-back" onClick={onBack}>← 返回</button>
          <button className="btn-save" onClick={saveConfig} disabled={saving}>
            {saving ? '保存中...' : '💾 保存'}
          </button>
        </div>
      </header>

      <div className="editor-split">
        {/* 左侧：编辑区 */}
        <div className="editor-left">
          <div className="editor-form">
            {/* 基本信息 */}
            <div className="form-section">
              <h3>📝 基本信息</h3>
              <div className="form-field">
                <label>图表 ID</label>
                <input
                  type="text"
                  value={formData.id}
                  onChange={e => updateField('id', e.target.value)}
                  placeholder="唯一标识符"
                />
              </div>
              <div className="form-field">
                <label>图表类型</label>
                <div className="chart-type-grid">
                  {CHART_TYPES.map(type => (
                    <button
                      key={type.id}
                      className={`chart-type-card ${formData.chartType === type.id ? 'active' : ''}`}
                      onClick={() => updateField('chartType', type.id)}
                    >
                      <div className="chart-type-icon">{type.icon}</div>
                      <div className="chart-type-name">{type.name}</div>
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* 图表样式配置 */}
            <div className="form-section">
              <h3>🎨 图表样式</h3>
              <div className="form-field">
                <label>图表标题</label>
                <input
                  type="text"
                  value={chartConfig.title}
                  onChange={e => updateChartConfig('title', e.target.value)}
                  placeholder="输入图表标题"
                />
              </div>

              {/* 柱状图、折线图、面积图、散点图的配置 */}
              {['bar', 'line', 'area', 'scatter'].includes(formData.chartType) && (
                <div className="form-grid-2">
                  <div className="form-field">
                    <label>X轴名称</label>
                    <input
                      type="text"
                      value={chartConfig.xAxisName}
                      onChange={e => updateChartConfig('xAxisName', e.target.value)}
                      placeholder="例如：日期"
                    />
                  </div>
                  <div className="form-field">
                    <label>Y轴名称</label>
                    <input
                      type="text"
                      value={chartConfig.yAxisName}
                      onChange={e => updateChartConfig('yAxisName', e.target.value)}
                      placeholder="例如：销售额"
                    />
                  </div>
                </div>
              )}

              {/* 折线图特有配置 */}
              {formData.chartType === 'line' && (
                <div className="form-field">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={chartConfig.smooth}
                      onChange={e => updateChartConfig('smooth', e.target.checked)}
                    />
                    <span>平滑曲线</span>
                  </label>
                </div>
              )}

              {/* 饼图特有配置 */}
              {formData.chartType === 'pie' && (
                <div className="form-field">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={chartConfig.showLabel}
                      onChange={e => updateChartConfig('showLabel', e.target.checked)}
                    />
                    <span>显示标签</span>
                  </label>
                </div>
              )}

              {/* 通用配置 */}
              <div className="form-field">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={chartConfig.showLegend}
                    onChange={e => updateChartConfig('showLegend', e.target.checked)}
                  />
                  <span>显示图例</span>
                </label>
              </div>
              <div className="form-field">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={chartConfig.showTooltip}
                    onChange={e => updateChartConfig('showTooltip', e.target.checked)}
                  />
                  <span>显示提示框</span>
                </label>
              </div>
            </div>

            {/* 数据绑定 */}
            <div className="form-section">
              <div className="section-header">
                <h3>🔗 数据绑定 ({formData.bindings.length})</h3>
                <button className="btn-add-small" onClick={addBinding}>+ 添加</button>
              </div>

              {formData.bindings.map((binding, index) => (
                <div key={index} className="binding-card">
                  <div className="binding-card-header">
                    <span className="binding-num">#{index + 1}</span>
                    <span className="binding-key-tag">{binding.bindingKey}</span>
                    <button className="btn-remove-small" onClick={() => removeBinding(index)}>×</button>
                  </div>

                  <div className="binding-card-body">
                    {/* 数据源 */}
                    <div className="form-field">
                      <label>数据源</label>
                      <div className="datasource-pills">
                        {DATA_SOURCES.map(ds => (
                          <button
                            key={ds.id}
                            className={`pill ${binding.datasourceId === ds.id ? 'active' : ''} ${ds.disabled ? 'disabled' : ''}`}
                            onClick={() => !ds.disabled && updateBinding(index, 'datasourceId', ds.id)}
                            disabled={ds.disabled}
                            style={{ borderColor: binding.datasourceId === ds.id ? ds.color : undefined }}
                          >
                            <span>{ds.icon}</span>
                            <span>{ds.name}</span>
                          </button>
                        ))}
                      </div>
                    </div>

                    {/* Mock 查询 */}
                    {binding.datasourceId === 'mock-adapter' && (
                      <div className="form-field">
                        <label>Mock 数据集</label>
                        <select 
                          value={binding.query} 
                          onChange={e => updateBinding(index, 'query', e.target.value)}
                        >
                          {MOCK_QUERIES.map(q => (
                            <option key={q.id} value={q.id}>{q.name}</option>
                          ))}
                        </select>
                      </div>
                    )}

                    {/* REST API */}
                    {binding.datasourceId === 'rest-adapter' && (
                      <div className="form-field">
                        <label>API 地址</label>
                        <input
                          type="text"
                          value={binding.query}
                          onChange={e => updateBinding(index, 'query', e.target.value)}
                          placeholder="https://api.example.com/data"
                        />
                      </div>
                    )}

                    {/* 数据库配置 */}
                    {binding.datasourceId === 'database-adapter' && (
                      <DatabaseConfig
                        binding={binding}
                        onUpdate={(field, value) => updateBinding(index, field, value)}
                      />
                    )}

                    {/* JsonPath */}
                    <div className="form-field">
                      <label>JsonPath 表达式</label>
                      <input
                        type="text"
                        value={binding.mappingPath}
                        onChange={e => updateBinding(index, 'mappingPath', e.target.value)}
                        placeholder="$.items[*].value"
                      />
                      <div className="jsonpath-chips">
                        {JSONPATH_TEMPLATES.map((tpl, idx) => (
                          <button
                            key={idx}
                            className="chip"
                            onClick={() => updateBinding(index, 'mappingPath', tpl)}
                          >
                            {tpl}
                          </button>
                        ))}
                      </div>
                    </div>

                    {/* 绑定键和名称 */}
                    <div className="form-grid-2">
                      <div className="form-field">
                        <label>绑定键</label>
                        <input
                          type="text"
                          value={binding.bindingKey}
                          onChange={e => updateBinding(index, 'bindingKey', e.target.value)}
                          placeholder="categories"
                        />
                      </div>
                      <div className="form-field">
                        <label>名称</label>
                        <input
                          type="text"
                          value={binding.name}
                          onChange={e => updateBinding(index, 'name', e.target.value)}
                          placeholder="ds1"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              ))}

              {formData.bindings.length === 0 && (
                <div className="empty-state-small">
                  <p>暂无数据绑定</p>
                  <button className="btn-primary" onClick={addBinding}>+ 添加第一个绑定</button>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 右侧：预览区 */}
        <div className="editor-right">
          <div className="preview-panel">
            <div className="preview-header">
              <h3>👁️ 实时预览</h3>
              <button className="btn-refresh" onClick={refreshPreview}>🔄 刷新</button>
            </div>
            <div className="preview-content">
              <PreviewChart 
                key={previewKey}
                formData={formData}
                chartConfig={chartConfig}
              />
            </div>
          </div>
        </div>
      </div>

      {message && (
        <div className={`editor-message ${message.type}`}>
          {message.text}
        </div>
      )}
    </div>
  );
}

// 预览组件
// 预览组件 - 点击按钮时调用后端渲染真实数据
function PreviewChart({ formData, chartConfig }) {
  const [previewId, setPreviewId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [needsRefresh, setNeedsRefresh] = useState(true);

  // 监听配置变化，标记需要刷新
  useEffect(() => {
    setNeedsRefresh(true);
  }, [formData, chartConfig]);

  async function handlePreview() {
    setLoading(true);
    setError(null);
    setNeedsRefresh(false);
    
    try {
      // 使用固定的临时ID，每次覆盖
      const tempId = 'preview-temp';
      
      // 根据用户配置生成 ECharts option
      const optionTemplate = generateChartOption(formData.chartType, chartConfig, formData.bindings);
      
      // 处理数据绑定
      const processedBindings = formData.bindings.map(binding => ({
        name: binding.name,
        datasourceId: binding.datasourceId,
        query: binding.query,
        mappingPath: binding.mappingPath,
        bindingKey: binding.bindingKey,
        stream: binding.stream || false
      }));
      
      const query = `
        mutation Save($input: ChartConfigInput!) {
          saveChartConfig(input: $input) {
            id
          }
        }
      `;

      const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query,
          variables: {
            input: {
              id: tempId,
              title: '预览图表',
              chartType: formData.chartType,
              optionTemplate,
              bindings: processedBindings
            }
          }
        })
      });

      const result = await response.json();
      if (result.errors) {
        throw new Error(result.errors[0].message);
      }

      setPreviewId(tempId);
    } catch (e) {
      console.error('预览失败:', e);
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  if (!previewId) {
    return (
      <div className="preview-empty">
        <p>👁️ 配置图表后点击预览按钮查看效果</p>
        <button 
          className="btn-preview-large" 
          onClick={handlePreview}
          disabled={loading}
        >
          {loading ? '⏳ 生成中...' : '🔍 预览图表'}
        </button>
      </div>
    );
  }

  return (
    <div className="preview-container">
      {needsRefresh && (
        <div className="preview-refresh-hint">
          <span>⚠️ 配置已更改</span>
          <button onClick={handlePreview} disabled={loading}>
            {loading ? '刷新中...' : '🔄 刷新预览'}
          </button>
        </div>
      )}
      {error && (
        <div className="preview-error">
          <p>❌ 预览失败: {error}</p>
          <button onClick={handlePreview}>重试</button>
        </div>
      )}
      <ChartRenderer
        key={previewId + Date.now()}
        chartId={previewId}
        variables={{}}
        graphqlUrl="/graphql"
        pollInterval={0}
      />
    </div>
  );
}

// 数据库配置组件
function DatabaseConfig({ binding, onUpdate }) {
  const [connections, setConnections] = useState([]);
  const [tables, setTables] = useState([]);
  const [columns, setColumns] = useState([]);
  const [previewData, setPreviewData] = useState([]);
  const [loading, setLoading] = useState(false);

  // 加载数据库连接列表
  useEffect(() => {
    fetchConnections();
  }, []);

  // 当选择连接后，加载表列表
  useEffect(() => {
    if (binding.dbConnectionId) {
      fetchTables(binding.dbConnectionId);
    }
  }, [binding.dbConnectionId]);

  // 当选择表后，加载字段列表
  useEffect(() => {
    if (binding.dbConnectionId && binding.dbTable) {
      fetchColumns(binding.dbConnectionId, binding.dbTable);
    }
  }, [binding.dbConnectionId, binding.dbTable]);

  async function fetchConnections() {
    try {
      const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query { databaseConnections { id name active } }`
        })
      });
      const result = await response.json();
      if (result.data) {
        setConnections(result.data.databaseConnections);
      }
    } catch (e) {
      console.error('Failed to fetch connections:', e);
    }
  }

  async function fetchTables(connectionId) {
    setLoading(true);
    try {
      const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query { databaseTables(connectionId: "${connectionId}") }`
        })
      });
      const result = await response.json();
      if (result.data) {
        setTables(result.data.databaseTables);
      }
    } catch (e) {
      console.error('Failed to fetch tables:', e);
    } finally {
      setLoading(false);
    }
  }

  async function fetchColumns(connectionId, tableName) {
    setLoading(true);
    try {
      const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query { 
            databaseColumns(connectionId: "${connectionId}", tableName: "${tableName}") {
              name type nullable
            }
          }`
        })
      });
      const result = await response.json();
      if (result.data) {
        setColumns(result.data.databaseColumns);
      }
    } catch (e) {
      console.error('Failed to fetch columns:', e);
    } finally {
      setLoading(false);
    }
  }

  async function previewTable() {
    if (!binding.dbConnectionId || !binding.dbTable) return;
    
    setLoading(true);
    try {
      const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query { 
            previewTableData(connectionId: "${binding.dbConnectionId}", tableName: "${binding.dbTable}", limit: 5)
          }`
        })
      });
      const result = await response.json();
      if (result.data) {
        setPreviewData(result.data.previewTableData);
      }
    } catch (e) {
      console.error('Failed to preview data:', e);
    } finally {
      setLoading(false);
    }
  }

  // 构建查询字符串
  function buildQuery() {
    if (!binding.dbConnectionId || !binding.dbTable) return '';
    
    if (binding.dbCustomSql) {
      return `${binding.dbConnectionId}:${binding.dbCustomSql}`;
    }
    
    const selectedCols = binding.dbSelectedColumns || [];
    if (selectedCols.length === 0) {
      return `${binding.dbConnectionId}:${binding.dbTable}`;
    }
    
    return `${binding.dbConnectionId}:SELECT ${selectedCols.join(', ')} FROM ${binding.dbTable}`;
  }

  // 更新 query 字段
  useEffect(() => {
    const query = buildQuery();
    if (query) {
      onUpdate('query', query);
    }
  }, [binding.dbConnectionId, binding.dbTable, binding.dbSelectedColumns, binding.dbCustomSql]);

  return (
    <div className="database-config">
      {/* 数据库连接选择 */}
      <div className="form-field">
        <label>数据库连接</label>
        <select
          value={binding.dbConnectionId || ''}
          onChange={e => onUpdate('dbConnectionId', e.target.value)}
        >
          <option value="">请选择数据库连接</option>
          {connections.map(conn => (
            <option key={conn.id} value={conn.id}>
              {conn.name} {!conn.active && '(未激活)'}
            </option>
          ))}
        </select>
      </div>

      {/* 表选择 */}
      {binding.dbConnectionId && (
        <div className="form-field">
          <label>选择表</label>
          <select
            value={binding.dbTable || ''}
            onChange={e => onUpdate('dbTable', e.target.value)}
            disabled={loading}
          >
            <option value="">请选择表</option>
            {tables.map(table => (
              <option key={table} value={table}>{table}</option>
            ))}
          </select>
        </div>
      )}

      {/* 字段选择 */}
      {binding.dbTable && columns.length > 0 && (
        <div className="form-field">
          <label>选择字段（可多选）</label>
          <div className="column-checkboxes">
            {columns.map(col => (
              <label key={col.name} className="checkbox-label">
                <input
                  type="checkbox"
                  checked={(binding.dbSelectedColumns || []).includes(col.name)}
                  onChange={e => {
                    const selected = binding.dbSelectedColumns || [];
                    if (e.target.checked) {
                      onUpdate('dbSelectedColumns', [...selected, col.name]);
                    } else {
                      onUpdate('dbSelectedColumns', selected.filter(c => c !== col.name));
                    }
                  }}
                />
                <span>{col.name} <span className="col-type">({col.type})</span></span>
              </label>
            ))}
          </div>
        </div>
      )}

      {/* 预览按钮 */}
      {binding.dbTable && (
        <div className="form-field">
          <button className="btn-preview" onClick={previewTable} disabled={loading}>
            {loading ? '加载中...' : '📋 预览数据'}
          </button>
        </div>
      )}

      {/* 数据预览 */}
      {previewData.length > 0 && (
        <div className="data-preview">
          <table>
            <thead>
              <tr>
                {Object.keys(previewData[0]).map(key => (
                  <th key={key}>{key}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {previewData.map((row, idx) => (
                <tr key={idx}>
                  {Object.values(row).map((val, i) => (
                    <td key={i}>{val !== null ? String(val) : 'NULL'}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* 高级选项：自定义 SQL */}
      <div className="form-field">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={binding.dbUseCustomSql || false}
            onChange={e => onUpdate('dbUseCustomSql', e.target.checked)}
          />
          <span>使用自定义 SQL</span>
        </label>
      </div>

      {binding.dbUseCustomSql && (
        <div className="form-field">
          <label>自定义 SQL 查询</label>
          <textarea
            value={binding.dbCustomSql || ''}
            onChange={e => onUpdate('dbCustomSql', e.target.value)}
            placeholder="SELECT product_name, revenue FROM product_sales WHERE revenue > 50000"
            rows={4}
            className="sql-editor"
          />
          <span className="field-hint">支持变量: ${'{varName}'}</span>
        </div>
      )}

      {/* 显示最终查询 */}
      {binding.dbConnectionId && binding.dbTable && (
        <div className="query-display">
          <label>最终查询</label>
          <code>{buildQuery()}</code>
        </div>
      )}
    </div>
  );
}

export default TemplateEditor;
