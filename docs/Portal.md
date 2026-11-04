# Operations portal: a tour in screenshots

Every screen on this page comes from the e2e run: the real portal (`dotnet/App.Platform.Portal`, Blazor Interactive
Server) on the docker compose stack, driven by Playwright in Chromium. Every click goes through the YARP gateway to the
services, and from service to service through Kafka. Nothing is mocked or retouched. The only change for the picture:
toasts are hidden while a shot is taken, except on the screen where the toast is the point.

- Tests: `dotnet/App.Platform.Portal/tests/App.Platform.Portal.E2E` → [`docs/screens/portal/`](screens/portal/).
  Every step asserts what the screen should show, so the tour is a test as well as a set of pictures.
- To take them again: `docker compose up -d --build`, then `scripts/screens.sh`. `PORTAL_URL` and `GATEWAY_URL`
  override `http://localhost:5080` and `http://localhost:5000`.

The UI is in Polish (*Dostawy* = deliveries, *Kierowcy* = drivers, *Płatności* = payments, *Rozliczenia* =
settlements, *Księga* = ledger, *Uzgodnienia* = reconciliations, *Powiadomienia* = notifications). The captions explain
each screen in English.

## Contents

- [Map of the portal](#map-of-the-portal)
- [Overview and the list](#overview-and-the-list)
- [One delivery's route](#one-deliverys-route)
- [Where the money goes](#where-the-money-goes)
- [When a delivery stops](#when-a-delivery-stops)
- [Drivers, payments, notifications](#drivers-payments-notifications)
- [On a phone](#on-a-phone)
- [How the delivery page keeps up](#how-the-delivery-page-keeps-up)
- [How the screenshots are made](#how-the-screenshots-are-made)
- [Found by the e2e run](#found-by-the-e2e-run)

## Map of the portal

```mermaid
flowchart LR
    subgraph portal["Portal :5080 (MainLayout: rail + pages)"]
        overview["/<br/>Pulpit · network"]
        list["/deliveries<br/>?status= &amp;q= &amp;page="]
        new["/deliveries/new"]
        detail["/deliveries/{id}<br/>route · next step · trail"]
        drivers["/drivers"]
        payments["/payments"]
        settlements["/settlements<br/>?deliveryId= ?driverId="]
        ledger["/ledger<br/>?referenceId="]
        recon["/reconciliations<br/>?deliveryId="]
        notif["/notifications"]
    end

    subgraph gw["YARP gateway :5000"]
        r1["/api/delivery/*"]
        r2["/api/payment/*"]
        r3["/api/settlement/*"]
        r4["/api/ledger/*"]
        r5["/api/reconciliation/*"]
        r6["/api/notification/*"]
    end

    overview --> list --> detail
    new --> detail
    detail -- "Szczegóły rozliczenia" --> settlements
    detail -- "Zapisy w księdze" --> ledger
    detail -- "Sprawa uzgodnienia" --> recon
    drivers -- "Rozliczenia" --> settlements

    overview & list & new & detail & drivers --> r1
    detail & payments --> r2
    detail & settlements --> r3
    detail & ledger --> r4
    detail & recon --> r5
    notif --> r6

    classDef page fill:#ecfdf5,stroke:#0f766e,color:#134e4a
    classDef route fill:#f1f5f9,stroke:#475569,color:#0f172a
    class overview,list,new,detail,drivers,payments,settlements,ledger,recon,notif page
    class r1,r2,r3,r4,r5,r6 route
```

The portal has no data types of its own for the services' data: every call goes through C# clients that Kiota
generates from the gateway's OpenAPI documents.

## Overview and the list

| | |
|---|---|
| <img src="screens/portal/01-overview.jpg" alt="Overview" /> | <img src="screens/portal/02-deliveries.jpg" alt="Deliveries" /> |
| **Pulpit** `/`: how many orders are at each stop of the route, cancellations, payments waiting for a decision, discrepancies, and the latest orders. | **Dostawy** `/deliveries`: every order with a mini route line, stage, distance and price. The services return slices (page + "has next"). |
| <img src="screens/portal/16-deliveries-delivered.jpg" alt="Deliveries filtered by stage" /> | <img src="screens/portal/03-new-delivery-validation.jpg" alt="New delivery form with errors" /> |
| **Stage filter** in the URL (`?status=DELIVERED`). The search takes a full id only; a partial one gets a hint instead of an empty list. | **Nowa dostawa**: fields checked before the call. One `Idempotency-Key` per opened form, so a double click creates one order. |

## One delivery's route

A 12.5 km order in Kraków, clicked from creation to reconciliation. The test also creates a driver of its own, so the
automatic assignment always has someone free.

```mermaid
sequenceDiagram
    autonumber
    actor D as Dispatcher (portal)
    participant DS as delivery-service
    participant K as Kafka
    participant PS as payment-service (C#)
    participant SS as settlement-service
    participant LS as ledger-service
    participant RS as reconciliation-service

    Note over D,DS: 04-delivery-created
    D->>DS: POST /deliveries (Idempotency-Key)
    D->>DS: POST /deliveries/{id}/confirm
    DS-)K: delivery.confirmed
    K-)PS: delivery.confirmed → payment Pending
    Note over D,PS: 05-payment-pending
    D->>PS: PUT /payments/{id}/accept
    PS-)K: payment.paid
    K-)DS: payment.paid → a free driver, ASSIGNED
    Note over D,DS: 06-driver-assigned
    D->>DS: POST …/pickup, POST …/start
    Note over D,DS: 07-in-transit
    D->>DS: POST …/complete
    DS-)K: delivery.completed
    K-)SS: delivery.completed → settlement
    SS-)K: settlement.created + driver.settlement.completed (one transaction)
    K-)LS: driver.settlement.completed → ledger transaction
    K-)RS: settlement.created → case PENDING
    Note over D,LS: 08-delivered-settled
    D->>PS: POST /payments/external ("Symuluj wpłatę od operatora")
    PS-)K: payment.completed
    K-)RS: payment.completed → RECONCILED (amounts match)
    Note over D,RS: 09-reconciled
```

| | |
|---|---|
| <img src="screens/portal/04-delivery-created.jpg" alt="Delivery created" /> | <img src="screens/portal/05-payment-pending.jpg" alt="Payment pending" /> |
| **Created**: the delivery service prices the order from the distance (10.00 PLN). The next step is confirmation; the toast is kept on this shot. | **Confirmed**: the payment service created the payment from `delivery.confirmed`, and the trail offers *Akceptuj* / *Odrzuć*. |
| <img src="screens/portal/06-driver-assigned.jpg" alt="Driver assigned" /> | <img src="screens/portal/07-in-transit.jpg" alt="In transit" /> |
| **Assigned**: after `payment.paid` the delivery service picked a free driver by itself. The next step: pickup. | **In transit**: picked up and on the way. Every stop on the route line has its time and how long it took. |
| <img src="screens/portal/08-delivered-settled.jpg" alt="Delivered and settled" /> | <img src="screens/portal/09-reconciled.jpg" alt="Reconciled" /> |
| **Delivered**: the settlement (70% of 10.00 PLN for the driver) and the ledger arrived on their own. *Symuluj wpłatę od operatora* stands in for the payment provider. | **Reconciled**: the provider's 10.00 PLN matches the delivery price, and every step of the trail is done. |

```mermaid
stateDiagram-v2
    direction LR
    [*] --> CREATED: POST /deliveries
    CREATED --> CONFIRMED: confirm
    CONFIRMED --> ASSIGNED: payment.paid (automatic) · assign-driver
    ASSIGNED --> PICKED_UP: pickup
    PICKED_UP --> IN_TRANSIT: start
    IN_TRANSIT --> DELIVERED: complete
    DELIVERED --> [*]

    CREATED --> CANCELLED: cancel
    ASSIGNED --> CANCELLED: cancel
    PICKED_UP --> CANCELLED: cancel
    IN_TRANSIT --> CANCELLED: cancel
    CONFIRMED --> CANCELLED: payment.declined
    CANCELLED --> [*]
```

The portal's *Anuluj dostawę* follows the same rule (`DeliveryStages.CanCancel` mirrors `Delivery.cancel()`): a
confirmed delivery cannot be cancelled by hand, only by a declined payment.

## Where the money goes

| | |
|---|---|
| <img src="screens/portal/10-settlement-of-the-delivery.jpg" alt="Settlement of the delivery" /> | <img src="screens/portal/11-ledger-transaction.jpg" alt="Ledger transaction" /> |
| **Rozliczenia** `?deliveryId=`: the driver's settlement of this delivery, with base and the night, weekend and distance additions. | **Księga** `?referenceId=`: the accounts with their balances, and the settlement as one transaction. The platform is debited 7.00 and the driver credited 7.00. |
| <img src="screens/portal/12-reconciliation-of-the-delivery.jpg" alt="Reconciliation of the delivery" /> | <img src="screens/portal/21-reconciliations.jpg" alt="All reconciliations" /> |
| **Uzgodnienia** `?deliveryId=`: the case of this delivery, expected and received. | **All cases**, with a status filter. The one in red: 10.00 received against 7.00 expected, a difference of 3.00 to explain. |

```mermaid
flowchart LR
    DC([delivery.completed]) --> S[settlement-service<br/>driver's share]
    S -- settlement.created --> R[reconciliation-service<br/>expected = delivery price]
    S -- driver.settlement.completed --> L[ledger-service<br/>debit platform · credit driver]
    X([POST /payments/external]) --> P[payment-service]
    P -- payment.completed --> R
    R --> ok{{RECONCILED}}
    R --> bad{{DISCREPANCY}}
    R -. "one side only" .-> pend{{PENDING}}
```

## When a delivery stops

| | |
|---|---|
| <img src="screens/portal/13-payment-declined.jpg" alt="Payment declined" /> | <img src="screens/portal/14-cancel-dialog.jpg" alt="Cancel dialog" /> |
| **Declined**: *Odrzuć* on the payment → `payment.declined` → the delivery service cancels the delivery at *Potwierdzona*. The page waits for that answer (see [below](#found-by-the-e2e-run)). | **Cancel**: an in-page dialog, never the browser's `confirm()`, which would block the Blazor circuit. |
| <img src="screens/portal/15-cancelled.jpg" alt="Cancelled" /> | <img src="screens/portal/23-delivery-not-found.jpg" alt="Delivery not found" /> |
| **Cancelled** at *Nowa*: the route line stops where the delivery was, and no action is left. | **Unknown id**: a 404 from the delivery service becomes *Nie ma takiej dostawy*, not an error. |

## Drivers, payments, notifications

| | |
|---|---|
| <img src="screens/portal/17-new-driver-form.jpg" alt="New driver form" /> | <img src="screens/portal/18-payments.jpg" alt="Payments" /> |
| **Kierowcy**: the form for a new driver. The delivery service refuses a phone number that is already taken, and the portal shows its reason. | **Płatności**: accept or decline straight from the list; the status filter is in the URL. |
| <img src="screens/portal/19-settlements.jpg" alt="Settlements" /> | <img src="screens/portal/22-notifications.jpg" alt="Notifications" /> |
| **Rozliczenia**: every driver's settlement with percentages and amounts. | **Powiadomienia**: what the notification service sent (EMAIL, SMS, PUSH; the senders are stubs that log), with attempts and errors. |

## On a phone

| | |
|---|---|
| <img src="screens/portal/24-phone-deliveries.jpg" width="360" alt="Deliveries on a phone" /> | <img src="screens/portal/25-phone-menu.jpg" width="360" alt="Menu on a phone" /> |
| **390 px**: the rail turns into a top bar, the filters wrap, and the table scrolls inside its own box. The test checks that the page never scrolls sideways. | **Menu**: the rail slides in; choosing a page closes it. |

## How the delivery page keeps up

The services answer through Kafka, a moment after the click. The delivery page does not ask the person to refresh.
After every action it asks the services again every `Portal:SyncInterval` (1.5 s), for at most `Portal:SyncWindow`
(45 s), while some step of the trail is still waiting for a service.

```mermaid
flowchart TD
    click([an action on the page]) --> window["sync window = now + 45 s"]
    window --> refresh["Refresh: delivery, status durations, payment, external tx,<br/>settlement, ledger, reconciliation - in parallel"]
    refresh --> trail["DeliveryTrail.Build(facts)"]
    trail --> waiting{"a step Waiting<br/>and window open?"}
    waiting -- yes --> tick["PeriodicTimer 1.5 s"] --> refresh
    waiting -- no --> idle([idle; a step that never answered<br/>names its service])
    refresh -. "a service does not answer" .-> stale["Nie odpowiada: … - last known data shown"]
```

## How the screenshots are made

```mermaid
flowchart LR
    sh["scripts/screens.sh"] --> build["dotnet build<br/>App.Platform.Portal.E2E"]
    build --> test["dotnet test<br/>PORTAL_SCREENS_TO_DOCS=1"]
    test --> pw["Playwright · Chromium<br/>1440×900 · 390×844 phone"]
    pw --> portal["portal :5080"] --> gateway["gateway :5000"] --> svc["7 services · Kafka · PostgreSQL"]
    test --> out["bin/…/screens/*.jpg"]
    test --> docs["docs/screens/portal/*.jpg"]
```

| Test class | What it walks through | Screens |
|---|---|---|
| `DeliveryJourneyTests` | the whole route with validation, payment, assignment, settlement, ledger and reconciliation | 03-12 |
| `StoppedDeliveryTests` | a declined payment; a cancel with its dialog | 13-15 |
| `PagesTests` | overview, list, stage filter, the money pages, notifications, the drivers' form, not found, the phone | 01-02, 16-25 |

The tests run one after another on one stack. They create their own drivers and orders, so they also work on a stack
that already has data. A full-page shot makes the window as tall as the page instead of stitching, so the sticky rail
stays where it belongs.

## Found by the e2e run

Writing the tour turned up two portal defects. Both are fixed together with the tests:

- **A declined payment never showed the cancellation.** The delivery service cancels the delivery a few seconds after
  `payment.declined`, but the trail had no step waiting for it. The page therefore stopped asking and stayed at
  *Potwierdzona*. Now the driver step (delivery-service) waits while the payment is declined and the delivery is still
  confirmed. Unit test: `DeliveryTrailTests.After_a_declined_payment_the_page_waits_for_the_delivery_service_to_cancel`.
- **On a phone the whole page scrolled sideways.** The shell's grid column was `1fr` (that is, `minmax(auto, 1fr)`),
  so a wide table widened the page instead of scrolling inside `.table-wrap`. Now it is `minmax(0, 1fr)`, and
  `PagesTests.On_a_phone_the_rail_folds_into_a_menu` checks it.

One more thing is visible on the notifications screen but not fixed here: every `payment.declined` notification ends up
*Nieudane* after 5 attempts with *Recipient could not be resolved*. A declined delivery never had a driver, and the
event carries no other recipient.
