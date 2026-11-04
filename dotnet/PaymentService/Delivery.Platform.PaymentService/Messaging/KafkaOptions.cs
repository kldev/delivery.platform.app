namespace Delivery.Platform.PaymentService.Messaging;

public sealed class KafkaOptions
{
    public const string SectionName = "Kafka";

    public required string BootstrapServers { get; init; }

    public required string GroupId { get; init; }

    public bool EnableAutoCommit { get; init; } = false;

    public string AutoOffsetReset { get; init; } = "Earliest";
}