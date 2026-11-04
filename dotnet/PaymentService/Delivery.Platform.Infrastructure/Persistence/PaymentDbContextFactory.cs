using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace Delivery.Platform.Infrastructure.Persistence;


public class PaymentDbContextFactory
    : IDesignTimeDbContextFactory<PaymentDbContext>
{
    public PaymentDbContext CreateDbContext(string[] args)
    {
        var optionsBuilder = new DbContextOptionsBuilder<PaymentDbContext>();

        optionsBuilder.UseNpgsql(
            "Host=localhost;Port=5432;Database=payments-design;Username=platform;Password=platform");

        return new PaymentDbContext(optionsBuilder.Options);
    }
}