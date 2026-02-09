import React, {useEffect, useState} from 'react';

function DashboardList({onEdit, onView}) {
    const [dashboards, setDashboards] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedId, setSelectedId] = useState(null);

    useEffect(() => {
        fetchDashboards();
    }, []);

    async function fetchDashboards() {
        setLoading(true);
        const query = `query { allDashboards { id name width height createdAt components { id type x y width height chartId title } } }`;
        try {
            const resp = await fetch('/graphql', {
                method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({query})
            });
            const json = await resp.json();
            if (json.data?.allDashboards) {
                setDashboards(json.data.allDashboards);
                if (json.data.allDashboards.length > 0) {
                    setSelectedId(json.data.allDashboards[0].id);
                }
            }
        } catch (e) {
            console.error('获取大屏列表失败:', e);
        } finally {
            setLoading(false);
        }
    }

    function formatDate(dateStr) {
        if (!dateStr) return '未知';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN', {
            year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
        });
    }

    const selectedDashboard = dashboards.find(d => d.id === selectedId);

    return (<div className="dashboard-list-container">
            <div className="dashboard-list-header">
                <h2>我的大屏</h2>
                <button className="create-btn" onClick={() => onEdit(null)}>
                    ➕ 创建新大屏
                </button>
            </div>

            <div className="dashboard-list-content">
                {/* 左侧列表 */}
                <div className="dashboard-sidebar">
                    {loading && <div className="loading">加载中...</div>}
                    {!loading && dashboards.length === 0 && (<div className="empty-state">
                            <p>还没有大屏</p>
                            <button onClick={() => onEdit(null)}>创建第一个大屏</button>
                        </div>)}
                    {dashboards.map(dashboard => (<div
                            key={dashboard.id}
                            className={`dashboard-item ${selectedId === dashboard.id ? 'active' : ''}`}
                            onClick={() => setSelectedId(dashboard.id)}
                        >
                            <div className="dashboard-item-icon">📐</div>
                            <div className="dashboard-item-info">
                                <div className="dashboard-item-name">{dashboard.name}</div>
                                <div className="dashboard-item-meta">
                                    {dashboard.width} × {dashboard.height}
                                </div>
                                <div className="dashboard-item-date">
                                    {formatDate(dashboard.createdAt)}
                                </div>
                            </div>
                        </div>))}
                </div>

                {/* 右侧预览 */}
                <div className="dashboard-preview">
                    {selectedDashboard ? (<>
                            <div className="preview-header">
                                <h3>{selectedDashboard.name}</h3>
                                <div className="preview-actions">
                                    <button onClick={() => onView(selectedDashboard)}>
                                        👁️ 查看
                                    </button>
                                    <button onClick={() => onEdit(selectedDashboard)}>
                                        ✏️ 编辑
                                    </button>
                                </div>
                            </div>
                            <div className="preview-canvas">
                                <div
                                    className="preview-miniature"
                                    style={{
                                        width: '100%',
                                        height: '100%',
                                        background: '#1a1a2e',
                                        position: 'relative',
                                        overflow: 'hidden'
                                    }}
                                >
                                    {selectedDashboard.components.map(comp => {
                                        const scale = 0.4; // 缩放比例
                                        return (<div
                                                key={comp.id}
                                                style={{
                                                    position: 'absolute',
                                                    left: `${(comp.x / selectedDashboard.width) * 100}%`,
                                                    top: `${(comp.y / selectedDashboard.height) * 100}%`,
                                                    width: `${(comp.width / selectedDashboard.width) * 100}%`,
                                                    height: `${(comp.height / selectedDashboard.height) * 100}%`,
                                                    background: 'rgba(100, 149, 237, 0.2)',
                                                    border: '1px solid rgba(100, 149, 237, 0.5)',
                                                    borderRadius: '4px',
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'center',
                                                    fontSize: '10px',
                                                    color: '#6495ed'
                                                }}
                                            >
                                                {comp.title || comp.type}
                                            </div>);
                                    })}
                                </div>
                            </div>
                            <div className="preview-info">
                                <div className="info-item">
                                    <span className="info-label">尺寸:</span>
                                    <span
                                        className="info-value">{selectedDashboard.width} × {selectedDashboard.height}</span>
                                </div>
                                <div className="info-item">
                                    <span className="info-label">组件数:</span>
                                    <span className="info-value">{selectedDashboard.components.length}</span>
                                </div>
                                <div className="info-item">
                                    <span className="info-label">创建时间:</span>
                                    <span className="info-value">{formatDate(selectedDashboard.createdAt)}</span>
                                </div>
                            </div>
                        </>) : (<div className="preview-empty">
                            <p>选择一个大屏查看预览</p>
                        </div>)}
                </div>
            </div>
        </div>);
}

export default DashboardList;
