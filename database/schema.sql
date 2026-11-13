-- =====================================================================
-- Streakly · Esquema de base de datos (PostgreSQL)
-- Generado desde las migraciones de EF Core:
--   dotnet ef migrations script -p src/Streakly.Infrastructure -s src/Streakly.Api -o ../database/schema.sql
--
-- Tablas: users, habits, habit_entries, goals, goal_tasks (+ historial de migraciones).
-- Restricciones: PK uuid, FK con ON DELETE CASCADE, indices unicos en
--   users.Email, habits(UserId, Name) y habit_entries(HabitId, EntryDate).
--
-- Uso: crear la base vacia "streakly" y ejecutar
--   psql -h localhost -U postgres -d streakly -f database/schema.sql
-- (No es necesario si la API arranca en Development: aplica las migraciones sola.)
-- =====================================================================

CREATE TABLE IF NOT EXISTS "__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
CREATE TABLE users (
    "Id" uuid NOT NULL,
    "Email" character varying(254) NOT NULL,
    "PasswordHash" character varying(100) NOT NULL,
    "CreatedAt" timestamp with time zone NOT NULL,
    CONSTRAINT "PK_users" PRIMARY KEY ("Id")
);

CREATE TABLE goals (
    "Id" uuid NOT NULL,
    "UserId" uuid NOT NULL,
    "Title" character varying(200) NOT NULL,
    "Description" character varying(1000),
    "DueDate" date,
    "CreatedAt" timestamp with time zone NOT NULL,
    CONSTRAINT "PK_goals" PRIMARY KEY ("Id"),
    CONSTRAINT "FK_goals_users_UserId" FOREIGN KEY ("UserId") REFERENCES users ("Id") ON DELETE CASCADE
);

CREATE TABLE habits (
    "Id" uuid NOT NULL,
    "UserId" uuid NOT NULL,
    "Name" character varying(60) NOT NULL,
    "Icon" character varying(30) NOT NULL,
    "Category" character varying(30) NOT NULL,
    "Type" character varying(20) NOT NULL,
    "TargetValue" numeric(10,2) NOT NULL,
    "Unit" character varying(30) NOT NULL,
    "ScheduledDays" integer NOT NULL,
    "ReminderTime" time without time zone,
    scale_minimum numeric(10,2),
    scale_good numeric(10,2),
    scale_excellent numeric(10,2),
    scale_maximum numeric(10,2),
    scale_unit character varying(30),
    "CreatedAt" timestamp with time zone NOT NULL,
    CONSTRAINT "PK_habits" PRIMARY KEY ("Id"),
    CONSTRAINT "FK_habits_users_UserId" FOREIGN KEY ("UserId") REFERENCES users ("Id") ON DELETE CASCADE
);

CREATE TABLE goal_tasks (
    "Id" uuid NOT NULL,
    "GoalId" uuid NOT NULL,
    "Title" character varying(200) NOT NULL,
    "IsCompleted" boolean NOT NULL,
    "Position" integer NOT NULL,
    CONSTRAINT "PK_goal_tasks" PRIMARY KEY ("Id"),
    CONSTRAINT "FK_goal_tasks_goals_GoalId" FOREIGN KEY ("GoalId") REFERENCES goals ("Id") ON DELETE CASCADE
);

CREATE TABLE habit_entries (
    "Id" uuid NOT NULL,
    "HabitId" uuid NOT NULL,
    "EntryDate" date NOT NULL,
    "Value" numeric(10,2) NOT NULL,
    CONSTRAINT "PK_habit_entries" PRIMARY KEY ("Id"),
    CONSTRAINT "FK_habit_entries_habits_HabitId" FOREIGN KEY ("HabitId") REFERENCES habits ("Id") ON DELETE CASCADE
);

CREATE INDEX "IX_goal_tasks_GoalId" ON goal_tasks ("GoalId");

CREATE INDEX "IX_goals_UserId" ON goals ("UserId");

CREATE UNIQUE INDEX "IX_habit_entries_HabitId_EntryDate" ON habit_entries ("HabitId", "EntryDate");

CREATE UNIQUE INDEX "IX_habits_UserId_Name" ON habits ("UserId", "Name");

CREATE UNIQUE INDEX "IX_users_Email" ON users ("Email");

INSERT INTO "__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261112030035_InitialCreate', '10.0.14');

COMMIT;

START TRANSACTION;
ALTER TABLE users ADD "LastActiveAt" timestamp with time zone;

ALTER TABLE users ADD "Role" character varying(20) NOT NULL DEFAULT 'User';

CREATE INDEX "IX_users_LastActiveAt" ON users ("LastActiveAt");

INSERT INTO "__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261113184530_AddUserRoleAndActivity', '10.0.14');

COMMIT;

