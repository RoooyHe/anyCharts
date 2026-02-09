package com.roy.anycharts.chart;

import com.roy.anycharts.chart.entity.ChartConfigEntity;
import com.roy.anycharts.chart.entity.DataSourceBindingEntity;
import com.roy.anycharts.chart.repository.ChartConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
  private final ChartConfigRepository repository;

  public DataInitializer(ChartConfigRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    System.out.println("=== DataInitializer 开始检查数据 ===");
    
    try {
      // 检查是否已有数据
      long count = repository.count();
      if (count > 0) {
        System.out.println("数据库已有 " + count + " 条图表记录，跳过初始化");
        return;
      }
      
      System.out.println("数据库为空，开始初始化示例数据...");
      
      // 柱状图
      ChartConfigEntity barChart = new ChartConfigEntity("sales-bar", "销售柱状图", "bar",
          "{\"title\":{\"text\":\"销售数据\"},\"tooltip\":{\"trigger\":\"axis\"},\"legend\":{\"data\":[\"销量\"]},\"xAxis\":{\"type\":\"category\",\"data\":\"{{binding:categories}}\"},\"yAxis\":{\"type\":\"value\"},\"series\":[{\"name\":\"销量\",\"type\":\"bar\",\"data\":\"{{binding:series1}}\"}]}");
      addBinding(barChart, "categories", "mock-adapter", "mock:sales", "$.items[*].date", "categories");
      addBinding(barChart, "series1", "mock-adapter", "mock:sales", "$.items[*].value", "series1");
      repository.save(barChart);
      System.out.println("✓ 保存柱状图");

      // 折线图
      ChartConfigEntity lineChart = new ChartConfigEntity("trend-line", "趋势折线图", "line",
          "{\"title\":{\"text\":\"趋势分析\"},\"tooltip\":{\"trigger\":\"axis\"},\"legend\":{\"data\":[\"增长率\"]},\"xAxis\":{\"type\":\"category\",\"boundaryGap\":false,\"data\":\"{{binding:categories}}\"},\"yAxis\":{\"type\":\"value\"},\"series\":[{\"name\":\"增长率\",\"type\":\"line\",\"data\":\"{{binding:series1}}\",\"smooth\":true}]}");
      addBinding(lineChart, "categories", "mock-adapter", "mock:trend", "$.items[*].date", "categories");
      addBinding(lineChart, "series1", "mock-adapter", "mock:trend", "$.items[*].value", "series1");
      repository.save(lineChart);
      System.out.println("✓ 保存折线图");

      // 饼图
      ChartConfigEntity pieChart = new ChartConfigEntity("distribution-pie", "分布饼图", "pie",
          "{\"title\":{\"text\":\"占比分布\",\"left\":\"center\"},\"tooltip\":{\"trigger\":\"item\"},\"legend\":{\"orient\":\"vertical\",\"left\":\"left\"},\"series\":[{\"type\":\"pie\",\"radius\":\"50%\",\"data\":\"{{binding:pieData}}\"}]}");
      addBinding(pieChart, "pieData", "mock-adapter", "mock:distribution", "$.items[*]", "pieData");
      repository.save(pieChart);
      System.out.println("✓ 保存饼图");

      // 散点图
      ChartConfigEntity scatterChart = new ChartConfigEntity("correlation-scatter", "散点图", "scatter",
          "{\"title\":{\"text\":\"相关性分析\"},\"tooltip\":{},\"xAxis\":{\"name\":\"X\",\"type\":\"value\"},\"yAxis\":{\"name\":\"Y\",\"type\":\"value\"},\"series\":[{\"type\":\"scatter\",\"symbolSize\":20,\"data\":\"{{binding:scatterData}}\"}]}");
      addBinding(scatterChart, "scatterData", "mock-adapter", "mock:correlation", "$.items[*]", "scatterData");
      repository.save(scatterChart);
      System.out.println("✓ 保存散点图");

      // 面积图
      ChartConfigEntity areaChart = new ChartConfigEntity("growth-area", "面积图", "area",
          "{\"title\":{\"text\":\"累计增长\"},\"tooltip\":{\"trigger\":\"axis\"},\"xAxis\":{\"type\":\"category\",\"boundaryGap\":false,\"data\":\"{{binding:categories}}\"},\"yAxis\":{\"type\":\"value\"},\"series\":[{\"name\":\"累计\",\"type\":\"line\",\"areaStyle\":{},\"data\":\"{{binding:series1}}\"}]}");
      addBinding(areaChart, "categories", "mock-adapter", "mock:growth", "$.items[*].date", "categories");
      addBinding(areaChart, "series1", "mock-adapter", "mock:growth", "$.items[*].value", "series1");
      repository.save(areaChart);
      System.out.println("✓ 保存面积图");

      // 兼容旧示例
      ChartConfigEntity oldChart = new ChartConfigEntity("sales-2026", "销售图", "bar",
          "{\"title\":{\"text\":\"销售\"},\"xAxis\":{\"data\":\"{{binding:categories}}\"},\"yAxis\":{},\"series\":[{\"type\":\"bar\",\"data\":\"{{binding:series1}}\"}]}");
      addBinding(oldChart, "ds1", "mock-adapter", "mock:sales", "$.items[*].date", "categories");
      addBinding(oldChart, "ds2", "mock-adapter", "mock:sales", "$.items[*].value", "series1");
      repository.save(oldChart);
      System.out.println("✓ 保存旧示例图");

      // 数据库示例：产品销售统计
      ChartConfigEntity dbChart = new ChartConfigEntity("db-product-sales", "产品销售统计（数据库）", "bar",
          "{\"title\":{\"text\":\"产品销售统计\"},\"tooltip\":{\"trigger\":\"axis\"},\"legend\":{},\"xAxis\":{\"type\":\"category\",\"data\":\"{{binding:products}}\"},\"yAxis\":{\"type\":\"value\",\"name\":\"销售额\"},\"series\":[{\"name\":\"销售额\",\"type\":\"bar\",\"data\":\"{{binding:revenue}}\"}]}");
      addBinding(dbChart, "products", "database-adapter", 
          "h2-default:SELECT product_name, revenue FROM product_sales ORDER BY revenue DESC LIMIT 5", 
          "$.items[*].product_name", "products");
      addBinding(dbChart, "revenue", "database-adapter", 
          "h2-default:SELECT product_name, revenue FROM product_sales ORDER BY revenue DESC LIMIT 5", 
          "$.items[*].revenue", "revenue");
      repository.save(dbChart);
      System.out.println("✓ 保存数据库示例图");

      count = repository.count();
      System.out.println("=== 数据初始化完成，共 " + count + " 条记录 ===");
    } catch (Exception e) {
      System.err.println("❌ 数据初始化失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void addBinding(ChartConfigEntity chart, String name, String datasourceId, String query, String mappingPath, String bindingKey) {
    DataSourceBindingEntity binding = new DataSourceBindingEntity(name, datasourceId, query, mappingPath, bindingKey, false);
    binding.setChartConfig(chart);
    chart.getBindings().add(binding);
  }
}
