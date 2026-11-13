# Streakly

**Hábitos pequeños, todos los días.** Streakly es un habit tracker: defines un *mínimo diario* para cada hábito
(p. ej. "Programar 30 min" o "Agua: 2 botellones") y, si lo cumples, el día suma a tu racha.
También permite crear **goals**: objetivos grandes divididos en metas que vas chuleando.

| Capa | Tecnología |
| --- | --- |
| Backend | .NET 10 · ASP.NET Core Web API · EF Core 10 · PostgreSQL · JWT · BCrypt |
| App móvil | Kotlin · Jetpack Compose · Material 3 · Navigation · Retrofit · DataStore |
| Tests | xUnit |

## Funcionalidades

- **Login / registro** con JWT (contraseñas con hash BCrypt).
- **Hoy**: hábitos programados para el día, progreso "3 de 7", racha global y check-in con un toque.
  Los hábitos tipo *contador* se suman con `−` / `+` hasta llegar al mínimo. Mantén presionado un hábito para **editarlo o eliminarlo**.
- **Nuevo hábito**: nombre, ícono, tipo (*hecho / no hecho* o *contador*), mínimo diario + unidad, días de la semana y recordatorio.
- **Rachas**: racha actual, mejor racha, % de cumplimiento, heatmap de las últimas 18 semanas y cumplimiento por hábito (30 días).
- **Goals**: tarjetas con fecha límite, días restantes, progreso y metas que se marcan / desmarcan. Buscador por texto, filtro *Todos / En curso / Completados* y edición (título, descripción, fecha, agregar o quitar metas).
- **Perfil**: resumen (racha global, mejor racha, hábitos, mínimos cumplidos), **preferencias** guardadas en el teléfono (*ocultar cumplidos*, *ordenar por nombre*) y cerrar sesión.
- **Admin** (solo lectura): los administradores ven desde Perfil → *Usuarios activos* quién usó la app en los últimos 7 días.

### Reglas de las rachas

- Solo cuentan los **días programados** del hábito; los demás no rompen la racha.
- Si hoy aún no cumples, la racha **no se rompe** (el día sigue pendiente).
- Un día programado sin cumplir (antes de hoy) reinicia la racha.
- **Racha global**: días seguidos en los que cumpliste al menos un hábito.

La lógica vive en `StreakCalculator` (capa Application) y está cubierta con tests.

## Arquitectura

```
streakly/
├── backend/                     Clean Architecture
│   ├── src/Streakly.Domain          Entidades y reglas de negocio (Habit, HabitEntry, Goal, GoalTask, User)
│   ├── src/Streakly.Application     DTOs, servicios (Auth, Habit, Goal), StreakCalculator, interfaces
│   ├── src/Streakly.Infrastructure  EF Core + PostgreSQL, repositorios, migraciones, JWT, BCrypt
│   ├── src/Streakly.Api             Controladores, autenticación, manejo global de errores
│   └── tests/Streakly.Tests         Tests unitarios (xUnit)
└── android/                     App Kotlin + Jetpack Compose (MVVM)
    └── app/src/main/java/com/streakly/app
        ├── data/        Retrofit, DTOs, DataStore (token), repositorios
        └── ui/          theme, components, auth, today, habit, streaks, goals, profile, navigation
```

## Cómo correrlo

### 1. Backend

Requisitos: .NET 10 SDK y PostgreSQL.

Los secretos **no** están en el repositorio. Configúralos con *user-secrets* (se guardan fuera del proyecto):

```bash
cd backend/src/Streakly.Api
dotnet user-secrets set "ConnectionStrings:DefaultConnection" "Host=localhost;Port=5432;Database=streakly;Username=postgres;Password=TU_PASSWORD"
dotnet user-secrets set "Jwt:Key" "una-clave-larga-y-aleatoria-de-al-menos-32-caracteres"
```

> También sirven variables de entorno: `ConnectionStrings__DefaultConnection` y `Jwt__Key`.

Crear la base y correr el API (en Development las migraciones se aplican solas al arrancar):

```bash
cd backend
dotnet run --project src/Streakly.Api --launch-profile http
```

El API queda en `http://localhost:5205`. En Development la documentación Swagger está en
`/swagger` (OpenAPI en `/openapi/v1.json`) y la salud en `/health`.
En `src/Streakly.Api/Streakly.Api.http` hay requests de ejemplo.

#### Usuarios administradores

El rol se asigna por correo desde la configuración y se sincroniza en cada login/registro
(quitar un correo de la lista le quita el rol en su siguiente inicio de sesión):

```bash
cd backend/src/Streakly.Api
dotnet user-secrets set "Admin:Emails:0" "tu-correo@ejemplo.com"
```

> Con variables de entorno: `Admin__Emails__0`. Si el usuario ya tenía sesión abierta, debe cerrarla y volver a entrar.

Tests:

```bash
cd backend
dotnet test
```

### 2. App Android

Requisitos: Android Studio (JDK 17+), emulador o teléfono con Android 8.0+.

1. Abre la carpeta `android/` en Android Studio.
2. Corre la app en el **emulador**: por defecto apunta a `http://10.0.2.2:5205/` (el localhost del PC).
3. En un **teléfono físico**, agrega a `android/local.properties` (no se sube al repo) la IP de tu PC en la red:

   ```properties
   streakly.apiUrl=http://192.168.X.X:5205/
   ```

   y corre el backend escuchando en la red: `dotnet run --project src/Streakly.Api --urls http://0.0.0.0:5205`.

El tráfico HTTP sin cifrar solo está permitido en el build *debug*.

## Base de datos: scripts

- `database/schema.sql`: esquema completo generado desde las migraciones de EF Core.
- `database/seed.sql`: datos de demostración con fechas relativas a hoy (≈4 meses de historial, rachas y goals).

```bash
psql -h localhost -U postgres -d streakly -f database/seed.sql
```

Cuentas **de prueba** que crea el seed: `demo@streakly.test` / `Demo12345!` (User) y `admin@streakly.test` / `Admin12345!` (Admin; agrégalo a `Admin:Emails` para que conserve el rol al iniciar sesión).

## Endpoints principales

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/auth/register` · `/api/auth/login` | Crea cuenta / inicia sesión → JWT |
| GET | `/api/auth/me` | Usuario actual |
| GET | `/api/today?date=YYYY-MM-DD` | Hábitos de hoy, progreso y racha global |
| GET · POST · PUT · DELETE | `/api/habits` · `/api/habits/{id}` | CRUD de hábitos (el tipo no se edita) |
| GET | `/api/habits?search=&type=` | Buscar por nombre y filtrar por tipo (`Boolean` / `Counter`) |
| PUT | `/api/habits/{id}/entries` | Registra el valor de un día (`{ date, value }`) |
| DELETE | `/api/habits/{id}/entries/{date}` | Borra el registro de un día |
| GET | `/api/habits/{id}/stats?weeks=18` | Rachas, cumplimiento y heatmap |
| GET | `/api/stats/compliance?days=30` · `/api/stats/summary` | Cumplimiento por hábito / resumen |
| GET · POST · PUT · DELETE | `/api/goals` · `/api/goals/{id}` | CRUD de goals |
| GET | `/api/goals?search=&status=` | Buscar en título/descripción y filtrar por estado (`All` / `Active` / `Completed`) |
| PUT · DELETE | `/api/goals/{id}/tasks/{taskId}` | Renombrar / quitar una meta |
| POST | `/api/goals/{id}/tasks` | Agrega una meta |
| PATCH | `/api/goals/{id}/tasks/{taskId}/toggle` | Marca / desmarca una meta |
| GET | `/api/admin/users/active?days=7` | **Solo Admin.** Usuarios con actividad en los últimos `days` días (1–365) |

Todas las rutas excepto registro y login requieren `Authorization: Bearer <token>`.
Un usuario cuenta como activo cuando inicia sesión o hace cualquier request autenticado (se registra como mucho cada 5 minutos).
La app envía su fecha local (`date`) para que "hoy" coincida con la zona horaria del usuario.

## Pendiente / ideas

- Notificaciones locales para el recordatorio (hoy se guarda la hora pero no se programa la alarma).
- Fuente pixel para los títulos (ver `ui/theme/Type.kt`).
