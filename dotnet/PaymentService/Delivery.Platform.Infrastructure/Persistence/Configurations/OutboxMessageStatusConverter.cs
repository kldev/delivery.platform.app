using Delivery.Platform.Infrastructure.Persistence.Outbox;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;

namespace Delivery.Platform.Infrastructure.Persistence.Configurations;

public sealed class OutboxMessageStatusConverter() : ValueConverter<OutboxMessageStatus, string>(
    status => ConvertToDatabase(status),
    value => ConvertFromDatabase(value))
{
    private static string ConvertToDatabase(
        OutboxMessageStatus status)
    {
        return status switch
        {
            OutboxMessageStatus.Pending => "PENDING",
            OutboxMessageStatus.Published => "PUBLISHED",
            OutboxMessageStatus.Dead => "DEAD",
            _ => "UNKNOWN"
        };
    }

    private static OutboxMessageStatus ConvertFromDatabase(
        string value)
    {
        return value switch
        {
            "PENDING" => OutboxMessageStatus.Pending,
            "PUBLISHED" => OutboxMessageStatus.Published,
            "DEAD" => OutboxMessageStatus.Dead,
            _ => OutboxMessageStatus.Pending
        };
    }
}