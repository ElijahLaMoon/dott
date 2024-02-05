CREATE TABLE IF NOT EXISTS orders (
  id INTEGER PRIMARY KEY,
  uuid TEXT NOT NULL,
  customer_name TEXT NOT NULL,
  customer_contact TEXT NOT NULL,
  shipping_address TEXT NOT NULL,
  grand_total INTEGER NOT NULL,
  created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_orders_uuid ON orders (uuid);