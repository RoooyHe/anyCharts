# AGENTS.md - anyCharts 项目指南

## 项目概述

**anyCharts** 是一个图表拖拽构建平台，支持通过拖拽组件和数据绑定配置来创建可视化图表。

- **后端**：Spring Boot 4.0.2 (Java 17) + GraphQL + WebFlux (Reactor)
- **前端**：Vue 3 + Vite + ECharts
- **通信协议**：GraphQL over HTTP
- **端口**：后端 8331，前端 5173

## 目录结构

```
anyCharts/
├── src/main/java/com/roy/anycharts/
│   ├── AnyChartsApplication.java      # Spring Boot 启动类
│   ├── adapter/                        # 数据源适配器
│   │   ├── AdapterRegistry.java        # 适配器注册中心
│   │   ├── DataSourceAdapter.java      # 适配器接口
│   │   └── impl/
│   │       ├── MockAdapter.java        # Mock 数据适配器
│   │       └── RestAdapter.java        # REST API 适配器
│   ├── chart/                          # 图表核心逻辑
│   │   ├── ChartConfig.java            # 图表配置实体
│   │   ├── ChartConfigStore.java       # 图表配置存储
│   │   ├── ChartService.java           # 图表渲染服务
│   │   └── DataSourceBinding.java      # 数据源绑定
│   ├── config/
│   │   ├── AdapterConfig.java          # 适配器配置
│   │   └── GraphQlConfig.java          # GraphQL 配置（JSON scalar）
│   └── graphql/
│       └── ChartController.java        # GraphQL 控制器
├── src/main/resources/
│   ├── application.yml                 # 应用配置
│   └── graphql/schema.graphqls         # GraphQL Schema
└── anychart-front/                     # Vue 前端
    ├── src/
    │   ├── App.vue                     # 主应用组件
    │   ├── main.js                     # 应用入口
    │   ├── styles.css                  # 全局样式
    │   └── components/
    │       └── ChartRenderer.vue       # 图表渲染组件
    ├── vite-config.js                  # Vite 配置
    └── package.json                    # 前端依赖
```

## 构建与运行

### 后端 (Spring Boot)

```powershell
# 开发模式运行 (端口 8331)
cd C:\Users\Roy\IdeaProjects\anyCharts
./mvnw spring-boot:run

# 或打包后运行
./mvnw package
java -jar target/anycharts-0.0.1-SNAPSHOT.jar
```

### 前端 (Vue + Vite)

```powershell
cd C:\Users\Roy\IdeaProjects\anyCharts\anychart-front

# 安装依赖
npm install

# 开发模式 (端口 5173)
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

### 完整启动

需要同时启动后端和前端：
1. 后端：`./mvnw spring-boot:run`
2. 前端：`npm run dev`

前端 Vite 配置了 GraphQL 代理，将 `/graphql` 请求转发到后端 `http://localhost:8331/graphql`。

## GraphQL API

### Schema

```graphql
scalar JSON

type Query {
    renderChart(id: ID!, variables: JSON): RenderedChart
    chartConfig(id: ID!): ChartConfig
}

type Subscription {
    chartUpdates(id: ID!, variables: JSON): RenderedChart
}

type DataSourceBinding {
    name: String!
    datasourceId: ID!
    query: String!
    mappingPath: String
    bindingKey: String
    stream: Boolean
}

type ChartConfig {
    id: ID!
    title: String
    optionTemplate: JSON
    bindings: [DataSourceBinding!]!
}

type RenderedChart {
    id: ID!
    option: JSON
}
```

### 示例请求

```bash
curl -X POST http://localhost:8331/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query Render($id: ID!) { renderChart(id: $id) { id option } }",
    "variables": {"id": "sales-2026"}
  }'
```

## 数据流架构

1. **前端请求**：Vue 组件通过 GraphQL 查询 `renderChart`
2. **后端处理**：
   - `ChartController` 接收请求
   - `ChartService` 从 `ChartConfigStore` 获取配置
   - 通过 `AdapterRegistry` 调用对应的 `DataSourceAdapter`
   - 使用 JsonPath 提取数据，替换模板中的占位符
   - 返回渲染好的 ECharts option
3. **前端渲染**：ECharts 接收 option 配置并渲染图表

### 模板占位符格式

后端模板使用 `"{{binding:key}}"` 格式的占位符，例如：

```json
{
  "title": {"text": "销售"},
  "xAxis": {"data": "{{binding:categories}}"},
  "yAxis": {},
  "series": [{"type": "bar", "data": "{{binding:series1}}"}]
}
```

`DataSourceBinding` 的 `bindingKey` 对应占位符中的 key，`mappingPath` 是 JsonPath 表达式。

## 开发约定

### 后端 (Java)

- 使用 **Reactor** (Mono/Flux) 处理异步操作
- GraphQL 控制器方法参数需要 `@Argument` 注解
- JSON scalar 通过 `GraphQlConfig` 配置
- 数据源适配器注册在 `AdapterRegistry`

### 前端 (Vue)

- 使用 **Vue 3 Composition API** (`<script setup>`)
- ECharts 在 `onMounted` 中初始化，`onBeforeUnmount` 中销毁
- 图表容器始终存在于 DOM，使用 CSS 控制显示/隐藏
- 轮询机制用于简单演示，生产环境应使用 GraphQL Subscription

### Git

- `node_modules/` 被 Git 忽略
- 提交前确保后端和前端都能正常运行

## 常见问题排查

1. **404 /graphql**：检查 GraphQL schema 是否在 `graphql/` 目录
2. **JSON scalar 错误**：确保 `GraphQlConfig` 已配置 `ExtendedScalars.Json`
3. **ECharts 尺寸为 0**：图表容器必须在 DOM 中存在且有尺寸
4. **参数无法识别**：GraphQL 方法参数需要 `@Argument` 注解
