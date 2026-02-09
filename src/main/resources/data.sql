-- 创建产品销售表
CREATE TABLE IF NOT EXISTS product_sales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100),
    category VARCHAR(50),
    sales_date DATE,
    quantity INT,
    revenue DECIMAL(10, 2),
    region VARCHAR(50)
);

-- 插入示例数据
INSERT INTO product_sales (product_name, category, sales_date, quantity, revenue, region) VALUES
('笔记本电脑', '电子产品', '2026-01-01', 120, 96000.00, '华东'),
('台式电脑', '电子产品', '2026-01-01', 80, 56000.00, '华东'),
('平板电脑', '电子产品', '2026-01-01', 150, 75000.00, '华东'),
('智能手机', '电子产品', '2026-01-02', 200, 120000.00, '华南'),
('智能手表', '电子产品', '2026-01-02', 180, 54000.00, '华南'),
('耳机', '配件', '2026-01-03', 300, 30000.00, '华北'),
('键盘', '配件', '2026-01-03', 250, 12500.00, '华北'),
('鼠标', '配件', '2026-01-04', 400, 16000.00, '华中'),
('显示器', '电子产品', '2026-01-04', 90, 54000.00, '华中'),
('打印机', '办公设备', '2026-01-05', 60, 36000.00, '西南');

-- 创建员工绩效表
CREATE TABLE IF NOT EXISTS employee_performance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    employee_name VARCHAR(100),
    department VARCHAR(50),
    sales_month VARCHAR(7),
    sales_amount DECIMAL(10, 2),
    target_amount DECIMAL(10, 2),
    completion_rate DECIMAL(5, 2)
);

-- 插入员工绩效数据
INSERT INTO employee_performance (employee_name, department, sales_month, sales_amount, target_amount, completion_rate) VALUES
('张三', '销售部', '2026-01', 150000.00, 120000.00, 125.00),
('李四', '销售部', '2026-01', 180000.00, 150000.00, 120.00),
('王五', '销售部', '2026-01', 90000.00, 100000.00, 90.00),
('赵六', '市场部', '2026-01', 110000.00, 100000.00, 110.00),
('钱七', '市场部', '2026-01', 95000.00, 90000.00, 105.56);

-- 创建区域统计表
CREATE TABLE IF NOT EXISTS region_stats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    region VARCHAR(50),
    total_sales DECIMAL(12, 2),
    customer_count INT,
    avg_order_value DECIMAL(10, 2)
);

-- 插入区域统计数据
INSERT INTO region_stats (region, total_sales, customer_count, avg_order_value) VALUES
('华东', 227000.00, 350, 648.57),
('华南', 174000.00, 380, 457.89),
('华北', 42500.00, 550, 77.27),
('华中', 70000.00, 490, 142.86),
('西南', 36000.00, 60, 600.00);
