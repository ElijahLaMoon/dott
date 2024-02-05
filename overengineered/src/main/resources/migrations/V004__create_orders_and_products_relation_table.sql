CREATE TABLE IF NOT EXISTS orders_products (
  order_uuid INTEGER NOT NULL,
  product_uuid INTEGER NOT NULL,
  FOREIGN KEY(order_uuid) REFERENCES orders (uuid),
  FOREIGN KEY(product_uuid) REFERENCES products (uuid)
);

CREATE INDEX idx_orders_products_order_uuid
  ON orders_products (order_uuid, product_uuid)