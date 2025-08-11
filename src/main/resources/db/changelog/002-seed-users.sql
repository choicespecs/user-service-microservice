INSERT INTO users (username, email, first_name, last_name, phone, deleted)
VALUES
  ('alice_liddell', 'alice@example.com', 'Alice', 'Liddell', '555-0100', false),
  ('bob_builder',   'bob@example.com',   'Bob',   'Builder', '555-0101', false)
ON CONFLICT (email) DO NOTHING;