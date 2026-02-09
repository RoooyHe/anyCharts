# anyCharts Frontend

[中文文档](./README_zh.md)

React-based frontend for the anyCharts visualization platform.

## Features

- 📊 **Chart Management** - Grid-based card layout for managing charts
- 🎨 **Dashboard Builder** - Drag-and-drop dashboard editor
- ✏️ **Chart Editor** - User-friendly chart configuration interface
- 👁️ **Live Preview** - Preview charts with real data before saving
- 💾 **Database Integration** - Visual interface for database queries
- 🔄 **Real-time Updates** - Automatic chart refresh on data changes

## Tech Stack

- **React 18** - UI framework with Hooks
- **Vite 5** - Fast build tool and dev server
- **ECharts 5** - Powerful charting library
- **GraphQL** - API communication

## Quick Start

### Install Dependencies

```bash
npm install
```

### Development Mode

```bash
npm run dev
```

Visit `http://localhost:5173`

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Project Structure

```
src/
├── App.jsx                     # Main application component
├── main.jsx                    # Application entry point
├── styles.css                  # Global styles
└── components/
    ├── ChartRenderer.jsx       # Chart rendering with ECharts
    ├── ChartList.jsx           # Chart management (grid layout)
    ├── TemplateEditor.jsx      # Chart configuration editor
    ├── DashboardList.jsx       # Dashboard management (list + preview)
    └── DashboardEditor.jsx     # Dashboard drag-and-drop editor
```

## Component Overview

### ChartRenderer
Renders ECharts based on chart configuration from backend.

**Props:**
- `chartId` - Chart ID to render
- `variables` - Variables for data binding
- `graphqlUrl` - GraphQL endpoint
- `pollInterval` - Auto-refresh interval (ms)

### ChartList
Displays all charts in a responsive grid with cards.

**Features:**
- Card-based layout with chart icons
- Preview modal with live chart rendering
- Edit and delete actions
- Creation timestamp display

### TemplateEditor
User-friendly chart configuration editor.

**Features:**
- Chart type selection (bar, line, pie, scatter, area)
- Visual configuration inputs (no JSON editing)
- Data source binding configuration
- Database query builder
- Live preview with real data

### DashboardList
Manages dashboards with list and preview layout.

**Features:**
- Left sidebar with dashboard list
- Right panel with miniature preview
- Dashboard metadata display
- Edit and view actions

### DashboardEditor
Drag-and-drop dashboard builder.

**Features:**
- Component palette (chart types)
- Draggable canvas (1920x1080)
- Resize handles for components
- Properties panel for configuration
- Chart binding to existing charts

## GraphQL Communication

### Queries

```javascript
// Fetch all charts
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

// Render chart with data
const query = `
  query {
    renderChart(id: "sales-bar", variables: {}) {
      id
      option
    }
  }
`;
```

### Mutations

```javascript
// Save chart configuration
const mutation = `
  mutation SaveChart($input: ChartConfigInput!) {
    saveChartConfig(input: $input) {
      id
      title
    }
  }
`;

// Save dashboard
const mutation = `
  mutation SaveDashboard($input: DashboardInput!) {
    saveDashboard(input: $input) {
      id
      name
    }
  }
`;
```

## Styling

The project uses vanilla CSS with a modern design system:

- **Colors**: Purple gradient primary, semantic colors for states
- **Layout**: Flexbox and Grid for responsive design
- **Components**: Card-based UI with shadows and hover effects
- **Typography**: System fonts with clear hierarchy

## Development Guidelines

### Adding a New Chart Type

1. Add to `CHART_TYPES` array in `TemplateEditor.jsx`:
```javascript
{
  id: 'newtype',
  name: 'New Type',
  icon: '📊',
  defaultConfig: { ... }
}
```

2. Add icon mapping in `CHART_TYPE_ICONS`

3. Add color mapping in `CHART_TYPE_COLORS`

### Adding a New Component

1. Create component file in `src/components/`
2. Import and use in `App.jsx`
3. Add corresponding styles in `styles.css`

## Backend Integration

The frontend communicates with the backend via GraphQL over HTTP.

**Vite Proxy Configuration:**
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

Ensure the backend is running on `http://localhost:8331`.

## Troubleshooting

### Charts Not Rendering

- Check if ECharts is properly initialized
- Verify chart container has dimensions
- Check browser console for errors

### GraphQL Errors

- Verify backend is running
- Check network tab for request/response
- Validate GraphQL query syntax

### Build Issues

- Delete `node_modules` and `package-lock.json`
- Run `npm install` again
- Clear Vite cache: `npm run dev -- --force`

## Performance Tips

- Use `React.memo` for expensive components
- Implement virtual scrolling for large lists
- Debounce chart updates on configuration changes
- Use code splitting for large components

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## License

MIT License
