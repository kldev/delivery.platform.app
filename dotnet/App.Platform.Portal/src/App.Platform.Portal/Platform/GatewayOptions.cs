using System.ComponentModel.DataAnnotations;

namespace App.Platform.Portal.Platform;

/// <summary>Where the portal reaches the platform: every service is called through the gateway (section "Gateway").</summary>
public sealed class GatewayOptions
{
    public const string Section = "Gateway";

    /// <summary>The gateway's root address, e.g. http://gateway:8080 in compose (env Gateway__BaseUrl).</summary>
    [Required, Url]
    public string BaseUrl { get; set; } = "";

    /// <summary>How long one call to a service may take before the page reports it (env Gateway__Timeout).</summary>
    [Range(typeof(TimeSpan), "00:00:01", "00:02:00")]
    public TimeSpan Timeout { get; set; } = TimeSpan.FromSeconds(10);
}

/// <summary>How the portal shows times (section "Portal").</summary>
public sealed class PortalOptions
{
    public const string Section = "Portal";

    /// <summary>IANA time zone of the people using the portal (env Portal__TimeZone).</summary>
    [Required]
    public string TimeZone { get; set; } = "Europe/Warsaw";

    /// <summary>How long a delivery page keeps asking the services for the effects of a change (env Portal__SyncWindow).</summary>
    [Range(typeof(TimeSpan), "00:00:05", "00:05:00")]
    public TimeSpan SyncWindow { get; set; } = TimeSpan.FromSeconds(45);

    /// <summary>The pause between two such questions (env Portal__SyncInterval).</summary>
    [Range(typeof(TimeSpan), "00:00:00.500", "00:00:30")]
    public TimeSpan SyncInterval { get; set; } = TimeSpan.FromMilliseconds(1500);
}
