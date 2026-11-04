namespace App.Platform.Portal.E2E.Infrastructure;

[CollectionDefinition(Name)]
public sealed class PortalTests : ICollectionFixture<RunningPortal>
{
    public const string Name = "portal";
}
