using Confluent.Kafka;

namespace Delivery.Platform.PaymentService.Publisher;

public static class MessagingExtensions
{
    public static IServiceCollection AddPublisher(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        services.AddSingleton<IProducer<string, string>>(_ =>
        {
            var bootstrapServers =
                configuration["Kafka:BootstrapServers"]
                ?? throw new InvalidOperationException(
                    "Kafka:BootstrapServers is not configured.");

            var producerConfig = new ProducerConfig
            {
                BootstrapServers = bootstrapServers,
                Acks = Acks.All,
                EnableIdempotence = true
            };

            return new ProducerBuilder<string, string>(
                    producerConfig)
                .Build();
        });

        services.AddHostedService<KafkaEventPublisher>();

        return services;
    }
}