INSERT INTO users (id, email, password_hash, name, role)
VALUES (
  uuid_generate_v4(),
  'owner@yourshop.in',
  -- bcrypt: password is 'ChangeMe123!'
  '$2a$10$7ZfpJHRzKkcKF4zuh5z2O.ydyqS4pMeEU0c8hLvXOa6XA7hJmXRB.',
  'Shop Owner',
  'ADMIN'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO categories (id, name, description)
VALUES
  (uuid_generate_v4(), 'Chocolate', 'All chocolate items'),
  (uuid_generate_v4(), 'Biscuits', 'Biscuits and cookies'),
  (uuid_generate_v4(), 'Snacks', 'Namkeen and snacks')
ON CONFLICT (name) DO NOTHING;
