CREATE TABLE payments (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  order_id UUID REFERENCES orders(id),
  gateway VARCHAR(50) NOT NULL,
  gateway_payment_id VARCHAR(120),
  gateway_status VARCHAR(50),
  amount NUMERIC(12,2) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'INR',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order ON payments(order_id);

CREATE TABLE refunds (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  order_id UUID REFERENCES orders(id),
  payment_id UUID REFERENCES payments(id),
  gateway_refund_id VARCHAR(120),
  amount NUMERIC(12,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refunds_order ON refunds(order_id);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);

CREATE MATERIALIZED VIEW sales_by_day AS
SELECT date_trunc('day', created_at) AS day,
       SUM(total_amount) AS total_sales,
       COUNT(*) AS order_count
FROM orders
GROUP BY 1;

CREATE INDEX ON sales_by_day(day);
