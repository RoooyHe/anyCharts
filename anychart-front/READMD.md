# anychart-front (Vite + Vue 3 PoC)

This is a minimal frontend demo for anyCharts. It fetches a pre-rendered ECharts `option` from the backend GraphQL `renderChart` API and renders it with ECharts.

Prereqs:
- Node 16+ / npm or pnpm
- Backend (Spring Boot) running at http://localhost:8080 (GraphQL path `/graphql`)

Quick start:
1. cd anychart-front
2. npm install
3. npm run dev
4. Open http://localhost:5173

Notes:
- Dev server proxies `/graphql` to `http://localhost:8080` (see vite.config.js). If your backend is on other host/port, update the proxy.
- This PoC uses simple polling for live updates (pollInterval). For production, use GraphQL subscriptions (websocket) to connect to backend Flux stream.