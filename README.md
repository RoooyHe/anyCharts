# anyCharts

[中文文档](./README_zh.md)

A visual chart building platform that supports creating charts through drag-and-drop components and data binding configuration.

## Features

- 📊 **Multiple Chart Types** - Bar, Line, Pie, Scatter, Area charts
- 🎨 **Dashboard Editor** - Drag-and-drop dashboard builder with component positioning
- 📈 **Chart Management** - Create, edit, and manage chart configurations
- 💾 **Database Integration** - Connect to databases and query data with visual interface
- 🔄 **Real-time Preview** - Preview charts with live data before saving
- 🎯 **Data Binding** - Flexible data source adapters (Mock, REST, Database)

## Tech Stack

### Backend
- **Spring Boot 4.0.2** (Java 17)
- **GraphQL** for API
- **WebFlux** (Reactor) for reactive programming
- **H2 Database** for data persistence
- **Spring Data JPA** for database operations

### Frontend
- **React 18** - UI framework
- **Vite 5** - Build tool
- **ECharts 5** - Chart library

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- Maven 3.6+

### Backend Setup

```powershell
# Navigate to project directory
cd C:\Users\Roy\IdeaProjects\anyCharts

# Run with Maven
./mvnw spring-boot:run

# Or build and run
./mvnw package
java -jar target/anycharts-0.0.1-SNAPSHOT.jar
```

Backend runs on `http://localhost:8331`

**H2 Console Access:**
- URL: `http://localhost:8331/h2-console`
- JDBC URL: `jdbc:h2:file:./data/anycharts`
- Username: `sa`
- Password: (empty)

### Frontend Setup

```powershell
# Navigate to frontend directory
cd anychart-front

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs on `http://localhost:5173`

## Project Structure

```
anyCharts/
├── src/main/java/com/roy/anycharts/
│   ├── adapter/                    # Data source adapters
│   │   ├── AdapterRegistry.java
│   │   ├── DataSourceAdapter.java
│   │   └── impl/
│   │       ├── MockAdapter.java    # Mock data adapter
│   │       ├── RestAdapter.java    # REST API adapter
│   │       └── DatabaseAdapter.java # Database adapter
│   ├── chart/                      # Chart core logic
│   │   ├── ChartConfig.java
│   │   ├── ChartConfigStore.java
│   │   ├── ChartService.java
│   │   └── DataInitializer.java
│   ├── dashboard/                  # Dashboard management
│   │   ├── Dashboard.java
│   │   └── DashboardStore.java
│   ├── datasource/                 # Database connections
│   │   ├── DatabaseConnection.java
│   │   └── DatabaseMetadataService.java
│   ├── config/                     # Configuration
│   │   ├── AdapterConfig.java
│   │   └── GraphQlConfig.java
│   └── graphql/                    # GraphQL controllers
│       └── ChartController.java
├── src/main/resources/
│   ├── application.yml             # Application config
│   ├── data.sql                    # Sample data
│   └── graphql/
│       └── schema.graphqls         # GraphQL schema
└── anychart-front/                 # React frontend
    ├── src/
    │   ├── App.jsx                 # Main app component
    │   ├── main.jsx                # Entry point
    │   ├── styles.css              # Global styles
    │   └── components/
    │       ├── ChartRenderer.jsx   # Chart rendering
    │       ├── ChartList.jsx       # Chart management
    │       ├── TemplateEditor.jsx  # Chart editor
    │       ├── DashboardList.jsx   # Dashboard management
    │       └── DashboardEditor.jsx # Dashboard editor
    ├── package.json
    └── vite.config.js
```

## GraphQL API

### Queries

```graphql
# Get all charts
query {
  allCharts {
    id
    title
    chartType
    createdAt
  }
}

# Get chart configuration
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

# Render chart with data
query {
  renderChart(id: "sales-bar", variables: {}) {
    id
    option
  }
}

# Get all dashboards
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

### Mutations

```graphql
# Save chart configuration
mutation {
  saveChartConfig(input: {
    id: "my-chart"
    title: "Sales Chart"
    chartType: "bar"
    optionTemplate: {...}
    bindings: [...]
  }) {
    id
    title
  }
}

# Save dashboard
mutation {
  saveDashboard(input: {
    id: "my-dashboard"
    name: "Sales Dashboard"
    width: 1920
    height: 1080
    components: [...]
  }) {
    id
    name
  }
}
```

## Data Source Adapters

### Mock Adapter
Provides sample data for testing and development.

```
datasourceId: "mock-adapter"
query: "mock:sales" | "mock:trend" | "mock:distribution"
```

### REST Adapter
Fetches data from REST APIs.

```
datasourceId: "rest-adapter"
query: "https://api.example.com/data"
```

### Database Adapter
Queries data from connected databases.

```
datasourceId: "database-adapter"
query: "h2-default:SELECT * FROM product_sales"
```

## Configuration

### Backend Configuration (`application.yml`)

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
      ddl-auto: update  # Preserves data on restart
```

### Frontend Configuration (`vite.config.js`)

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

## Development

### Adding a New Chart Type

1. Add chart type configuration in `TemplateEditor.jsx`
2. Define default template in `CHART_TYPES` array
3. Add icon mapping in `CHART_TYPE_ICONS`

### Adding a New Data Adapter

1. Implement `DataSourceAdapter` interface
2. Register in `AdapterRegistry`
3. Add configuration in `AdapterConfig`

## Troubleshooting

### Backend Issues

- **Port 8331 already in use**: Change port in `application.yml`
- **Database connection failed**: Check H2 configuration
- **GraphQL 404**: Ensure schema file is in `resources/graphql/`

### Frontend Issues

- **Cannot connect to backend**: Verify backend is running on port 8331
- **Charts not displaying**: Check browser console for errors
- **Build fails**: Delete `node_modules` and run `npm install` again

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
