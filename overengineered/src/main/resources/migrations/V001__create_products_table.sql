CREATE TABLE IF NOT EXISTS products (
  id INTEGER PRIMARY KEY,
  uuid TEXT NOT NULL,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  weight REAL NOT NULL,
  price INTEGER NOT NULL,
  created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_products_uuid ON products (uuid);
CREATE UNIQUE INDEX idx_products_name ON products (name);