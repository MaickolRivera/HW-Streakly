using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Streakly.Infrastructure.Persistence.Migrations
{
    /// <inheritdoc />
    public partial class AddUserRoleAndActivity : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<DateTime>(
                name: "LastActiveAt",
                table: "users",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Role",
                table: "users",
                type: "character varying(20)",
                maxLength: 20,
                nullable: false,
                defaultValue: "User");

            migrationBuilder.CreateIndex(
                name: "IX_users_LastActiveAt",
                table: "users",
                column: "LastActiveAt");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_users_LastActiveAt",
                table: "users");

            migrationBuilder.DropColumn(
                name: "LastActiveAt",
                table: "users");

            migrationBuilder.DropColumn(
                name: "Role",
                table: "users");
        }
    }
}
