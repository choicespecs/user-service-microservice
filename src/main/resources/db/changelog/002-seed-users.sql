INSERT INTO users (email, first_name, last_name, phone, deleted)
VALUES
  ('alice@example.com', 'Alice', 'Liddell', '555-0100', false),
  ('bob@example.com',   'Bob',   'Builder', '555-0101', false)
ON CONFLICT (email) DO NOTHING;