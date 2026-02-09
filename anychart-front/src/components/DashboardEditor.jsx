import React, {useEffect, useRef, useState} from 'react';
import ChartRenderer from './ChartRenderer';

// 图表类型配置
const CHART_TYPES = [
    {id: 'bar', name: '柱状图', icon: '📊'},
    {id: 'line', name: '折线图', icon: '📈'},
    {id: 'pie', name: '饼图', icon: '🥧'},
    {id: 'scatter', name: '散点图', icon: '⚬'},
    {id: 'area', name: '面积图', icon: '📉'}
];

// 默认图表配置模板
const DEFAULT_TEMPLATES = {
    bar: {
        title: {text: '柱状图'},
        tooltip: {trigger: 'axis'},
        xAxis: {type: 'category', data: '{{binding:categories}}'},
        yAxis: {type: 'value'},
        series: [{type: 'bar', data: '{{binding:data1}}'}]
    },
    line: {
        title: {text: '折线图'},
        tooltip: {trigger: 'axis'},
        xAxis: {type: 'category', data: '{{binding:categories}}'},
        yAxis: {type: 'value'},
        series: [{type: 'line', data: '{{binding:data1}}', smooth: true}]
    },
    pie: {
        title: {text: '饼图', left: 'center'},
        tooltip: {trigger: 'item'},
        series: [{type: 'pie', radius: '50%', data: '{{binding:pieData}}'}]
    },
    scatter: {
        title: {text: '散点图'},
        xAxis: {type: 'value'},
        yAxis: {type: 'value'},
        series: [{type: 'scatter', data: '{{binding:scatterData}}'}]
    },
    area: {
        title: {text: '面积图'},
        tooltip: {trigger: 'axis'},
        xAxis: {type: 'category', boundaryGap: false, data: '{{binding:categories}}'},
        yAxis: {type: 'value'},
        series: [{type: 'line', areaStyle: {}, data: '{{binding:data1}}'}]
    }
};

function DashboardEditor({dashboard, onBack, onSave}) {
    const [components, setComponents] = useState([]);
    const [dashboardName, setDashboardName] = useState('');

    // 加载已有大屏数据
    useEffect(() => {
        if (dashboard) {
            setDashboardName(dashboard.name || '');
            setComponents(dashboard.components || []);
        }
    }, [dashboard]);
    const [selectedId, setSelectedId] = useState(null);
    const [draggedType, setDraggedType] = useState(null);
    const [isDragging, setIsDragging] = useState(false);
    const canvasRef = useRef(null);
    const [canvasSize, setCanvasSize] = useState({width: 1920, height: 1080});

    const selectedComponent = components.find(c => c.id === selectedId);

    // 开始拖拽组件类型
    function handleDragStart(type) {
        setDraggedType(type);
        setIsDragging(true);
    }

    // 在画布上放置组件
    function handleCanvasDrop(e) {
        e.preventDefault();
        if (!draggedType) return;

        const rect = canvasRef.current.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        const newComponent = {
            id: `chart-${Date.now()}`,
            type: draggedType.id,
            name: draggedType.name,
            x: Math.max(0, x - 200),
            y: Math.max(0, y - 150),
            width: 400,
            height: 300,
            chartId: null, // 未绑定到后端图表
            optionTemplate: DEFAULT_TEMPLATES[draggedType.id],
            bindings: []
        };

        setComponents([...components, newComponent]);
        setSelectedId(newComponent.id);
        setDraggedType(null);
        setIsDragging(false);
    }

    function handleCanvasDragOver(e) {
        e.preventDefault();
    }

    // 更新组件属性
    function updateComponent(id, updates) {
        setComponents(components.map(c => c.id === id ? {...c, ...updates} : c));
    }

    // 删除组件
    function deleteComponent(id) {
        setComponents(components.filter(c => c.id !== id));
        if (selectedId === id) setSelectedId(null);
    }

    // 保存大屏配置
    async function saveDashboard() {
        try {
            const id = dashboard?.id || `dashboard-${Date.now()}`;
            const name = dashboardName || prompt('请输入大屏名称:', '我的大屏');

            if (!name) return;

            console.log('=== 准备保存大屏 ===');
            console.log('components:', components);

            const query = `
        mutation SaveDashboard($input: DashboardInput!) {
          saveDashboard(input: $input) {
            id
            name
          }
        }
      `;

            const variables = {
                input: {
                    id: id,
                    name: name,
                    width: canvasSize.width,
                    height: canvasSize.height,
                    components: components.map(c => ({
                        id: c.id,
                        type: c.type,
                        x: c.x,
                        y: c.y,
                        width: c.width,
                        height: c.height,
                        chartId: c.chartId || null,
                        title: c.name
                    }))
                }
            };

            console.log('GraphQL variables:', JSON.stringify(variables, null, 2));

            const response = await fetch('/graphql', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({query, variables})
            });

            const result = await response.json();
            console.log('GraphQL response:', result);

            if (result.errors) {
                throw new Error(JSON.stringify(result.errors));
            }

            alert('大屏保存成功！');
            if (onSave) onSave(result.data.saveDashboard);
        } catch (e) {
            console.error('保存大屏失败:', e);
            alert('保存失败: ' + e.message);
        }
    }

    return (
        <div className="dashboard-editor">
            {/* 顶部工具栏 */}
            <header className="editor-toolbar">
                <div className="toolbar-left">
                    <h2>📐 大屏编辑器</h2>
                    <input
                        type="text"
                        className="dashboard-name-input"
                        placeholder="输入大屏名称..."
                        value={dashboardName}
                        onChange={(e) => setDashboardName(e.target.value)}
                    />
                </div>
                <div className="toolbar-actions">
                    <button onClick={onBack}>← 返回</button>
                    <button onClick={saveDashboard} className="btn-primary">💾 保存</button>
                </div>
            </header>

            <div className="editor-layout">
                {/* 左侧：组件面板 */}
                <aside className="component-palette">
                    <h3>组件库</h3>
                    <div className="palette-grid">
                        {CHART_TYPES.map(type => (
                            <div
                                key={type.id}
                                className="palette-item"
                                draggable
                                onDragStart={() => handleDragStart(type)}
                                onDragEnd={() => setIsDragging(false)}
                            >
                                <div className="palette-icon">{type.icon}</div>
                                <div className="palette-name">{type.name}</div>
                            </div>
                        ))}
                    </div>
                </aside>

                {/* 中间：画布 */}
                <main className="canvas-container">
                    <div className="canvas-toolbar">
                        <span>画布尺寸: {canvasSize.width} × {canvasSize.height}</span>
                    </div>
                    <div
                        ref={canvasRef}
                        className={`canvas ${isDragging ? 'dragging' : ''}`}
                        style={{width: canvasSize.width, height: canvasSize.height}}
                        onDrop={handleCanvasDrop}
                        onDragOver={handleCanvasDragOver}
                    >
                        {components.map(comp => (
                            <DraggableComponent
                                key={comp.id}
                                component={comp}
                                isSelected={selectedId === comp.id}
                                onSelect={() => setSelectedId(comp.id)}
                                onUpdate={(updates) => updateComponent(comp.id, updates)}
                                onDelete={() => deleteComponent(comp.id)}
                            />
                        ))}
                        {components.length === 0 && (
                            <div className="canvas-empty">
                                拖拽左侧组件到此处开始设计
                            </div>
                        )}
                    </div>
                </main>

                {/* 右侧：属性面板 */}
                <aside className="properties-panel">
                    {selectedComponent ? (
                        <ComponentProperties
                            component={selectedComponent}
                            onUpdate={(updates) => updateComponent(selectedComponent.id, updates)}
                        />
                    ) : (
                        <div className="panel-empty">
                            <p>选择一个组件以编辑属性</p>
                        </div>
                    )}
                </aside>
            </div>
        </div>
    );
}

// 可拖拽的组件
function DraggableComponent({component, isSelected, onSelect, onUpdate, onDelete}) {
    const [isDragging, setIsDragging] = useState(false);
    const [isResizing, setIsResizing] = useState(false);
    const [dragStart, setDragStart] = useState({x: 0, y: 0});
    const componentRef = useRef(null);

    function handleMouseDown(e) {
        if (e.target.classList.contains('resize-handle')) return;
        e.stopPropagation();
        onSelect();
        setIsDragging(true);
        setDragStart({
            x: e.clientX - component.x,
            y: e.clientY - component.y
        });
    }

    function handleMouseMove(e) {
        if (isDragging) {
            onUpdate({
                x: Math.max(0, e.clientX - dragStart.x),
                y: Math.max(0, e.clientY - dragStart.y)
            });
        }
    }

    function handleMouseUp() {
        setIsDragging(false);
        setIsResizing(false);
    }

    useEffect(() => {
        if (isDragging || isResizing) {
            document.addEventListener('mousemove', handleMouseMove);
            document.addEventListener('mouseup', handleMouseUp);
            return () => {
                document.removeEventListener('mousemove', handleMouseMove);
                document.removeEventListener('mouseup', handleMouseUp);
            };
        }
    }, [isDragging, isResizing, dragStart]);

    // 调整大小
    function handleResizeStart(e, direction) {
        e.stopPropagation();
        setIsResizing(direction);
        setDragStart({x: e.clientX, y: e.clientY});
    }

    function handleResizeMove(e) {
        if (!isResizing) return;
        const dx = e.clientX - dragStart.x;
        const dy = e.clientY - dragStart.y;

        if (isResizing === 'se') {
            onUpdate({
                width: Math.max(200, component.width + dx),
                height: Math.max(150, component.height + dy)
            });
            setDragStart({x: e.clientX, y: e.clientY});
        }
    }

    useEffect(() => {
        if (isResizing) {
            document.addEventListener('mousemove', handleResizeMove);
            return () => document.removeEventListener('mousemove', handleResizeMove);
        }
    }, [isResizing, dragStart]);

    return (
        <div
            ref={componentRef}
            className={`canvas-component ${isSelected ? 'selected' : ''}`}
            style={{
                left: component.x,
                top: component.y,
                width: component.width,
                height: component.height
            }}
            onMouseDown={handleMouseDown}
        >
            <div className="component-header">
                <span>{component.name}</span>
                <button className="btn-delete" onClick={(e) => {
                    e.stopPropagation();
                    onDelete();
                }}>
                    ×
                </button>
            </div>
            <div className="component-content">
                {component.chartId ? (
                    <ChartRenderer
                        chartId={component.chartId}
                        variables={{}}
                        graphqlUrl="/graphql"
                        pollInterval={0}
                    />
                ) : (
                    <div className="component-placeholder">
                        <p>未绑定数据源</p>
                        <p className="hint">在右侧属性面板配置</p>
                    </div>
                )}
            </div>
            {isSelected && (
                <div
                    className="resize-handle resize-se"
                    onMouseDown={(e) => handleResizeStart(e, 'se')}
                />
            )}
        </div>
    );
}

// 组件属性面板
function ComponentProperties({component, onUpdate}) {
    const [availableCharts, setAvailableCharts] = useState([]);
    const [showBindingPanel, setShowBindingPanel] = useState(false);
    const [previewData, setPreviewData] = useState(null);

    useEffect(() => {
        fetchCharts();
    }, []);

    async function fetchCharts() {
        try {
            const resp = await fetch('/graphql', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    query: `query { allCharts { id title chartType } }`
                })
            });
            const json = await resp.json();
            if (json.data?.allCharts) {
                setAvailableCharts(json.data.allCharts);
            }
        } catch (e) {
            console.error('获取图表列表失败:', e);
        }
    }

    // 预览选中的图表数据
    async function previewChart(chartId) {
        if (!chartId) {
            setPreviewData(null);
            return;
        }

        try {
            const resp = await fetch('/graphql', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    query: `query { chartConfig(id: "${chartId}") { id title chartType optionTemplate bindings { name datasourceId query mappingPath bindingKey } } }`
                })
            });
            const json = await resp.json();
            if (json.data?.chartConfig) {
                setPreviewData(json.data.chartConfig);
            }
        } catch (e) {
            console.error('预览失败:', e);
        }
    }

    const matchingCharts = availableCharts.filter(c => c.chartType === component.type);

    return (
        <div className="properties-content">
            <h3>📝 组件属性</h3>

            {/* 基本信息 */}
            <div className="property-section">
                <h4>基本信息</h4>
                <div className="property-group">
                    <label>组件名称</label>
                    <input
                        type="text"
                        value={component.name}
                        onChange={e => onUpdate({name: e.target.value})}
                        placeholder="输入组件名称"
                    />
                </div>

                <div className="property-group">
                    <label>组件类型</label>
                    <div className="type-badge">{component.type}</div>
                </div>
            </div>

            {/* 位置和尺寸 */}
            <div className="property-section">
                <h4>位置和尺寸</h4>
                <div className="property-grid">
                    <div className="property-group">
                        <label>X 坐标</label>
                        <input
                            type="number"
                            value={component.x}
                            onChange={e => onUpdate({x: Number(e.target.value)})}
                        />
                    </div>
                    <div className="property-group">
                        <label>Y 坐标</label>
                        <input
                            type="number"
                            value={component.y}
                            onChange={e => onUpdate({y: Number(e.target.value)})}
                        />
                    </div>
                    <div className="property-group">
                        <label>宽度</label>
                        <input
                            type="number"
                            value={component.width}
                            onChange={e => onUpdate({width: Number(e.target.value)})}
                        />
                    </div>
                    <div className="property-group">
                        <label>高度</label>
                        <input
                            type="number"
                            value={component.height}
                            onChange={e => onUpdate({height: Number(e.target.value)})}
                        />
                    </div>
                </div>
            </div>

            {/* 数据绑定 */}
            <div className="property-section">
                <h4>🔗 数据绑定</h4>

                {matchingCharts.length === 0 ? (
                    <div className="empty-state">
                        <p>暂无匹配的 {component.type} 类型图表</p>
                        <p className="hint">请先在图表编辑器中创建</p>
                    </div>
                ) : (
                    <>
                        <div className="chart-list">
                            {matchingCharts.map(chart => (
                                <div
                                    key={chart.id}
                                    className={`chart-item ${component.chartId === chart.id ? 'active' : ''}`}
                                    onClick={() => {
                                        onUpdate({chartId: chart.id});
                                        previewChart(chart.id);
                                    }}
                                >
                                    <div className="chart-item-icon">📊</div>
                                    <div className="chart-item-info">
                                        <div className="chart-item-title">{chart.title}</div>
                                        <div className="chart-item-type">{chart.chartType}</div>
                                    </div>
                                    {component.chartId === chart.id && (
                                        <div className="chart-item-check">✓</div>
                                    )}
                                </div>
                            ))}
                        </div>

                        {component.chartId && (
                            <button
                                className="btn-secondary"
                                onClick={() => setShowBindingPanel(!showBindingPanel)}
                            >
                                {showBindingPanel ? '隐藏' : '查看'} 数据绑定详情
                            </button>
                        )}
                    </>
                )}

                {/* 数据绑定详情面板 */}
                {showBindingPanel && previewData && (
                    <div className="binding-details">
                        <h5>数据源配置</h5>
                        {previewData.bindings.map((binding, idx) => (
                            <div key={idx} className="binding-item">
                                <div className="binding-header">
                                    <span className="binding-key">{binding.bindingKey}</span>
                                    <span className="binding-badge">{binding.datasourceId}</span>
                                </div>
                                <div className="binding-info">
                                    <div className="binding-row">
                                        <span className="label">查询:</span>
                                        <code>{binding.query}</code>
                                    </div>
                                    <div className="binding-row">
                                        <span className="label">路径:</span>
                                        <code>{binding.mappingPath}</code>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* 快捷操作 */}
            <div className="property-section">
                <h4>快捷操作</h4>
                <div className="action-buttons">
                    <button
                        className="btn-action"
                        onClick={() => onUpdate({
                            x: Math.round(component.x / 20) * 20,
                            y: Math.round(component.y / 20) * 20
                        })}
                    >
                        📐 对齐网格
                    </button>
                    <button
                        className="btn-action"
                        onClick={() => onUpdate({
                            width: 400,
                            height: 300
                        })}
                    >
                        📏 重置尺寸
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DashboardEditor;
