namespace App.Platform.Portal.Platform;

public sealed record Toast(Guid Id, string Title, string? Text, bool IsError);

/// <summary>Short confirmations of what just happened ("Zlecenie potwierdzone"), one list per circuit.</summary>
public sealed class Toasts
{
    private readonly List<Toast> _items = [];

    public IReadOnlyList<Toast> Items => _items;

    public event Action? Changed;

    public void Success(string title, string? text = null) =>
        Add(new Toast(Guid.NewGuid(), title, text, IsError: false));

    public void Error(string title, string? text = null) =>
        Add(new Toast(Guid.NewGuid(), title, text, IsError: true));

    public void Dismiss(Guid id)
    {
        if (_items.RemoveAll(t => t.Id == id) > 0)
            Changed?.Invoke();
    }

    private void Add(Toast toast)
    {
        _items.Add(toast);
        if (_items.Count > 4)
            _items.RemoveAt(0);
        Changed?.Invoke();
    }
}
