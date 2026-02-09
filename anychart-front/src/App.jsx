import React, { useState } from 'react';
import DashboardList from './components/DashboardList';
import DashboardEditor from './components/DashboardEditor';
import ChartList from './components/ChartList';
import TemplateEditor from './components/TemplateEditor';

function App() {
  const [currentView, setCurrentView] = useState('dashboards'); // 'dashboards', 'charts', 'dashboardEditor', 'chartEditor'
  const [editingDashboard, setEditingDashboard] = useState(null);
  const [editingChart, setEditingChart] = useState(null);

  function handleEditDashboard(dashboard) {
    setEditingDashboard(dashboard);
    setCurrentView('dashboardEditor');
  }

  function handleViewDashboard(dashboard) {
    // TODO: 实现大屏查看模式
    alert(`查看大屏: ${dashboard.name}`);
  }

  function handleEditChart(chart) {
    setEditingChart(chart);
    setCurrentView('chartEditor');
  }

  function handleBackToList() {
    setCurrentView('dashboards');
    setEditingDashboard(null);
    setEditingChart(null);
  }

  return (
    <div className="app">
      <header>
        <h1>anyCharts</h1>
        <p>图表可视化编辑器</p>
        <div className="header-actions">
          {(currentView === 'dashboardEditor' || currentView === 'chartEditor') && (
            <button className="editor-btn" onClick={handleBackToList}>
              🏠 返回主页
            </button>
          )}
          {currentView === 'dashboards' && (
            <button className="editor-btn" onClick={() => setCurrentView('charts')}>
              📊 图表管理
            </button>
          )}
          {currentView === 'charts' && (
            <button className="editor-btn" onClick={() => setCurrentView('dashboards')}>
              📐 大屏管理
            </button>
          )}
        </div>
      </header>

      {currentView === 'dashboards' && (
        <DashboardList onEdit={handleEditDashboard} onView={handleViewDashboard} />
      )}

      {currentView === 'charts' && (
        <ChartList onEdit={handleEditChart} />
      )}

      {currentView === 'dashboardEditor' && (
        <DashboardEditor
          dashboard={editingDashboard}
          onBack={handleBackToList}
          onSave={(config) => {
            console.log('保存大屏配置:', config);
            handleBackToList();
          }}
        />
      )}

      {currentView === 'chartEditor' && (
        <TemplateEditor
          chart={editingChart}
          onBack={handleBackToList}
          onSaved={() => {
            handleBackToList();
          }}
        />
      )}
    </div>
  );
}

export default App;
