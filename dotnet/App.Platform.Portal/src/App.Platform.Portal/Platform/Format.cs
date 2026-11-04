using System.Globalization;

namespace App.Platform.Portal.Platform;

/// <summary>How the portal writes amounts, dates and identifiers (Polish conventions).</summary>
public static class Format
{
    private static readonly CultureInfo Pl = CultureInfo.GetCultureInfo("pl-PL");

    public static string Money(decimal? amount, string? currency) =>
        amount is null ? "-" : $"{amount.Value.ToString("N2", Pl)} {currency ?? ""}".TrimEnd();

    public static string Money(double? amount, string? currency) =>
        Money(amount is null ? null : (decimal)amount.Value, currency);

    public static string Number(double? value, int decimals = 1) =>
        value is null ? "-" : value.Value.ToString("N" + decimals, Pl);

    public static string Percent(double? value) =>
        value is null ? "-" : $"{value.Value.ToString("0.##", Pl)}%";

    /// <summary>The first 8 characters of an id: enough to tell rows apart, the full id is one click away.</summary>
    public static string ShortId(Guid? id) => id is null ? "-" : id.Value.ToString("N")[..8];

    public static string DateTime(DateTimeOffset? value, TimeZoneInfo zone) =>
        value is null
            ? "-"
            : TimeZoneInfo.ConvertTime(value.Value, zone).ToString("d MMM yyyy, HH:mm", Pl);

    public static string Time(DateTimeOffset? value, TimeZoneInfo zone) =>
        value is null ? "-" : TimeZoneInfo.ConvertTime(value.Value, zone).ToString("HH:mm:ss", Pl);

    /// <summary>"przed chwilą", "5 min temu", "3 godz. temu", then the date.</summary>
    public static string Ago(DateTimeOffset? value, DateTimeOffset now, TimeZoneInfo zone)
    {
        if (value is null)
            return "-";
        var age = now - value.Value;
        return age switch
        {
            { TotalSeconds: < 45 } => "przed chwilą",
            { TotalMinutes: < 60 } => $"{(int)age.TotalMinutes} min temu",
            { TotalHours: < 24 } => $"{(int)age.TotalHours} godz. temu",
            _ => DateTime(value, zone),
        };
    }

    /// <summary>"12 s", "4 min 5 s", "2 godz. 10 min" - how long a delivery stayed at a stop.</summary>
    public static string Duration(double? seconds)
    {
        if (seconds is null)
            return "-";
        var span = TimeSpan.FromSeconds(seconds.Value);
        return span switch
        {
            { TotalMinutes: < 1 } => $"{(int)span.TotalSeconds} s",
            { TotalHours: < 1 } => $"{span.Minutes} min {span.Seconds} s",
            { TotalDays: < 1 } => $"{(int)span.TotalHours} godz. {span.Minutes} min",
            _ => $"{(int)span.TotalDays} d {span.Hours} godz.",
        };
    }
}

/// <summary>The time zone of the portal's users, resolved once from <see cref="PortalOptions"/>.</summary>
public sealed class PortalClock(
    Microsoft.Extensions.Options.IOptions<PortalOptions> options,
    TimeProvider time
)
{
    public TimeZoneInfo Zone { get; } = TimeZoneInfo.FindSystemTimeZoneById(options.Value.TimeZone);

    public DateTimeOffset Now => time.GetUtcNow();
}
