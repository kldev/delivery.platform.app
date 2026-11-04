using Confluent.Kafka;

namespace Delivery.Platform.PaymentService.Messaging;

internal static class KafkaConsumerFactory
{
    public static IConsumer<string, string> Create(
        KafkaOptions options, ILogger logger)
    {
        var config = new ConsumerConfig
        {
            BootstrapServers = options.BootstrapServers,
            GroupId = options.GroupId,
            EnableAutoCommit = options.EnableAutoCommit,
            AutoOffsetReset =
                Enum.Parse<AutoOffsetReset>(
                    options.AutoOffsetReset,
                    ignoreCase: true),

            EnablePartitionEof = false,
        };

        return new ConsumerBuilder<string, string>(config)
            .SetLogHandler((_, message) =>
            {
                if (message.Level <= SyslogLevel.Error)
                {
                    logger.LogError(
                        "Kafka: {Message}",
                        message.Message);
                }
            })
            .Build();
    }
}