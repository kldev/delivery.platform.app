using Microsoft.Kiota.Abstractions;
using DeliveryProblem = App.Platform.Portal.ApiClients.Delivery.Models.ProblemDetail;
using LedgerProblem = App.Platform.Portal.ApiClients.Ledger.Models.ProblemDetail;
using ReconciliationProblem = App.Platform.Portal.ApiClients.Reconciliation.Models.ProblemDetail;
using SettlementProblem = App.Platform.Portal.ApiClients.Settlement.Models.ProblemDetail;

namespace App.Platform.Portal.Platform;

/// <summary>A failed call, as the portal tells it: what went wrong and, where it can, what to do.</summary>
public sealed record ApiFailure(string Message, IReadOnlyDictionary<string, string> Fields)
{
    public string? Field(string name) => Fields.TryGetValue(name, out var message) ? message : null;
}

/// <summary>
/// Turns an exception of a service call into a sentence for the person using the portal. The Spring services answer
/// with RFC 9457 problems that carry a stable "code" and, for invalid requests, "errors" per field.
/// </summary>
public static class ApiErrors
{
    private static readonly Dictionary<string, string> KnownCodes = new(StringComparer.Ordinal)
    {
        ["delivery.invalid-transition"] =
            "Tej zmiany nie da się zrobić w obecnym statusie dostawy. Odśwież stronę - ktoś mógł ją już zmienić.",
        ["driver.none-available"] = "Nie ma wolnego kierowcy. Dodaj kierowcę albo spróbuj później.",
        ["driver.not-found"] = "Nie ma takiego kierowcy.",
        ["driver.email-taken"] = "Kierowca z tym adresem e-mail już istnieje.",
        ["driver.phone-number-taken"] = "Kierowca z tym numerem telefonu już istnieje.",
        ["delivery.not-found"] = "Nie ma takiej dostawy - mogła zostać usunięta.",
        ["idempotency.in-progress"] = "To zlecenie jest właśnie zapisywane. Poczekaj chwilę.",
        ["idempotency.key-reused"] =
            "Ten formularz został już wysłany z innymi danymi. Otwórz go ponownie.",
    };

    public static ApiFailure Describe(Exception exception) =>
        exception switch
        {
            DeliveryProblem p => FromProblem(
                p.Code,
                p.Detail,
                p.Title,
                p.ResponseStatusCode,
                p.Errors?.Select(e => (e.Field, e.Message))
            ),
            SettlementProblem p => FromProblem(
                p.Code,
                p.Detail,
                p.Title,
                p.ResponseStatusCode,
                p.Errors?.Select(e => (e.Field, e.Message))
            ),
            LedgerProblem p => FromProblem(
                p.Code,
                p.Detail,
                p.Title,
                p.ResponseStatusCode,
                p.Errors?.Select(e => (e.Field, e.Message))
            ),
            ReconciliationProblem p => FromProblem(
                p.Code,
                p.Detail,
                p.Title,
                p.ResponseStatusCode,
                p.Errors?.Select(e => (e.Field, e.Message))
            ),
            ApiException { ResponseStatusCode: 404 } => Plain(
                "Nie znaleziono - rekord mógł zostać usunięty."
            ),
            ApiException { ResponseStatusCode: 409 } => Plain(
                "Stan rekordu zmienił się w międzyczasie. Odśwież stronę i spróbuj ponownie."
            ),
            ApiException { ResponseStatusCode: 429 } => Plain(
                "Za dużo zapytań naraz. Spróbuj za chwilę."
            ),
            ApiException { ResponseStatusCode: 502 or 503 or 504 } => Plain(
                "Serwis jest teraz niedostępny. Sprawdź, czy działa (docker compose ps)."
            ),
            ApiException { ResponseStatusCode: var status } => Plain(
                $"Serwis odpowiedział błędem {status}. Spróbuj ponownie."
            ),
            HttpRequestException => Plain(
                "Brak połączenia z bramą API. Sprawdź, czy stos działa (docker compose up -d)."
            ),
            TaskCanceledException or TimeoutException => Plain(
                "Serwis nie odpowiedział na czas. Spróbuj ponownie."
            ),
            _ => Plain("Coś poszło nie tak. Spróbuj ponownie."),
        };

    private static ApiFailure FromProblem(
        string? code,
        string? detail,
        string? title,
        int status,
        IEnumerable<(string? Field, string? Message)>? errors
    )
    {
        var fields = (errors ?? [])
            .Where(e => !string.IsNullOrWhiteSpace(e.Field))
            .GroupBy(e => e.Field!, StringComparer.Ordinal)
            .ToDictionary(
                g => g.Key,
                g => g.First().Message ?? "niepoprawna wartość",
                StringComparer.Ordinal
            );

        if (fields.Count > 0)
            return new ApiFailure("Popraw zaznaczone pola.", fields);
        if (code is not null && KnownCodes.TryGetValue(code, out var known))
            return Plain(known);

        var text = string.IsNullOrWhiteSpace(detail) ? title : detail;
        return Plain(
            string.IsNullOrWhiteSpace(text) ? $"Serwis odpowiedział błędem {status}." : text
        );
    }

    private static ApiFailure Plain(string message) =>
        new(message, new Dictionary<string, string>());
}
