-- Usuarios de demostración. Todos comparten la contraseña 'Periferia2026!'
-- (mismo hash BCrypt reutilizado: son datos de prueba, no de producción).
--
-- Estos UUID son constantes literales compartidas con socialdb: al no existir
-- clave foránea entre bases, la correspondencia se mantiene a mano. En un sistema
-- real esto sería un evento UserRegistered que social-service consumiría para
-- construir su propia proyección de autores.
INSERT INTO users (id, username, password_hash, first_name, last_name, birth_date, alias) VALUES
  ('11111111-1111-4111-8111-111111111111', 'leo',    '$2a$10$IAHhjBCuQ9usLiaV/w6yTOWNF3DVwVIIW0ZFYcEj3lAXsLjJmg70G', 'Leonel', 'Benítez',   '1993-04-12', 'leo'),
  ('22222222-2222-4222-8222-222222222222', 'mafe',   '$2a$10$IAHhjBCuQ9usLiaV/w6yTOWNF3DVwVIIW0ZFYcEj3lAXsLjJmg70G', 'María',  'Fernández', '1995-09-30', 'mafe'),
  ('33333333-3333-4333-8333-333333333333', 'carlos', '$2a$10$IAHhjBCuQ9usLiaV/w6yTOWNF3DVwVIIW0ZFYcEj3lAXsLjJmg70G', 'Carlos', 'Restrepo',  '1988-01-22', 'carlos'),
  ('44444444-4444-4444-8444-444444444444', 'ana',    '$2a$10$IAHhjBCuQ9usLiaV/w6yTOWNF3DVwVIIW0ZFYcEj3lAXsLjJmg70G', 'Ana',    'Gómez',     '1997-06-05', 'ana'),
  ('55555555-5555-4555-8555-555555555555', 'diego',  '$2a$10$IAHhjBCuQ9usLiaV/w6yTOWNF3DVwVIIW0ZFYcEj3lAXsLjJmg70G', 'Diego',  'Salazar',   '1991-11-17', 'diego');
