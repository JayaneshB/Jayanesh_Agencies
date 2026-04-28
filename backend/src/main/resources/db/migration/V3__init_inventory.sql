CREATE TABLE inventory (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  product_id UUID REFERENCES products(id) UNIQUE,
  total_stock INT NOT NULL DEFAULT 0,
  reserved INT NOT NULL DEFAULT 0,
  sold INT NOT NULL DEFAULT 0,
  available INT GENERATED ALWAYS AS (total_stock - reserved) STORED,
  reorder_threshold INT NOT NULL DEFAULT 0,
  moq INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_movements (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  product_id UUID REFERENCES products(id),
  quantity INT NOT NULL,
  reason VARCHAR(50) NOT NULL,
  note TEXT,
  actor_user_id UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_actor ON stock_movements(actor_user_id);
