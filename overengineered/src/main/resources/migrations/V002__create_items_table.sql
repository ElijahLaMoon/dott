CREATE TABLE items (
  id INTEGER PRIMARY KEY,
  product_id INTEGER NOT NULL,
  FOREIGN KEY(product_id) REFERENCES products(id),
  shipping_fee INTEGER NOT NULL,
  tax_amount INTEGER NOT NULL,
  cost INTEGER NOT NULL
)