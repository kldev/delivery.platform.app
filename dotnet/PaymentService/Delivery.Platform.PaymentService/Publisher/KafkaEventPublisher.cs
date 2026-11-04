using System.Text;
using Confluent.Kafka;
using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Delivery.Platform.Infrastructure.Port;

namespace Delivery.Platform.PaymentService.Publisher;

public sealed class KafkaEventPublisher(
    IProducer<string, string> producer,
    IServiceScopeFactory factory,
    ILogger<KafkaEventPublisher> logger)
    : BackgroundService
{
    private const int BatchSize = 100;
    private static readonly TimeSpan LockDuration =
        TimeSpan.FromSeconds(30);

    protected override async Task ExecuteAsync(
        CancellationToken stoppingToken)
    {
        
        using var scope = factory.CreateScope();
        var outboxRepository = scope.ServiceProvider.GetRequiredService<IOutboxRepository>();
        
        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                var now = DateTimeOffset.UtcNow;
             
                var messages =
                    await outboxRepository.ClaimPending(
                        now,
                        now.Add(LockDuration),
                        BatchSize,
                        stoppingToken);

                if (messages.Count == 0)
                {
                    await Task.Delay(
                        TimeSpan.FromSeconds(5),
                        stoppingToken);

                    continue;
                }

                foreach (var message in messages)
                {
                    await Publish(
                        message,
                        outboxRepository,
                        stoppingToken);
                }
            }
            catch (OperationCanceledException)
                when (stoppingToken.IsCancellationRequested)
            {
                break;
            }
            catch (Exception exception)
            {
                logger.LogError(
                    exception,
                    "Error while processing outbox");
            }
        }
    }

    private async Task Publish(
        OutboxMessage message,
        IOutboxRepository outboxRepository,
        CancellationToken cancellationToken)
    {
        var topic = EventToTopic.Map(
            message.EventType,
            message.Module);

        try
        {
            var kafkaMessage = new Message<string, string>
            {
                Key = message.AggregateId.ToString(),
                Value = message.Payload,
                Headers = new Headers
                {
                    {
                        "event-version",
                        [.. "1"u8]
                    },
                }
            };

            await producer.ProduceAsync(
                topic,
                kafkaMessage,
                cancellationToken);

            await outboxRepository.MarkPublished(
                message.Id,
                DateTimeOffset.UtcNow,
                cancellationToken);
        }
        catch (Exception exception)
        {
            logger.LogError(
                exception,
                "Failed to publish outbox event {EventId}",
                message.EventId);

            var nextAttemptAt =
                DateTimeOffset.UtcNow.AddSeconds(
                    CalculateRetryDelay(message.Attempts));

            if (message.Attempts >= 3)
            {
                await outboxRepository.MarkDead(
                    message.Id,
                    exception.Message,
                    cancellationToken);

                return;
            }

            await outboxRepository.MarkFailed(
                message.Id,
                nextAttemptAt,
                exception.Message,
                cancellationToken);
        }
    }

    private static int CalculateRetryDelay(int attempts)
    {
        return Math.Min(
            60,
            (int)Math.Pow(2, attempts));
    }
}