-- =====================================================================
-- Streakly · Datos de demostración (PostgreSQL)
--
-- Uso (después de schema.sql o de arrancar la API, que aplica migraciones):
--   psql -h localhost -U postgres -d streakly -f database/seed.sql
--
-- Crea dos cuentas DE PRUEBA (no usar estas contraseñas en producción):
--   demo@streakly.test   / Demo12345!   -> rol User, con ~4 meses de historial
--   admin@streakly.test  / Admin12345!  -> rol Admin
--
-- Nota: el rol se sincroniza con Admin:Emails en cada login. Para que
-- admin@streakly.test siga siendo Admin, agrégalo a la configuración:
--   dotnet user-secrets set "Admin:Emails:0" "admin@streakly.test"
--
-- Las fechas son relativas a CURRENT_DATE, así que el heatmap y las rachas
-- siempre se ven "vivos". Hoy queda sin registrar para marcarlo en la demo.
-- Se puede ejecutar varias veces: primero borra las cuentas demo (cascada).
-- =====================================================================

BEGIN;

DELETE FROM users WHERE "Email" IN ('demo@streakly.test', 'admin@streakly.test');

-- ---------- Usuarios (contraseñas con hash BCrypt, factor 11) ----------
INSERT INTO users ("Id", "Email", "PasswordHash", "CreatedAt", "Role", "LastActiveAt") VALUES
  ('11111111-1111-1111-1111-111111111111', 'demo@streakly.test',
   '$2a$11$37MBu5.ktE6bVl74yfqOoebt8N5WB2fGHX6MG05uabwcT5TcC/ma2',
   now() - interval '130 days', 'User', now() - interval '1 day'),
  ('22222222-2222-2222-2222-222222222222', 'admin@streakly.test',
   '$2a$11$d56aOeFO.bjTVTB.0U0Wce1XyHyC5rGPALo3fVGZC7MiOrTlUv2Um',
   now() - interval '130 days', 'Admin', now() - interval '2 days');

-- ---------- Hábitos ----------
-- ScheduledDays es un bitmask: L=1, M=2, X=4, J=8, V=16, S=32, D=64
--   31 = lunes a viernes · 127 = todos los días
INSERT INTO habits ("Id", "UserId", "Name", "Icon", "Category", "Type", "TargetValue", "Unit", "ScheduledDays", "ReminderTime", "CreatedAt") VALUES
  ('a0000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Programar', 'code',  'Productivity', 'Boolean', 30, 'min',        31,  '07:00', now() - interval '125 days'),
  ('a0000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Agua',      'drop',  'Nutrition',    'Counter',  2, 'botellones', 127, NULL,    now() - interval '120 days'),
  ('a0000000-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Meditar',   'leaf',  'Mindfulness',  'Boolean',  5, 'min',        127, '21:30', now() - interval '60 days'),
  ('a0000000-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', 'Inglés',    'chat',  'Learning',     'Boolean', 15, 'min',        31,  NULL,    now() - interval '90 days'),
  ('a0000000-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', 'Leer',      'book',  'Learning',     'Boolean', 10, 'min',        127, NULL,    now() - interval '100 days');

-- ---------- Seguimientos diarios (habit_entries) ----------
-- Solo en días programados del hábito, hasta AYER. Los "huecos" (días sin
-- cumplir) siguen un patrón fijo para que las rachas se vean realistas.
WITH dias AS (
  SELECT h."Id" AS habit_id, h."Type" AS tipo, h."TargetValue" AS meta,
         d::date AS dia,
         (CURRENT_DATE - d::date) AS hace,
         row_number() OVER (PARTITION BY h."Id" ORDER BY d) AS n
  FROM habits h
  CROSS JOIN LATERAL generate_series(h."CreatedAt"::date, CURRENT_DATE - 1, interval '1 day') AS d
  WHERE h."UserId" = '11111111-1111-1111-1111-111111111111'
    AND ((h."ScheduledDays" >> (EXTRACT(ISODOW FROM d)::int - 1)) & 1) = 1
)
INSERT INTO habit_entries ("Id", "HabitId", "EntryDate", "Value")
SELECT gen_random_uuid(), habit_id, dia,
       CASE
         WHEN tipo = 'Counter' THEN
           CASE WHEN n % 9 = 0 THEN 1          -- día parcial (no cumple)
                WHEN n % 4 = 0 THEN 3          -- superó el mínimo
                ELSE meta END
         ELSE 1
       END
FROM dias
WHERE
  -- Programar: racha actual de ~4 semanas, fallas ocasionales antes
  (habit_id = 'a0000000-0000-0000-0000-000000000001' AND (hace <= 28 OR n % 11 <> 5))
  -- Agua: casi todos los días (los parciales se insertan con valor 1)
  OR (habit_id = 'a0000000-0000-0000-0000-000000000002' AND n % 13 <> 7)
  -- Meditar: racha corta actual (falló hace 5 días)
  OR (habit_id = 'a0000000-0000-0000-0000-000000000003' AND hace <> 5 AND n % 6 <> 0)
  -- Inglés: constante entre semana
  OR (habit_id = 'a0000000-0000-0000-0000-000000000004' AND n % 8 <> 3)
  -- Leer: más irregular
  OR (habit_id = 'a0000000-0000-0000-0000-000000000005' AND n % 3 <> 0);

-- ---------- Goals y metas ----------
INSERT INTO goals ("Id", "UserId", "Title", "Description", "DueDate", "CreatedAt") VALUES
  ('b0000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
   'Entregar la app Android', 'Tener la versión final funcionando con backend y base de datos.',
   CURRENT_DATE + 20, now() - interval '30 days'),
  ('b0000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
   'Dominar Jetpack Compose', 'Entender estado, navegación y listas sin copiar y pegar.',
   CURRENT_DATE + 60, now() - interval '15 days'),
  ('b0000000-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111',
   'Leer 3 libros', NULL,
   CURRENT_DATE - 3, now() - interval '90 days');

INSERT INTO goal_tasks ("Id", "GoalId", "Title", "IsCompleted", "Position") VALUES
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000001', 'Diseño de pantallas', true,  0),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000001', 'Backend con login',   true,  1),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000001', 'Hábitos y rachas',    true,  2),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000001', 'Goals con metas',     false, 3),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000001', 'Pruebas y entrega',   false, 4),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000002', 'Estado y recomposición', true,  0),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000002', 'Navigation Compose',     false, 1),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000002', 'LazyColumn y listas',    false, 2),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000002', 'Temas con Material 3',   false, 3),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000003', 'Hábitos atómicos',       true,  0),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000003', 'El programador pragmático', true, 1),
  (gen_random_uuid(), 'b0000000-0000-0000-0000-000000000003', 'Clean Code',             true,  2);

COMMIT;
