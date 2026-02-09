import React, { useState } from 'react';
import DashboardList from './components/DashboardList';
import DashboardEditor from './components/DashboardEditor';
import TemplateEditor from './components/TemplateEditor';

function App() {
  const [currentView, setCurrentView] = useState('list'); // 'list', 'editor', 'chartEditor'
  const [editingDashboard, setEditingDashboard] = useState(null);

  function handleEdit(dashboard) {
    setEditingDashboard(dashboard);
    setCurrentView('editor');
  }

  function handleView(dashboard) {
    // TODO: 实现大屏查看模式
    alert(`查看大屏: ${dashboard.name}`);
  }

  function handleBackToList() {
    setCurrentView('list');
    setEditingDashboard(null);
  }

  return (
    <div className="app">
      <header>
        <h1>anyCharts</h1>
        <p>图表可视化编辑器</p>
        <div className="header-actions">
          {currentView !== 'list' && (
            <button className="editor-btn" onClick={handleBackToList}>
              🏠 返回主页
            </button>
          )}
          {currentView === 'list' && (
            <button className="editor-btn" onClick={() => setCurrentView('chartEditor')}>
              ✏️ 图表编辑器
            </button>
          )}
        </div>
      </header>

      {currentView === 'list' && (
        <DashboardList onEdit={handleEdit} onView={handleView} />
      )}

      {currentView === 'editor' && (
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
          onBack={handleBackToList}
          onSaved={() => {}}
        />
      )}
    </div>
  );
}

export default App;
