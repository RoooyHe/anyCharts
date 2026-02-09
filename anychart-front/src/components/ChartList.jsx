import React, {useEffect, useState} from 'react';
import ChartRenderer from './ChartRenderer';

const CHART_TYPE_ICONS = {
    bar: '📊', line: '📈', pie: '🥧', scatter: '⚬', area: '📉'
};

const CHART_TYPE_COLORS = {
    bar: '#4caf50', line: '#2196f3', pie: '#ff9800', scatter: '#9c27b0', area: '#00bcd4'
};

function ChartList({onEdit}) {
    const [charts, setCharts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedChart, setSelectedChart] = useState(null);
    const [showPreview, setShowPreview] = useState(false);

    useEffect(() => {
        fetchCharts();
    }, []);

    async function fetchCharts() {
        setLoading(true);
        const query = `query { allCharts { id title chartType createdAt } }`;
        try {
            console.log('=== 图表管理：正在获取图表列表 ===');
            const resp = await fetch('/graphql', {
                method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({query})
            });
            const json = await resp.json();
            console.log('图表管理：GraphQL响应:', json);

            if (json.errors) {
                console.error('图表管理：GraphQL错误:', json.errors);
            }

            if (json.data?.allCharts) {
                console.log('图表管理：获取到', json.data.allCharts.length, '个图表');
                setCharts(json.data.allCharts);
            } else {
                console.warn('图表管理：没有返回图表数据');
            }
        } catch (e) {
            console.error('获取图表列表失败:', e);
        } finally {
            setLoading(false);
        }
    }

    async function deleteChart(chartId, e) {
        e.stopPropagation();
        if (!confirm('确定要删除这个图表吗？')) return;

        const query = `mutation DeleteChart($id: ID!) { deleteChartConfig(id: $id) }`;
        try {
            const resp = await fetch('/graphql', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({query, variables: {id: chartId}})
            });
            const json = await resp.json();
            if (json.data?.deleteChartConfig) {
                alert('删除成功！');
                fetchCharts();
                if (selectedChart?.id === chartId) {
                    setSelectedChart(null);
                    setShowPreview(false);
                }
            }
        } catch (e) {
            console.error('删除图表失败:', e);
            alert('删除失败: ' + e.message);
        }
    }

    function handlePreview(chart, e) {
        e.stopPropagation();
        setSelectedChart(chart);
        setShowPreview(true);
    }

    function handleEdit(chart, e) {
        if (e) e.stopPropagation();
        onEdit(chart);
    }

    function formatDate(dateStr) {
        if (!dateStr) return '未知';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN', {
            year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
        });
    }

    return (<div className="chart-management">
            <div className="chart-management-header">
                <div className="header-left">
                    <h2>📊 图表管理</h2>
                    <span className="chart-count">{charts.length} 个图表</span>
                </div>
                <button className="create-chart-btn" onClick={() => onEdit(null)}>
                    ➕ 创建新图表
                </button>
            </div>

            {loading && <div className="loading-state">加载中...</div>}

            {!loading && charts.length === 0 && (<div className="empty-chart-state">
                    <div className="empty-icon">📊</div>
                    <p>还没有创建任何图表</p>
                    <button className="create-first-btn" onClick={() => onEdit(null)}>
                        创建第一个图表
                    </button>
                </div>)}

            {!loading && charts.length > 0 && (<div className="chart-grid">
                    {charts.map(chart => (<div
                            key={chart.id}
                            className="chart-card"
                            style={{borderTopColor: CHART_TYPE_COLORS[chart.chartType]}}
                        >
                            <div className="chart-card-header">
                                <div className="chart-card-icon"
                                     style={{background: CHART_TYPE_COLORS[chart.chartType]}}>
                                    {CHART_TYPE_ICONS[chart.chartType] || '📊'}
                                </div>
                                <div className="chart-card-type">{chart.chartType}</div>
                            </div>

                            <div className="chart-card-body">
                                <h3 className="chart-card-title">{chart.title}</h3>
                                <div className="chart-card-meta">
                                    <span className="chart-card-id">ID: {chart.id}</span>
                                    <span className="chart-card-date">{formatDate(chart.createdAt)}</span>
                                </div>
                            </div>

                            <div className="chart-card-actions">
                                <button
                                    className="card-action-btn preview-btn"
                                    onClick={(e) => handlePreview(chart, e)}
                                    title="预览"
                                >
                                    👁️ 预览
                                </button>
                                <button
                                    className="card-action-btn edit-btn"
                                    onClick={(e) => handleEdit(chart, e)}
                                    title="编辑"
                                >
                                    ✏️ 编辑
                                </button>
                                <button
                                    className="card-action-btn delete-btn"
                                    onClick={(e) => deleteChart(chart.id, e)}
                                    title="删除"
                                >
                                    🗑️
                                </button>
                            </div>
                        </div>))}
                </div>)}

            {/* 预览弹窗 */}
            {showPreview && selectedChart && (
                <div className="chart-preview-modal" onClick={() => setShowPreview(false)}>
                    <div className="preview-modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="preview-modal-header">
                            <h3>{selectedChart.title}</h3>
                            <button className="close-btn" onClick={() => setShowPreview(false)}>✕</button>
                        </div>
                        <div className="preview-modal-body">
                            <ChartRenderer
                                chartId={selectedChart.id}
                                variables={{}}
                                graphqlUrl="/graphql"
                                pollInterval={0}
                            />
                        </div>
                        <div className="preview-modal-footer">
                            <button onClick={() => setShowPreview(false)}>关闭</button>
                            <button className="primary-btn" onClick={() => {
                                setShowPreview(false);
                                handleEdit(selectedChart);
                            }}>
                                编辑图表
                            </button>
                        </div>
                    </div>
                </div>)}
        </div>);
}

export default ChartList;
