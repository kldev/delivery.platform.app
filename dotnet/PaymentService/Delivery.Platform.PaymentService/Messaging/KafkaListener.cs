using System.Text.Json;
using Confluent.Kafka;
using Delivery.Platform.PaymentService.Exceptions;

namespace Delivery.Platform.PaymentService.Messaging;

public sealed class KafkaListener<TEvent>(
    IServiceScopeFactory scopeFactory,
    IConsumer<string, string> consumer,
    string topic,
    JsonSerializerOptions jsonOptions,
    ILogger<KafkaListener<TEvent>> logger)
    : BackgroundService
{
    protected override async Task ExecuteAsync(
        CancellationToken stoppingToken)
    {
        consumer.Subscribe(topic);

        logger.LogInformation(
            "Kafka listener {Listener} subscribed to topic {Topic}",
            typeof(TEvent).Name,
            topic);

        try
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                ConsumeResult<string, string> result;

                try
                {
                    result = consumer.Consume(stoppingToken);
                }
                catch (ConsumeException ex)
                {
                    logger.LogError(
                        ex,
                        "Kafka consume error for topic {Topic}",
                        topic);

                    continue;
                }

                TEvent @event;

                try
                {
                    @event =
                        JsonSerializer.Deserialize<TEvent>(
                            result.Message.Value,
                            jsonOptions)
                        ?? throw new JsonException(
                            $"Deserialized {typeof(TEvent).Name} is null.");
                }
                catch (JsonException ex)
                {
                    logger.LogError(
                        ex,
                        "Invalid Kafka message for {EventType}. " +
                        "Topic: {Topic}, Partition: {Partition}, Offset: {Offset}. " +
                        "Message will be skipped.",
                        typeof(TEvent).Name,
                        result.Topic,
                        result.Partition,
                        result.Offset);

                    /*
                     * Wiadomość jest niepoprawna i nie może zostać
                     * przetworzona przez handler.
                     *
                     * Commitujemy offset, aby poison message nie
                     * blokował dalszego konsumowania.
                     */
                    consumer.Commit(result);

                    continue;
                }

                try
                {
                    await using var scope =
                        scopeFactory.CreateAsyncScope();

                    var handler =
                        scope.ServiceProvider
                            .GetRequiredService<
                                IEventHandler<TEvent>>();

                    await handler.HandleAsync(
                        @event,
                        stoppingToken);

                    /*
                     * Commit dopiero po pomyślnym wykonaniu handlera.
                     */
                    consumer.Commit(result);

                    logger.LogDebug(
                        "Kafka message processed. " +
                        "EventType: {EventType}, " +
                        "Topic: {Topic}, Partition: {Partition}, Offset: {Offset}",
                        typeof(TEvent).Name,
                        result.Topic,
                        result.Partition,
                        result.Offset);
                }
                catch (OperationCanceledException)
                    when (stoppingToken.IsCancellationRequested)
                {
                    throw;
                }
                catch (ValidationException vex)
                {
                    logger.LogError(
                        vex,
                        "Error processing Kafka event {EventType}. " +
                        "Topic: {Topic}, Partition: {Partition}, Offset: {Offset}. " +
                        "Offset will be committed.",
                        typeof(TEvent).Name,
                        result.Topic,
                        result.Partition,
                        result.Offset);
                    
                    consumer.Commit(result);
                }
                catch (Exception ex)
                {
                    logger.LogError(
                        ex,
                        "Error processing Kafka event {EventType}. " +
                        "Topic: {Topic}, Partition: {Partition}, Offset: {Offset}. " +
                        "Offset will not be committed.",
                        typeof(TEvent).Name,
                        result.Topic,
                        result.Partition,
                        result.Offset);

                    /*
                     * Brak Commit().
                     *
                     * Event pozostaje niezatwierdzony.
                     *
                     * UWAGA:
                     * samo niezatwierdzenie offsetu nie powoduje natychmiastowego
                     * ponownego wywołania HandleAsync w tej samej pętli.
                     *
                     * Retry/DLQ powinno być rozwiązane osobną polityką.
                     */

                    throw;
                }
            }
        }
        catch (OperationCanceledException)
            when (stoppingToken.IsCancellationRequested)
        {
            logger.LogInformation(
                "Kafka listener {Listener} is stopping",
                typeof(TEvent).Name);
        }
        finally
        {
            consumer.Close();

            logger.LogInformation(
                "Kafka listener {Listener} stopped",
                typeof(TEvent).Name);
        }
    }
}