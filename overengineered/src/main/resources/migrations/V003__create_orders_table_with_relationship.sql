CREATE TABLE orders (
  id INTEGER PRIMARY KEY,
  customer_name TEXT NOT NULL,
  customer_contact TEXT NOT NULL,
  shipping_address TEXT NOT NULL,
  grand_total INTEGER NOT NULL,
  created_at TEXT NOT NULL
);

CREATE TABLE orders_items (
  order_id INTEGER NOT NULL,
  item_id INTEGER NOT NULL,
  FOREIGN KEY(order_id) REFERENCES orders(id),
  FOREIGN KEY(item_id) REFERENCES items(id)
)