namespace App.Platform.Portal.E2E.Infrastructure;

/// <summary>
/// Writes screenshots to <c>screens/</c> next to the test output and, with <c>PORTAL_SCREENS_TO_DOCS=1</c>
/// (<c>scripts/screens.sh</c>), also to <c>docs/screens/portal/</c> at the repo root - the ones <c>docs/Portal.md</c>
/// shows.
/// </summary>
public sealed class Screens
{
    private readonly string _dir = Path.Combine(AppContext.BaseDirectory, "screens");
    private readonly string? _docsDir =
        Environment.GetEnvironmentVariable("PORTAL_SCREENS_TO_DOCS") == "1"
            ? Path.Combine(RepoRoot(), "docs", "screens", "portal")
            : null;

    public Screens()
    {
        Directory.CreateDirectory(_dir);
        if (_docsDir is not null)
            Directory.CreateDirectory(_docsDir);
    }

    /// <param name="keepToasts">false: close the confirmations first, so they do not cover the page.</param>
    public async Task ShotAsync(
        IPage page,
        string name,
        bool fullPage = true,
        bool keepToasts = false
    )
    {
        await page.WaitForLoadStateAsync(LoadState.NetworkIdle);
        // The confirmations sit over the right column; hidden for the picture only (they come and go by themselves).
        await page.EvaluateAsync(
            $"document.documentElement.classList.toggle('e2e-no-toasts', {(keepToasts ? "false" : "true")})"
        );
        await page.AddStyleTagAsync(
            new() { Content = ".e2e-no-toasts .toasts { visibility: hidden; }" }
        );
        // Let the arrival animations and toasts settle.
        await page.WaitForTimeoutAsync(400);
        var viewport = page.ViewportSize!;
        if (fullPage)
        {
            // A window as tall as the page instead of Playwright's stitching: the sticky rail stays where it belongs.
            await page.EvaluateAsync("window.scrollTo(0, 0)");
            var height = await page.EvaluateAsync<int>("document.documentElement.scrollHeight");
            await page.SetViewportSizeAsync(viewport.Width, Math.Max(viewport.Height, height));
            await page.WaitForTimeoutAsync(150);
        }
        var bytes = await page.ScreenshotAsync(new() { Type = ScreenshotType.Jpeg, Quality = 85 });
        await page.EvaluateAsync("document.documentElement.classList.remove('e2e-no-toasts')");
        if (fullPage)
            await page.SetViewportSizeAsync(viewport.Width, viewport.Height);
        await File.WriteAllBytesAsync(Path.Combine(_dir, $"{name}.jpg"), bytes);
        if (_docsDir is not null)
            await File.WriteAllBytesAsync(Path.Combine(_docsDir, $"{name}.jpg"), bytes);
    }

    private static string RepoRoot()
    {
        for (
            var dir = new DirectoryInfo(AppContext.BaseDirectory);
            dir is not null;
            dir = dir.Parent
        )
            if (File.Exists(Path.Combine(dir.FullName, "settings.gradle.kts")))
                return dir.FullName;
        throw new InvalidOperationException(
            "Repo root (settings.gradle.kts) not found above " + AppContext.BaseDirectory
        );
    }
}
