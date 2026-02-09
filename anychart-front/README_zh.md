# anyCharts 前端

[English Documentation](./README.md)

anyCharts 可视化平台的 React 前端。

## 功能特性

- 📊 **图表管理** - 基于网格的卡片布局管理图表
- 🎨 **大屏构建器** - 拖拽式大屏编辑器
- ✏️ **图表编辑器** - 用户友好的图表配置界面
- 👁️ **实时预览** - 保存前预览带有真实数据的图表
- 💾 **数据库集成** - 数据库查询的可视化界面
- 🔄 **实时更新** - 数据变化时自动刷新图表

## 技术栈

- **React 18** - 使用 Hooks 的 UI 框架
- **Vite 5** - 快速构建工具和开发服务器
- **ECharts 5** - 强大的图表库
- **GraphQL** - API 通信

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 `http://localhost:5173`

### 生产构建

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
src/
├── App.jsx                     # 主应用组件
├── main.jsx                    # 应用入口
├── styles.css                  # 全局样式
└── components/
    ├── ChartRenderer.jsx       # ECharts 图表渲染
    ├── ChartList.jsx           # 图表管理（网格布局）
    ├── TemplateEditor.jsx      # 图表配置编辑器
    ├── DashboardList.jsx       # 大屏管理（列表+预览）
    └── DashboardEditor.jsx     # 大屏拖拽编辑器
```

## 组件概览

### ChartRenderer
基于后端图表配置渲染 ECharts。

**属性：**
- `chartId` - 要渲染的图表 ID
- `variables` - 数据绑定变量
- `graphqlUrl` - GraphQL 端点
- `pollInterval` - 自动刷新间隔（毫秒）

### ChartList
以响应式网格卡片形式显示所有图表。

**功能：**
- 基于卡片的布局，带图表图标
- 预览模态框，实时图表渲染
- 编辑和删除操作
- 创建时间显示

### TemplateEditor
用户友好的图表配置编辑器。

**功能：**
- 图表类型选择（柱状图、折线图、饼图、散点图、面积图）
- 可视化配置输入（无需编辑 JSON）
- 数据源绑定配置
- 数据库查询构建器
- 真实数据实时预览

### DashboardList
使用列表和预览布局管理大屏。

**功能：**
- 左侧边栏显示大屏列表
- 右侧面板显示缩略图预览
- 大屏元数据显示
- 编辑和查看操作

### DashboardEditor
拖拽式大屏构建器。

**功能：**
- 组件面板（图表类型）
- 可拖拽画布（1920x1080）
- 组件调整大小手柄
- 配置属性面板
- 绑定到现有图表

## GraphQL 通信

### 查询

```javascript
// 获取所有图表
const query = `
  query {
    allCharts {
      id
      title
      chartType
      createdAt
    }
  }
`;

// 渲染图表数据
const query = `
  query {
    renderChart(id: "sales-bar", variables: {}) {
      id
      option
    }
  }
`;
```

### 变更

```javascript
// 保存图表配置
const mutation = `
  mutation SaveChart($input: ChartConfigInput!) {
    saveChartConfig(input: $input) {
      id
      title
    }
  }
`;

// 保存大屏
const mutation = `
  mutation SaveDashboard($input: DashboardInput!) {
    saveDashboard(input: $input) {
      id
      name
    }
  }
`;
```

## 样式

项目使用原生 CSS 和现代设计系统：

- **颜色**：紫色渐变主色，语义化状态颜色
- **布局**：Flexbox 和 Grid 响应式设计
- **组件**：基于卡片的 UI，带阴影和悬停效果
- **排版**：系统字体，清晰的层次结构

## 开发指南

### 添加新图表类型

1. 在 `TemplateEditor.jsx` 的 `CHART_TYPES` 数组中添加：
```javascript
{
  id: 'newtype',
  name: '新类型',
  icon: '📊',
  defaultConfig: { ... }
}
```

2. 在 `CHART_TYPE_ICONS` 中添加图标映射

3. 在 `CHART_TYPE_COLORS` 中添加颜色映射

### 添加新组件

1. 在 `src/components/` 中创建组件文件
2. 在 `App.jsx` 中导入和使用
3. 在 `styles.css` 中添加对应样式

## 后端集成

前端通过 HTTP 上的 GraphQL 与后端通信。

**Vite 代理配置：**
```javascript
export default {
  server: {
    proxy: {
      '/graphql': {
        target: 'http://localhost:8331',
        changeOrigin: true
      }
    }
  }
}
```

确保后端运行在 `http://localhost:8331`。

## 常见问题

### 图表不渲染

- 检查 ECharts 是否正确初始化
- 确认图表容器有尺寸
- 检查浏览器控制台错误

### GraphQL 错误

- 确认后端正在运行
- 检查网络标签的请求/响应
- 验证 GraphQL 查询语法

### 构建问题

- 删除 `node_modules` 和 `package-lock.json`
- 重新运行 `npm install`
- 清除 Vite 缓存：`npm run dev -- --force`

## 性能优化建议

- 对昂贵的组件使用 `React.memo`
- 为大列表实现虚拟滚动
- 配置变化时对图表更新进行防抖
- 对大组件使用代码分割

## 浏览器支持

- Chrome/Edge（最新版）
- Firefox（最新版）
- Safari（最新版）

## 许可证

MIT License
