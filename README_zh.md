# anyCharts

[English Documentation](./README.md)

一个可视化图表构建平台，支持通过拖拽组件和数据绑定配置来创建图表。

## 功能特性

- 📊 **多种图表类型** - 柱状图、折线图、饼图、散点图、面积图
- 🎨 **大屏编辑器** - 拖拽式大屏构建器，支持组件定位
- 📈 **图表管理** - 创建、编辑和管理图表配置
- 💾 **数据库集成** - 连接数据库并通过可视化界面查询数据
- 🔄 **实时预览** - 保存前预览带有真实数据的图表
- 🎯 **数据绑定** - 灵活的数据源适配器（Mock、REST、数据库）

## 技术栈

### 后端
- **Spring Boot 4.0.2** (Java 17)
- **GraphQL** API
- **WebFlux** (Reactor) 响应式编程
- **H2 Database** 数据持久化
- **Spring Data JPA** 数据库操作

### 前端
- **React 18** - UI 框架
- **Vite 5** - 构建工具
- **ECharts 5** - 图表库

## 快速开始

### 环境要求
- Java 17 或更高版本
- Node.js 16 或更高版本
- Maven 3.6+

### 后端启动

```powershell
# 进入项目目录
cd C:\Users\Roy\IdeaProjects\anyCharts

# 使用 Maven 运行
./mvnw spring-boot:run

# 或者打包后运行
./mvnw package
java -jar target/anycharts-0.0.1-SNAPSHOT.jar
```

后端运行在 `http://localhost:8331`

**H2 控制台访问：**
- URL: `http://localhost:8331/h2-console`
- JDBC URL: `jdbc:h2:file:./data/anycharts`
- 用户名: `sa`
- 密码: (留空)

### 前端启动

```powershell
# 进入前端目录
cd anychart-front

# 安装依赖
npm install

# 运行开发服务器
npm run dev
```

前端运行在 `http://localhost:5173`

## 项目结构

```
anyCharts/
├── src/main/java/com/roy/anycharts/
│   ├── adapter/                    # 数据源适配器
│   │   ├── AdapterRegistry.java
│   │   ├── DataSourceAdapter.java
│   │   └── impl/
│   │       ├── MockAdapter.java    # Mock 数据适配器
│   │       ├── RestAdapter.java    # REST API 适配器
│   │       └── DatabaseAdapter.java # 数据库适配器
│   ├── chart/                      # 图表核心逻辑
│   │   ├── ChartConfig.java
│   │   ├── ChartConfigStore.java
│   │   ├── ChartService.java
│   │   └── DataInitializer.java
│   ├── dashboard/                  # 大屏管理
│   │   ├── Dashboard.java
│   │   └── DashboardStore.java
│   ├── datasource/                 # 数据库连接
│   │   ├── DatabaseConnection.java
│   │   └── DatabaseMetadataService.java
│   ├── config/                     # 配置
│   │   ├── AdapterConfig.java
│   │   └── GraphQlConfig.java
│   └── graphql/                    # GraphQL 控制器
│       └── ChartController.java
├── src/main/resources/
│   ├── application.yml             # 应用配置
│   ├── data.sql                    # 示例数据
│   └── graphql/
│       └── schema.graphqls         # GraphQL schema
└── anychart-front/                 # React 前端
    ├── src/
    │   ├── App.jsx                 # 主应用组件
    │   ├── main.jsx                # 入口文件
    │   ├── styles.css              # 全局样式
    │   └── components/
    │       ├── ChartRenderer.jsx   # 图表渲染
    │       ├── ChartList.jsx       # 图表管理
    │       ├── TemplateEditor.jsx  # 图表编辑器
    │       ├── DashboardList.jsx   # 大屏管理
    │       └── DashboardEditor.jsx # 大屏编辑器
    ├── package.json
    └── vite.config.js
```

## GraphQL API

### 查询

```graphql
# 获取所有图表
query {
  allCharts {
    id
    title
    chartType
    createdAt
  }
}

# 获取图表配置
query {
  chartConfig(id: "sales-bar") {
    id
    title
    chartType
    optionTemplate
    bindings {
      name
      datasourceId
      query
      mappingPath
      bindingKey
    }
  }
}

# 渲染图表数据
query {
  renderChart(id: "sales-bar", variables: {}) {
    id
    option
  }
}

# 获取所有大屏
query {
  allDashboards {
    id
    name
    width
    height
    createdAt
    components {
      id
      type
      x
      y
      width
      height
      chartId
      title
    }
  }
}
```

### 变更

```graphql
# 保存图表配置
mutation {
  saveChartConfig(input: {
    id: "my-chart"
    title: "销售图表"
    chartType: "bar"
    optionTemplate: {...}
    bindings: [...]
  }) {
    id
    title
  }
}

# 保存大屏
mutation {
  saveDashboard(input: {
    id: "my-dashboard"
    name: "销售大屏"
    width: 1920
    height: 1080
    components: [...]
  }) {
    id
    name
  }
}
```

## 数据源适配器

### Mock 适配器
提供示例数据用于测试和开发。

```
datasourceId: "mock-adapter"
query: "mock:sales" | "mock:trend" | "mock:distribution"
```

### REST 适配器
从 REST API 获取数据。

```
datasourceId: "rest-adapter"
query: "https://api.example.com/data"
```

### 数据库适配器
从连接的数据库查询数据。

```
datasourceId: "database-adapter"
query: "h2-default:SELECT * FROM product_sales"
```

## 配置

### 后端配置 (`application.yml`)

```yaml
server:
  port: 8331

spring:
  datasource:
    url: jdbc:h2:file:./data/anycharts
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: update  # 重启时保留数据
```

### 前端配置 (`vite.config.js`)

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

## 开发指南

### 添加新图表类型

1. 在 `TemplateEditor.jsx` 中添加图表类型配置
2. 在 `CHART_TYPES` 数组中定义默认模板
3. 在 `CHART_TYPE_ICONS` 中添加图标映射

### 添加新数据适配器

1. 实现 `DataSourceAdapter` 接口
2. 在 `AdapterRegistry` 中注册
3. 在 `AdapterConfig` 中添加配置

## 常见问题

### 后端问题

- **端口 8331 已被占用**：在 `application.yml` 中修改端口
- **数据库连接失败**：检查 H2 配置
- **GraphQL 404**：确保 schema 文件在 `resources/graphql/` 目录

### 前端问题

- **无法连接后端**：确认后端运行在 8331 端口
- **图表不显示**：检查浏览器控制台错误
- **构建失败**：删除 `node_modules` 并重新运行 `npm install`

## 许可证

MIT License

## 贡献

欢迎贡献！请随时提交 Pull Request。
