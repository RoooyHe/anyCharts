import React, { useEffect, useRef, useState } from 'react';
import * as echarts from 'echarts';

function ChartRenderer({ chartId, variables = {}, graphqlUrl = '/graphql', pollInterval = 0 }) {
  const chartRef = useRef(null);
  const chartInstanceRef = useRef(null);
  const pollHandleRef = useRef(null);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  async function loadChart() {
    setLoading(true);
    setError(null);
    try {
      const resp = await fetch(graphqlUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query Render($id: ID!, $vars: JSON) {
            renderChart(id: $id, variables: $vars) { id option }
          }`,
          variables: { id: chartId, vars: variables }
        })
      });
      if (!resp.ok) throw new Error('Network error: ' + resp.status);
      const json = await resp.json();
      if (json.errors) throw new Error(JSON.stringify(json.errors));

      const renderResult = json.data?.renderChart;
      const chartOption = renderResult?.option?.option || renderResult?.option;

      if (chartOption && chartInstanceRef.current) {
        chartInstanceRef.current.setOption(chartOption, true);
      }
    } catch (e) {
      console.error(e);
      setError(e.message || String(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    // 初始化 ECharts
    if (chartRef.current && !chartInstanceRef.current) {
      chartInstanceRef.current = echarts.init(chartRef.current);
      
      const handleResize = () => {
        if (chartInstanceRef.current) {
          chartInstanceRef.current.resize();
        }
      };
      window.addEventListener('resize', handleResize);

      return () => {
        window.removeEventListener('resize', handleResize);
        if (chartInstanceRef.current) {
          chartInstanceRef.current.dispose();
          chartInstanceRef.current = null;
        }
      };
    }
  }, []);

  useEffect(() => {
    loadChart();

    // 设置轮询
    if (pollInterval > 0) {
      pollHandleRef.current = setInterval(loadChart, pollInterval);
    }

    return () => {
      if (pollHandleRef.current) {
        clearInterval(pollHandleRef.current);
        pollHandleRef.current = null;
      }
    };
  }, [chartId, variables, graphqlUrl, pollInterval]);

  return (
    <div>
      {loading && <div className="loading">加载中...</div>}
      <div ref={chartRef} className="chart-container"></div>
      {error && <div className="error">错误：{error}</div>}
    </div>
  );
}

export default ChartRenderer;
