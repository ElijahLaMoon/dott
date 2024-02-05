CREATE TABLE IF NOT EXISTS items (
  id INTEGER PRIMARY KEY,
  product_uuid TEXT NOT NULL,
  shipping_fee INTEGER NOT NULL,
  tax_amount INTEGER NOT NULL,
  cost INTEGER NOT NULL,
  FOREIGN KEY (product_uuid) REFERENCES products (uuid)
);

CREATE UNIQUE INDEX idx_items_product_uuid ON items (product_uuid);