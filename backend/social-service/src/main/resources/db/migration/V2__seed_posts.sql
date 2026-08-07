-- Una publicación por usuario sembrado.
--
-- Los author_id son EXACTAMENTE los mismos UUID que en authdb.V2__seed_users.sql.
-- Al no existir clave foránea entre bases, la correspondencia se mantiene a mano
-- con constantes literales. En un sistema real esto sería un evento UserRegistered
-- que este servicio consumiría para construir su propia proyección de autores.
INSERT INTO posts (id, author_id, author_alias, message, created_at, like_count) VALUES
  ('aaaaaaaa-0001-4000-8000-000000000001', '11111111-1111-4111-8111-111111111111', 'leo',
   'Arrancando con la arquitectura de microservicios. Dos servicios, dos bases, cero llamadas entre ellos.',
   now() - interval '5 hours', 0),

  ('aaaaaaaa-0002-4000-8000-000000000002', '22222222-2222-4222-8222-222222222222', 'mafe',
   'El truco de los likes en tiempo real no está en el WebSocket, está en reconciliar el update optimista.',
   now() - interval '4 hours', 0),

  ('aaaaaaaa-0003-4000-8000-000000000003', '33333333-3333-4333-8333-333333333333', 'carlos',
   'Hoy aprendí que un dominio rico se testea en milisegundos y un procedimiento almacenado no.',
   now() - interval '3 hours', 0),

  ('aaaaaaaa-0004-4000-8000-000000000004', '44444444-4444-4444-8444-444444444444', 'ana',
   'Compartir el store y los hooks entre web y móvil ahorra más tiempo que cualquier framework.',
   now() - interval '2 hours', 0),

  ('aaaaaaaa-0005-4000-8000-000000000005', '55555555-5555-4555-8555-555555555555', 'diego',
   'Un healthcheck de verdad vale más que diez sleeps en el docker-compose.',
   now() - interval '1 hour', 0);
