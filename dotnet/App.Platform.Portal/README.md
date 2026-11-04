# App.Platform.Portal

Operations portal of the delivery platform: Blazor Web App (.NET 10, Interactive Server, no prerendering). It shows
where every delivery is on its route, moves it to the next stop, and follows what the other services did with it
(payment, driver, settlement, ledger, reconciliation). The UI is in Polish.

Every call goes through the gateway (YARP) with clients that **Kiota** generates from the gateway's OpenAPI documents:
the portal has no data types of its own for the services' data.

## Pages

| Page | What it is for |
|---|---|
| Pulpit `/` | the network: deliveries per stop of the route, pending payments, discrepancies, latest orders |
| Dostawy `/deliveries` | list with stage filters in the URL, search by id, paging (the services return slices) |
| Nowa dostawa `/deliveries/new` | create an order; one `Idempotency-Key` per opened form |
| Dostawa `/deliveries/{id}` | the route line (status history with times), the next action, cancel, and the trail in the other services that refreshes itself while a service is expected to react |
| Kierowcy, Płatności, Rozliczenia, Księga, Uzgodnienia, Powiadomienia | the other services' data; accept / decline a payment |

On a delivery's page, after an action the page asks the services again every `Portal:SyncInterval` for at most
`Portal:SyncWindow`, while some step of the trail is expected to change (e.g. after accepting the payment: the driver
assigned by the delivery service from `payment.paid`). Steps that changed play a short arrival; a service that does not
answer in the window is named on its step.

"Symuluj wpłatę od operatora" sends `POST /payments/external` - what the payment provider would send after collecting
the money. Locally there is no provider; without it the payment service never publishes `payment.completed`.

## Layout

```
App.Platform.Portal.slnx
src/App.Platform.Portal/               the Blazor host
  Platform/                            Kiota setup, options, error texts, lifecycle and trail rules, formatting
  Components/Layout, Components/Ui     shell, icons (inline Lucide), route line, toasts, dialog...
  Components/Pages                     one page per area
  wwwroot/app.css                      all styles, no build step
src/App.Platform.Portal.ApiClients/    Kiota output - never edited by hand
  openapi/<service>.json               the gateway's documents, committed
  prepare_spec.py                      adjustments before Kiota (see its docstring)
  generate.sh                          fetch + regenerate
tests/App.Platform.Portal.Tests        xUnit v3: lifecycle, trail, error texts, /health
tests/App.Platform.Portal.E2E          Playwright against the running stack; screenshots of docs/Portal.md
```

## Run

```bash
docker compose up -d                       # repo root: the stack, the gateway (5000) and the portal (5080)
```

From the IDE / CLI against the stack (gateway on localhost:5000):

```bash
dotnet run --project src/App.Platform.Portal   # http://localhost:5280
```

## Configuration

| Key (env) | Default | Meaning |
|---|---|---|
| `Gateway:BaseUrl` (`Gateway__BaseUrl`) | `http://localhost:5000` | the gateway's root; `http://gateway:8080` in compose |
| `Gateway:Timeout` | `00:00:10` | one call to a service |
| `Portal:TimeZone` | `Europe/Warsaw` | how times are shown |
| `Portal:SyncWindow` | `00:00:45` | how long a delivery page waits for the services after a change |
| `Portal:SyncInterval` | `00:00:01.500` | pause between two questions in that window |

Invalid values stop the start with the reason (options validated on start). `/health` is liveness only: the portal is
up even when a service is not - its pages say which one does not answer.

## Regenerating the clients

After a change of a service's API, with the stack running:

```bash
src/App.Platform.Portal.ApiClients/generate.sh             # fetch from the gateway, then regenerate
src/App.Platform.Portal.ApiClients/generate.sh --no-fetch  # from the committed documents only
```

Commit `openapi/*.json` and `Generated/` together. Kiota is a local tool (`.config/dotnet-tools.json`).

## Checks

```bash
dotnet build
dotnet test --project tests/App.Platform.Portal.Tests
dotnet test --project tests/App.Platform.Portal.E2E   # needs the stack: docker compose up -d --build
../../scripts/screens.sh                                # the e2e run that rewrites docs/screens/portal/
dotnet csharpier check .
docker build -t app-platform-portal:dev .
```
