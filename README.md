# anyCharts

图表拖拽构建平台，支持通过拖拽组件和数据绑定配置来创建可视化图表。

## 技术栈

- **后端**：Spring Boot 4.0.2 (Java 17) + GraphQL + WebFlux
- **前端**：Vue 3 + Vite + ECharts
- **通信协议**：GraphQL over HTTP

## 快速开始

### 后端

```powershell
cd C:\Users\Roy\IdeaProjects\anyCharts
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8331`

### 前端

```powershell
cd C:\Users\Roy\IdeaProjects\anyCharts\anychart-front
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

## 项目结构

```
anyCharts/
├── src/main/java/com/roy/anycharts/
│   ├── adapter/           # 数据源适配器
│   ├── chart/             # 图表核心逻辑
│   ├── config/            # 配置类
│   └── graphql/           # GraphQL 控制器
├── src/main/resources/
│   └── graphql/           # GraphQL Schema
└── anychart-front/        # Vue 前端
    └── src/components/    # Vue 组件
```

## GraphQL API

查看 `src/main/resources/graphql/schema.graphqls` 获取完整的 Schema 定义。
