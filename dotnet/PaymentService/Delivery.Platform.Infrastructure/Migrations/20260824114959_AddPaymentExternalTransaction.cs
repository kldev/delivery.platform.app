using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Delivery.Platform.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddPaymentExternalTransaction : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "payment_external_transactions",
                columns: table => new
                {
                    Id = table.Column<Guid>(type: "uuid", nullable: false),
                    PaymentId = table.Column<Guid>(type: "uuid", nullable: false),
                    ExternalTransactionId = table.Column<string>(type: "character varying(255)", maxLength: 255, nullable: false),
                    Provider = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    CreatedAt = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_payment_external_transactions", x => x.Id);
                    table.ForeignKey(
                        name: "FK_payment_external_transactions_payments_PaymentId",
                        column: x => x.PaymentId,
                        principalTable: "payments",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_payment_external_transactions_ExternalTransactionId",
                table: "payment_external_transactions",
                column: "ExternalTransactionId");

            migrationBuilder.CreateIndex(
                name: "IX_payment_external_transactions_PaymentId",
                table: "payment_external_transactions",
                column: "PaymentId");

            migrationBuilder.CreateIndex(
                name: "IX_payment_external_transactions_Provider_ExternalTransactionId",
                table: "payment_external_transactions",
                columns: new[] { "Provider", "ExternalTransactionId" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "payment_external_transactions");
        }
    }
}
