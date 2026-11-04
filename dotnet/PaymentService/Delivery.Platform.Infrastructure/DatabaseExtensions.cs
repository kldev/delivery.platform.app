using Delivery.Platform.Infrastructure.Persistence;
using Delivery.Platform.Infrastructure.Persistence.Adapter;
using Delivery.Platform.Infrastructure.Port;
using Delivery.Platform.PaymentService.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

namespace Delivery.Platform.Infrastructure;

public static class DatabaseExtensions
{
    public static IServiceCollection AddDatabase(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("Database")
                               ?? throw new InvalidOperationException(
                                   "Database connection string is not configured.");

        services.AddDbContext<PaymentDbContext>(options =>
            options.UseNpgsql(connectionString));
        
        services.AddScoped<IOutboxRepository, OutboxRepository>();
        
        return services;
    }
    public static async Task MigrateDatabaseAsync(
        this IHost app,
        CancellationToken cancellationToken = default)
    {
        using var scope = app.Services.CreateScope();

        var dbContext = scope.ServiceProvider
            .GetRequiredService<PaymentDbContext>();

        await dbContext.Database.MigrateAsync(cancellationToken);
    }
}