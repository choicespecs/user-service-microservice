INSERT INTO users (id, email, first_name, last_name, phone, deleted)
VALUES
  ('u_alice', 'alice@example.com', 'Alice', 'Liddell', '555-0100', false),
  ('u_bob', 'bob@example.com', 'Bob', 'Builder', '555-0101', false)
ON CONFLICT (email) DO NOTHING;