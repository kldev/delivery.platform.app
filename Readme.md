# Delivery Platform

Event-driven microservices platform for managing deliveries, payments, external transactions and financial reconciliation.

The project demonstrates a distributed architecture built around **Kotlin (JVM 25), Spring Boot, Quarkus, C#, ASP.NET Core, YARP, Blazor, PostgreSQL and Apache Kafka**.

The operations portal in screenshots, with diagrams of the flows: **[`docs/Portal.md`](docs/Portal.md)** (screens in [`docs/screens/portal/`](docs/screens/portal/)).

The main goal of the project is to demonstrate how independent microservices can cooperate through REST APIs and asynchronous Kafka events while maintaining clear domain boundaries.

---

## Architecture

The platform consists of several independent services:

```text
                                  ┌─────────────────┐
                                  │   YARP Gateway  │
                                  │   ASP.NET Core  │
                                  └────────┬────────┘
                                           │
                                  HTTP / REST
                                           │
        ┌──────────────────────────┬───────┴───────┬──────────────────────────┐
        │                          │               │                          │
        ▼                          ▼               ▼                          ▼
 ┌─────────────┐            ┌─────────────┐ ┌─────────────┐            ┌─────────────┐
 │   Delivery  │            │   Payment   │ │   Ledger    │            │  Settlement │
 │   Service   │            │   Service   │ │   Service   │            │   Service   │
 │   Kotlin    │            │     C#      │ │   Kotlin    │            │   Kotlin    │
 │ Spring Boot │            │ ASP.NET Core│ │ Spring Boot │            │ Spring Boot │
 └──────┬──────┘            └──────┬──────┘ └──────┬──────┘            └──────┬──────┘
        │                          │               │                            │
        └──────────────────────────┴───────┬───────┴────────────────────────────┘
                                           │
                              ┌────────────▼────────────┐
                              │       PostgreSQL         │
                              │      Service Database    │
                              └──────────────────────────┘


                         ┌────────────────────────────────────┐
                         │             Kafka                  │
                         │        Event Backbone              │
                         └────────────────────────────────────┘

  Delivery ── delivery.confirmed ──► Payment ── payment.paid / payment.declined ──► Delivery
  Delivery ── delivery.completed ──► Settlement ── driver.settlement.completed ──► Ledger
                                     Settlement ── settlement.created ──────────► Reconciliation
  Payment  ── payment.completed (external transaction) ─────────────────────────► Reconciliation
  delivery / payment / settlement / driver topics ─────────────────────────────► Notification

 Also behind the gateway: Notification Service and Reporting Service (Kotlin, Quarkus).
 In front of the gateway: Operations Portal (Blazor, .NET).
```

Each service owns its domain and persistence model.

Communication is split into:

- *REST* — synchronous commands and queries
- *Kafka* — asynchronous domain/integration events
- *PostgreSQL* — persistent storage
- *YARP Gateway* — single HTTP entry point for client applications                         

---

## Services
### Delivery Service

Responsible for the delivery lifecycle.

A delivery goes through the following workflow:

```text

CREATED
   │
   ▼
CONFIRMED
   │
   ▼
ASSIGNED
   │
   ▼
PICKED_UP
   │
   ▼
IN_TRANSIT
   │
   ▼
DELIVERED
```

`CANCELLED` ends the route: by `/cancel` from CREATED, ASSIGNED, PICKED_UP or IN_TRANSIT, or from CONFIRMED when the
payment is declined (`payment.declined`). ASSIGNED is set automatically when `payment.paid` arrives (a free driver is
picked), or by hand with `/assign-driver`.

The service manages:

- deliveries
- drivers
- delivery state transitions
- delivery assignment
- pickup
- delivery start
- delivery completion

---

### Payment Service

Responsible for payment lifecycle and external transactions.

A payment is created from `delivery.confirmed` as `Pending`, then accepted (`Paid`, publishes `payment.paid`) or
declined (`Failed`, publishes `payment.declined`). An external transaction reported by a payment provider is stored
and publishes `payment.completed`.

Example external transaction:

```json
{
  "paymentId": "690c5ba8-02c7-4f46-80f2-df9fa089260a",
  "transactionId": "TXT-690c5ba8-02c7-4f46-80f2-df9fa089260a",
  "amount": 7.0000,
  "provider": "K6-TEST"
}
```

The payment service stores external transactions and publishes events used by the reconciliation service.

---

###  Ledger Service

The Ledger Service posts double-entry transactions (platform debited, driver credited) from driver settlements
(`driver.settlement.completed`).

It can be queried independently from the Payment and Delivery services.

---

### Reconciliation Service

The Reconciliation Service compares:

```text
Expected Amount  = delivery price, from the settlement      (settlement.created)
       │
       ▼
Actual Amount    = external transaction of the provider     (payment.completed)
```

and determines whether the payment is correctly settled.

Reconciliation states:
- PENDING - only one side is known yet
- RECONCILED
- DISCREPANCY
- MISSING_PAYMENT, MISSING_SETTLEMENT - defined, not set by any flow yet

Example of a successful reconciliation:

```json
{
  "expectedAmount": 7.0000,
  "actualAmount": 7.0000,
  "currency": "PLN",
  "status": "RECONCILED",
  "difference": 0.0000
}
```

Example of a discrepancy:

```json
{
  "expectedAmount": 7.0000,
  "actualAmount": 9.0000,
  "currency": "PLN",
  "status": "DISCREPANCY",
  "difference": 2.0000
}
```

This makes the reconciliation flow capable of detecting situations where an external payment provider reports an amount different from the expected payment amount.

---

### End-to-End Flow

The main business flow implemented by the project is:

```text
Create Delivery
      │
      ▼
Confirm Delivery
      │
      │ delivery.confirmed
      └──────────────► Payment created asynchronously (Pending)
                              │
                              ▼
                       Accept Payment
                              │ payment.paid
                              ▼
                       Delivery Assigned
                              │
                              ▼
                         Pickup
                              │
                              ▼
                      Start Delivery
                              │
                              ▼
                    Complete Delivery
                              │ delivery.completed
                              ▼
                     Driver Settlement ─── driver.settlement.completed ──► Ledger Entry
                              │
                              │ settlement.created            External Transaction
                              │                                       │ payment.completed
                              ▼                                       │
                       Reconciliation ◄───────────────────────────────┘
                              │
                 ┌────────────┴────────────┐
                 ▼                         ▼
            RECONCILED                DISCREPANCY
```

The important part of the architecture is that the services do not need to synchronously call each other for every state transition.

For example:

```text
Delivery Service
      │
      │ delivery.confirmed
      ▼
     Kafka
      │
      ▼
Payment Service
      │
      │ payment.paid (after acceptance)
      ▼
     Kafka
      │
      ▼
Delivery Service (assigns a driver), Notification Service
```

This allows individual services to remain independently deployable and scalable.

---

### Technology Stack

Backend
- Kotlin on JVM 25
- Spring Boot, Spring Data / JPA (delivery, settlement, ledger, reconciliation)
- Quarkus: Mutiny and the reactive PostgreSQL client (notification), REST and REST client (reporting)
- C#, .NET, ASP.NET Core, EF Core (payment), YARP (gateway), Blazor (portal)

All JVM services are written in Kotlin, while the Payment service, the gateway and the portal demonstrate the .NET/C# side of the platform.

---

### API Gateway

The project uses YARP (Yet Another Reverse Proxy) as the API Gateway.

The gateway is responsible for routing external requests to individual microservices.

Example:

```text
/api/delivery/*       → Delivery Service
/api/payment/*        → Payment Service
/api/settlement/*     → Settlement Service
/api/ledger/*         → Ledger Service
/api/reconciliation/* → Reconciliation Service
/api/notification/*   → Notification Service
/api/reporting/*      → Reporting Service
```

Each route strips its prefix and adds `/api`: `/api/delivery/deliveries` becomes `/api/deliveries` on the delivery service.
This gives clients a single entry point while keeping services internally independent.

---

### Operations Portal

`dotnet/App.Platform.Portal` - a Blazor (Interactive Server) portal for dispatchers and finance: deliveries on their
route, drivers, payments, settlements, ledger, reconciliations and notifications. It calls the services only through
the gateway, with C# clients that Kiota generates from the gateway's OpenAPI documents. In compose it runs at
http://localhost:5080 (`docker compose up -d portal`). Details: [dotnet/App.Platform.Portal/README.md](dotnet/App.Platform.Portal/README.md).
A tour in screenshots, taken by its Playwright e2e tests: [docs/Portal.md](docs/Portal.md).

---

### Messaging

**Apache Kafka** is used as the event backbone.

Kafka is responsible for asynchronous communication between services.

Typical events include concepts such as:

```text
delivery.created, delivery.confirmed, delivery.assigned, delivery.picked_up,
delivery.started, delivery.completed, delivery.cancelled
payment.paid, payment.declined, payment.completed
settlement.created, driver.settlement.completed
```

Topics (`infrastructure/kafka/init-topics.sh`): the events other services consume have their own topic
(`delivery.confirmed`, `delivery.completed`, `payment.paid`, `payment.declined`, `payment.completed`,
`settlement.created`, `driver.settlement.completed`); the rest go to `delivery.events`, `payment.events` and
`settlement.events`. Failed events of notification, reconciliation and settlement go to `<service>.dlq`.

The exact event contracts are owned by the corresponding domains.

---

### Database

**PostgreSQL** is used as the primary relational database.

Each service is designed around its own persistence boundary rather than sharing domain tables between services.

This allows the services to evolve their schemas independently.

---

### API Documentation

The APIs are documented using **OpenAPI** and exposed through **Scalar**: the gateway aggregates the documents of all
services at http://localhost:5000/docs.

Scalar provides an interactive API documentation experience for testing and exploring the REST endpoints.

---

### REST API

The gateway exposes endpoints grouped by bounded context.

#### Delivery

```http
GET    /api/delivery/drivers
POST   /api/delivery/drivers

GET    /api/delivery/deliveries
POST   /api/delivery/deliveries

GET    /api/delivery/deliveries/{id}
GET    /api/delivery/deliveries/status-durations
GET    /api/delivery/drivers/{id}

POST   /api/delivery/deliveries/{id}/confirm
POST   /api/delivery/deliveries/{id}/assign-driver
POST   /api/delivery/deliveries/{id}/pickup
POST   /api/delivery/deliveries/{id}/start
POST   /api/delivery/deliveries/{id}/complete
POST   /api/delivery/deliveries/{id}/cancel
```

#### Payment

```http
GET  /api/payment/payments
PUT  /api/payment/payments/{id}/accept
PUT  /api/payment/payments/{id}/decline

GET  /api/payment/payments/external
POST /api/payment/payments/external
```

#### Ledger

```http
GET /api/ledger/ledger-accounts
GET /api/ledger/ledger-accounts/{id}
GET /api/ledger/ledger-entries
GET /api/ledger/ledger-transactions
```

#### Reconciliation

```http
GET /api/reconciliation/reconciliations
GET /api/reconciliation/reconciliations/{id}
```

#### Settlement

```http
GET /api/settlement/settlements
GET /api/settlement/settlements/{id}
```

#### Notification

```http
GET  /api/notification/notifications
GET  /api/notification/notifications/{id}
POST /api/notification/notifications
```

---

### Reconciliation

The reconciliation process compares the expected payment amount with the amount reported by an external payment provider.

**Matching transaction**

```text
Expected: 7.00 PLN
Actual:   7.00 PLN

Difference: 0.00 PLN

Result:
RECONCILED
```

**Payment discrepancy**

```text
Expected: 7.00 PLN
Actual:   9.00 PLN

Difference: 2.00 PLN

Result:
DISCREPANCY
```

Example response:

```json
{
  "id": "045fd60e-f6cc-40d9-af06-6d20e07066fd",
  "deliveryId": "8bd6afc7-c6ac-4c01-823b-64752244c5f7",
  "settlementId": "992a4b11-0f61-48e1-aa63-2f0939bf9b9c",
  "paymentId": "16cd75aa-5136-46e6-a48d-4453ffd88e6d",
  "externalTransactionId": "TXT-16cd75aa-5136-46e6-a48d-4453ffd88e6d",
  "expectedAmount": 7.0000,
  "actualAmount": 9.0000,
  "currency": "PLN",
  "status": "DISCREPANCY",
  "difference": 2.0000,
  "createdAt": "2026-08-24T16:28:52.578363Z",
  "reconciledAt": "2026-08-24T16:28:53.655591Z"
}
```

---

### Load Testing

The project includes an end-to-end k6 scenario testing the complete business flow.

The test executes:

```text
Create Delivery
      ↓
Confirm Delivery
      ↓
Wait for Payment
      ↓
Accept Payment
      ↓
Wait for Assignment
      ↓
Pickup
      ↓
Start
      ↓
Complete
      ↓
Get Ledger Entries
      ↓
Submit External Transaction
      ↓
Get Reconciliations
```

The current scenario uses:

```javascript
{
    executor: 'shared-iterations',
    vus: 50,
    iterations: 50,
    maxDuration: '2m'
}
```

This means the test executes **50 complete business flows using up to 50 virtual users**.

The test also verifies asynchronous processing by polling for:

- payment creation
- delivery assignment
- delivery completion

Ledger entries and reconciliations are read once, with a check of the HTTP status only.

---

### Load Test Scenarios

The external transaction amount is intentionally randomized:

```javascript
amount: Math.random() < 0.5
    ? 7.0000
    : 9.000
```

The expected amount is the delivery price, which the delivery service sets from the distance (up to 10 km: 5,
up to 50 km: 10, up to 100 km: 20, more: 50). k6 uses 20-120 km and a random currency (PLN, EUR or GBP), so the price
is 10, 20 or 50 and every k6 flow currently ends in a discrepancy:

```text
Expected = 10 / 20 / 50
Actual   = 7 or 9
           ↓
       DISCREPANCY
```

A matching amount (RECONCILED) is covered by the portal's e2e test, which sends the payment's own amount
([docs/Portal.md](docs/Portal.md#one-deliverys-route)).

---

### Testing Strategy

The project combines several levels of testing:

```text
                 ┌──────────────────┐
                 │   Unit Tests     │
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │ Integration Tests│
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │ REST API Tests   │
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │  Kafka / Events  │
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │   k6 E2E Load    │
                 │      Tests       │
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │ Portal E2E       │
                 │ (Playwright)     │
                 └──────────────────┘
```

The Kafka / event tests exercise the listeners and the JSON contracts without a broker. Konsist architecture tests
check the layering of the Kotlin services. The portal's Playwright tests click through the running stack and produce the
screenshots of [docs/Portal.md](docs/Portal.md).

The k6 test is particularly useful for validating the complete distributed flow rather than testing individual endpoints in isolation.

---

### Running Locally

The platform is designed to run locally using Docker Compose.

Infrastructure includes:

```text
PostgreSQL     localhost:5632
Kafka          localhost:9092
Kafka UI       http://localhost:8080
Microservices  delivery, payment, settlement, ledger, reconciliation, notification, reporting
YARP Gateway   http://localhost:5000  (Scalar: /docs)
Portal         http://localhost:5080
```

```bash
docker compose up -d --build
```

After starting the environment, the gateway can be used as the main entry point for API requests.

---

### Design Goals

The project focuses on practical microservice architecture rather than simply splitting a monolith into multiple applications.

Key goals include:

- clear service boundaries
- domain-oriented architecture
- asynchronous communication
- eventual consistency
- independent persistence
- REST APIs for synchronous operations
- Kafka events for integration
- resilient asynchronous workflows
- end-to-end business flow testing
- load testing of distributed workflows
- detection of financial discrepancies
- independent scalability of services

---

### Architecture Highlights

The project demonstrates several real-world distributed system problems.

**Eventual consistency**

After confirming a delivery, the payment may not exist immediately.

The client/test therefore waits until the corresponding asynchronous event has been processed.

```text
Confirm Delivery
      │
      ▼
   Kafka (delivery.confirmed)
      │
      ▼
Payment Created (no event - the client polls GET /payments)
```

**Distributed workflow**

Completing a delivery triggers additional processing in other services.

```text
Delivery (delivery.completed)
   │
   └──► Settlement
           │
           ├──► Ledger          (driver.settlement.completed)
           │
           └──► Reconciliation  (settlement.created)
```

## Outbox Pattern

The system uses the **Transactional Outbox Pattern** to reliably publish domain events to Kafka.

### Problem

Without the Outbox Pattern, updating business data and publishing an event to Kafka would be two independent operations:

```text
Database transaction
        |
        +---- save business data
        |
        +---- publish event to Kafka
```

This can lead to inconsistencies:

* business data is committed, but publishing to Kafka fails,
* the event is published, but the database transaction is rolled back,
* the application crashes between the database update and event publication.

The Outbox Pattern solves this by storing the event in the database as part of the **same transaction** as the business operation.

### Architecture

```text
                    Database
                 ┌─────────────┐
                 │ Business    │
                 │ tables      │
                 │             │
                 │ outbox_     │
                 │ messages    │
                 └──────┬──────┘
                        │
                 scheduled worker
                        │
                        ▼
                 ┌─────────────┐
                 │ Outbox      │
                 │ Publisher   │
                 └──────┬──────┘
                        │
                        ▼
                    ┌───────┐
                    │ Kafka │
                    └───────┘
```

During a business operation, the domain event is stored in `outbox_messages` within the same database transaction:

```text
BEGIN

    UPDATE payment ...

    INSERT INTO outbox_messages (...)

COMMIT
```

If the transaction is rolled back, both the business change and the event are rolled back.

If the transaction succeeds, the event remains in the Outbox and can be published asynchronously by the Outbox Publisher.

---

## Outbox Message Lifecycle

Each Outbox message has a status describing its current state:

```text
PENDING
   │
   │ publish succeeded
   ▼
PUBLISHED

PENDING
   │
   │ publish failed
   ▼
PENDING
   │
   │ maximum attempts reached
   ▼
DEAD
```

An Outbox message typically contains:

* `id` — unique identifier of the Outbox message,
* `event_id` — identifier of the domain event,
* `event_type` — event type,
* `aggregate_id` — identifier of the affected aggregate,
* `payload` — serialized event payload,
* `status` — current message status,
* `attempts` — number of publication attempts,
* `next_attempt_at` — earliest time when the next attempt can be made,
* `locked_until` — expiration time of the worker lease,
* `created_at` — message creation timestamp,
* `published_at` — successful publication timestamp.

---

## Claiming Messages with `FOR UPDATE SKIP LOCKED`

The Outbox Publisher does not simply select pending messages:

```sql
SELECT *
FROM outbox_messages
WHERE status = 'PENDING'
LIMIT 50;
```

With multiple application instances, this could cause several publishers to select the same messages.

Instead, messages are claimed using:

```sql
FOR UPDATE SKIP LOCKED
```

The implementation uses a single PostgreSQL query:

```sql
UPDATE outbox_messages
SET locked_until = :lockedUntil
WHERE id IN (
    SELECT id
    FROM outbox_messages
    WHERE status = 'PENDING'
      AND next_attempt_at <= :now
      AND (
          locked_until IS NULL
          OR locked_until < :now
      )
    ORDER BY created_at
    FOR UPDATE SKIP LOCKED
    LIMIT :limit
)
RETURNING *;
```

### How `SKIP LOCKED` works

Assume that two application instances are running the Outbox Publisher:

```text
                 outbox_messages
                ┌─────┬─────────┐
                │ ID  │ STATUS  │
                ├─────┼─────────┤
                │ A   │ PENDING │
                │ B   │ PENDING │
                │ C   │ PENDING │
                │ D   │ PENDING │
                └─────┴─────────┘

          ┌──────────────┐
          │ Publisher #1 │
          └──────┬───────┘
                 │
             locks A, B
                 │
                 ▼

          ┌──────────────┐
          │ Publisher #2 │
          └──────┬───────┘
                 │
          SKIP A, B
                 │
             claims C, D
                 ▼
```

The first publisher locks rows using `FOR UPDATE`.

When the second publisher encounters already locked rows, it **does not wait** for those locks to be released. `SKIP LOCKED` causes it to skip those rows and claim other available messages.

This allows multiple application instances to process the Outbox concurrently:

```text
Publisher #1 ──► A B C
Publisher #2 ──► D E F
Publisher #3 ──► G H I
```

without requiring a separate distributed lock.

---

## Application-Level Lease

`FOR UPDATE SKIP LOCKED` protects the claim operation while the database transaction is active.

However, the SQL row lock is released when the transaction ends.

For this reason, the Outbox also uses:

```text
locked_until
```

This represents an **application-level lease**.

When a message is claimed:

```text
locked_until = now + lease duration
```

Another publisher can only claim the message after the lease expires:

```sql
locked_until IS NULL
OR locked_until < :now
```

This is useful when:

* multiple application instances are running,
* publishing takes longer than expected,
* a worker crashes,
* the application is restarted,
* Kafka becomes temporarily unavailable.

If a worker crashes after claiming a message, the lease eventually expires and another worker can claim the message again.

---

## Publishing to Kafka

After claiming the messages, `OutboxPublisher` passes each message to the `EventPublisher`.

The Kafka implementation uses `KafkaTemplate` and `ProducerRecord`:

```text
OutboxMessage
     │
     ▼
KafkaEventPublisher
     │
     ▼
ProducerRecord
     │
     ▼
Kafka
```

The record key is the `aggregateId`. The event carries its metadata in the JSON payload, and the version in a Kafka
header:

```text
header   event-version: 1
payload  eventId, eventType, aggregateId, module, occurredAt, ...
```

For example:

```text
eventId:     8d7...
eventType:   payment.completed
occurredAt:  2026-08-24T16:30:00Z
```

The `eventId` allows consumers to uniquely identify a particular event and can be used for idempotency and deduplication.

---

## Retry and Exponential Backoff

A failed publication is not retried immediately.

After a failure, the message is updated with:

```text
attempts++
next_attempt_at = now + backoff
last_error = exception
```

The publisher uses exponential backoff with a maximum delay.

The current implementation starts with a 5-second base delay:

```text
failure 1 → retry after 10 seconds
failure 2 → retry after 20 seconds
failure 3 → DEAD (MAX_ATTEMPTS = 3)
```

The delay is capped by `MAX_BACKOFF_SECONDS` (300).

This is the Kotlin outbox of the delivery and settlement services. The Payment service (C#) has an equivalent
`BackgroundService`: batch 100, a 30 s lease, `2^attempts` seconds of backoff capped at 60 s, 3 attempts.

This prevents the application from continuously retrying failed Kafka operations in a tight loop.

---

## Dead Messages

After reaching the maximum number of attempts, the message is marked as:

```text
DEAD
```

The lifecycle becomes:

```text
PENDING
   │
   ├── attempt 1 ──► failed
   │
   ├── attempt 2 ──► failed
   │
   └── attempt 3 ──► DEAD
```

The message remains in the database together with information about the failure:

```text
attempts
last_error
```

This makes failed events available for investigation and potential manual recovery.

---

## Scheduled Publisher

Publishing is performed periodically using Spring Scheduler:

```kotlin
@Scheduled(
    fixedDelayString = "\${delivery.outbox.publish-interval-ms:1000}"
)
fun publishPending()
```

Messages are processed in batches:

```text
BATCH_SIZE = 50
```

The publisher first claims a batch and then processes each message independently:

```text
              Outbox Publisher
                     │
              claim max 50
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
      Event A      Event B      Event C
        │            │            │
      Kafka        Kafka        Kafka
        │            │            │
        ▼            ▼            ▼
   PUBLISHED     PUBLISHED   PENDING (retry later)
```

---

## Multiple Application Instances

The Outbox implementation is designed to work with multiple application instances:

```text
                  PostgreSQL
                      │
             outbox_messages
                      │
       ┌──────────────┼──────────────┐
       │              │              │
       ▼              ▼              ▼
 Publisher #1    Publisher #2    Publisher #3
       │              │              │
       └──────────────┼──────────────┘
                      ▼
                    Kafka
```

`FOR UPDATE SKIP LOCKED` allows the database table to act as a concurrent work queue.

Each publisher can claim its own set of messages without waiting for messages currently being processed by another instance.

---

## At-Least-Once Delivery

The Outbox Pattern provides **at-least-once delivery**, not exactly-once delivery.

For example, the following situation is possible:

```text
1. Worker claims the event
2. Worker publishes the event to Kafka
3. Kafka accepts the event
4. Worker fails before marking the event as PUBLISHED
5. Worker is restarted
6. The event becomes available again
7. The event is published again
```

As a result, consumers must be **idempotent**.

The `event-id` can be used as a deduplication key:

```text
event-id
    │
    ▼
consumer inbox / processed_events
    │
    ├── already processed → ignore
    │
    └── new event → process
```

The Outbox guarantees that an event is not lost between the business transaction and the broker publication. Handling potential duplicates is the responsibility of the consumer.

---

## Summary

The implementation combines several mechanisms:

| Mechanism            | Purpose                                           |
| -------------------- | ------------------------------------------------- |
| Transactional Outbox | Atomically persist business data and events       |
| `FOR UPDATE`         | Safely claim messages                             |
| `SKIP LOCKED`        | Allow concurrent processing by multiple instances |
| `locked_until`       | Application-level lease for claimed messages      |
| Batch processing     | Limit the number of messages processed at once    |
| Exponential backoff  | Handle temporary failures                         |
| `DEAD` status        | Isolate messages that cannot be published         |
| Kafka headers        | Transport event metadata                          |
| `event-id`           | Identify and deduplicate events                   |
| Idempotent consumers | Safely handle at-least-once delivery              |

This approach provides reliable event delivery while allowing the Outbox Publisher to scale horizontally across multiple application instances.


## Idempotent Event Consumption

Because the Outbox Pattern provides **at-least-once delivery**, the same event may occasionally be delivered to a consumer more than once.

To prevent duplicate processing, consumers use a `processed_events` table to keep track of events that have already been successfully processed.

```sql
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);
```

The `event_id` is the unique identifier of the event and is used as the primary key.

### Consumer Flow

Before processing an incoming event, the consumer checks whether its `event_id` already exists in `processed_events`:

```text
                     Kafka Event
                          │
                          ▼
                ┌──────────────────┐
                │ Check event_id   │
                │ in processed_    │
                │ events           │
                └────────┬─────────┘
                         │
              ┌──────────┴──────────┐
              │                     │
        event exists           event not found
              │                     │
              ▼                     ▼
        Ignore event          Process event
                                      │
                                      ▼
                             Insert event_id
                             into processed_events
```

If the event already exists, the consumer treats it as a duplicate and does not execute the business operation again.

If the event does not exist, the consumer processes the event and records its `event_id` in `processed_events`.

### Example

The first delivery of an event:

```text
event-id = 8d7...
```

The consumer does not find the ID in `processed_events`:

```text
processed_events
┌───────────┬────────────┬─────────────────────┐
│ event_id  │ event_type │ processed_at        │
├───────────┼────────────┼─────────────────────┤
│           │            │                     │
└───────────┴────────────┴─────────────────────┘
```

The event is processed and recorded:

```sql
INSERT INTO processed_events (
    event_id,
    event_type,
    processed_at
)
VALUES (
    :eventId,
    :eventType,
    :processedAt
);
```

If Kafka delivers the same event again:

```text
event-id = 8d7...
```

the consumer finds the existing record:

```text
event_id already exists
        │
        ▼
   skip processing
```

This prevents the business operation from being executed twice.

### Why `event_id` is the Primary Key

Using `event_id` as the primary key provides database-level protection against duplicate registrations of the same event.

Even if multiple consumer executions race with each other, the database guarantees that only one record for a given `event_id` can exist.

```text
event_id = 8d7...
     │
     ├── Consumer #1 ──► INSERT ──► success
     │
     └── Consumer #2 ──► INSERT ──► duplicate / already processed
```

The `processed_events` table therefore acts as a lightweight **Inbox / Processed Events** mechanism on the consumer side.

Combined with the Outbox Pattern, the overall messaging model is:

```text
┌──────────────┐
│ Business     │
│ Transaction  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Outbox       │
│ Pattern      │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│    Kafka     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Consumer     │
│              │
│ Check        │
│ event_id     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ processed_   │
│ events       │
└──────────────┘
```

This gives the system reliable **at-least-once delivery with idempotent processing**.

> **Important:** The business operation and the `processed_events` insert should be performed within the same database transaction. This prevents the consumer from marking an event as processed when the actual business operation has failed.
# Notification Service

The **Notification Service** is responsible for delivering user-facing notifications generated by events occurring across the platform. It consumes asynchronous events from Kafka, determines the appropriate recipient and delivery channel, and creates notifications without coupling the business services directly to the notification mechanism.

## Responsibilities

* Consume domain events from Kafka.
* Identify the recipient of a notification.
* Resolve recipient information when it is not included directly in the event.
* Support notification payloads containing a `deliveryId`.
* Persist notifications for reliable processing.
* Process pending notifications asynchronously.
* Send notifications through the appropriate channel: EMAIL, SMS or PUSH (the senders are stubs that log for now).
* Handle failures without blocking the originating business process.

## Architecture

The service follows a **Clean Architecture / Ports & Adapters** approach:

```text
Kafka
  │
  ▼
Event Consumer
  │
  ▼
Notification Handler
  │
  ├── Recipient Resolver
  │
  └── Notification Repository
          │
          ▼
   Reactive PostgreSQL
       + Connection Pool
          │
          ▼
   Notification Scheduler
          │
          ▼
      Email Client
```

The Kafka consumer is responsible only for receiving events and delegating processing to the application layer. Notification delivery is performed asynchronously, keeping external communication outside the Kafka consumption path.

## Reactive Database Access

The service uses **reactive PostgreSQL access** instead of blocking JDBC calls.

Database operations are performed asynchronously using the reactive PostgreSQL client and Mutiny `Uni`/`Multi`. This allows database I/O to be integrated with the reactive execution model used by the application.

```text
Application
     │
     ▼
Repository
     │
     ▼
Reactive PostgreSQL Client
     │
     ▼
Connection Pool
     │
     ▼
PostgreSQL
```

The application does not dedicate a worker thread to waiting for a database response. Instead, the database operation returns a reactive type and the execution continues when PostgreSQL responds.

For example:

```kotlin
// PgNotificationQueue: claims a batch for sending in one statement
// (UPDATE notifications SET status = 'PROCESSING', attempts = attempts + 1 ... FOR UPDATE SKIP LOCKED),
// also reclaiming PROCESSING rows whose lease has expired.
fun claim(batchSize: Int, lease: Duration): Uni<List<Notification>>
```

This approach is particularly useful for the notification processing workload because many operations are I/O-bound:

* reading pending notifications,
* resolving recipients,
* persisting notifications,
* updating delivery status,
* communicating with external notification providers.

## Connection Pool

The reactive PostgreSQL client uses a **connection pool** to efficiently manage database connections.

Instead of creating a new database connection for every operation, the application maintains a pool of reusable connections:

```text
              ┌──────────────────────┐
              │   Connection Pool    │
              │                      │
Request ─────►│  C1  C2  C3  C4 ... │
              └───────┬──────────────┘
                      │
                      ▼
                 PostgreSQL
```

When a repository operation needs to access PostgreSQL:

1. A connection is acquired from the pool.
2. The SQL query is executed asynchronously.
3. The result is returned through the reactive pipeline.
4. The connection is released back to the pool.

The pool provides controlled concurrency and prevents the application from creating an excessive number of database connections.

Pool configuration can be tuned according to the expected workload, for example:

```properties
quarkus.datasource.reactive.max-size=20
quarkus.datasource.reactive.idle-timeout=60S
```

These two are not set in the project yet, so Quarkus defaults apply.

The pool size should not be treated as a simple "more is better" setting. It should be chosen according to PostgreSQL capacity, application concurrency and the expected number of concurrent database operations.

## Reactive Processing

Reactive database access is combined with the service's asynchronous processing model.

For example, processing a notification can be represented as:

```text
Find Pending Notification
          │
          ▼
    Acquire Connection
          │
          ▼
     Execute SQL
          │
          ▼
   Release Connection
          │
          ▼
     Send Email
          │
          ▼
    Update Status
```

Each I/O operation returns a reactive type instead of blocking the executing thread.

This allows the service to efficiently handle multiple notifications concurrently while keeping the number of database connections under control.

## Transaction Management

Database operations that need to be atomic are executed within a reactive transaction.

For example, creating a notification and updating related state can be performed as one transaction:

```text
BEGIN
  │
  ├── INSERT notification
  │
  ├── UPDATE related state
  │
  ▼
COMMIT
```

If an operation fails, the transaction is rolled back and the notification remains available for subsequent processing according to the retry strategy.

Reactive transactions are especially important here because using a traditional blocking transaction model would undermine the reactive database access pattern.

## Notification Flow

1. A business service publishes an event to Kafka.
2. The Notification Service consumes the event.
3. The notification handler determines whether a recipient is already present.
4. A notification is persisted with a `PENDING` status, one per channel (EMAIL, SMS, PUSH by event type); the recipient may still be empty.
5. A scheduler periodically claims pending notifications using reactive database queries.
6. If the recipient is missing, the service resolves it at send time: for events containing a `deliveryId`, it asks the delivery service (REST) for the driver and the driver's contact information.
7. A notification whose recipient cannot be resolved fails after the retries (e.g. `payment.declined` of a delivery that never had a driver).
8. The notification is delivered through the configured channel.
9. The notification status is updated to reflect the result of the delivery.

```text
Domain Event
     │
     ▼
 Kafka Consumer
     │
     ▼
Create Notification
     │
     ▼
Persist PENDING
     │
     ▼
Scheduler (claim batch)
     │
     ├── recipient exists ───────┐
     │                           │
     └── deliveryId exists       │
              │                  │
              ▼                  │
       Resolve Driver (REST)     │
              │                  │
              └──────────┬───────┘
                         ▼
          Sender (EMAIL / SMS / PUSH)
                         │
                  ┌──────┴──────┐
                  ▼             ▼
               SENT           FAILED
```

## Asynchronous Delivery

Notification delivery is deliberately separated from Kafka event consumption.

The Kafka consumer does not wait for the external email provider to complete the operation. Instead, it stores the notification and acknowledges the Kafka message once the notification has been safely persisted.

This provides:

* isolation from external service latency,
* reliable retry possibilities,
* shorter Kafka processing time,
* better failure handling,
* protection against temporary email-provider outages.

## Persistence

PostgreSQL is used to store notification state and delivery metadata.

A notification contains information such as:

* notification identifier,
* notification type,
* recipient,
* payload,
* delivery status,
* number of attempts,
* creation timestamp,
* processing timestamp.

Typical lifecycle:

```text
PENDING → PROCESSING → SENT
              │
              └→ PENDING (retry) ... after 5 attempts → FAILED
```

The persisted state allows the scheduler to safely resume processing after service restarts or temporary infrastructure failures.

## Technology Stack

* **Kotlin**
* **Quarkus**
* **Mutiny**
* **Reactive PostgreSQL Client**
* **Apache Kafka**
* **PostgreSQL**
* **Flyway**
* **REST Client**
* **Docker / Docker Compose**

## Design Goals

The Notification Service is designed around several principles:

* **Event-driven architecture** – notifications are triggered by domain events.
* **Reactive I/O** – database and external I/O operations use non-blocking APIs.
* **Connection pooling** – database connections are reused and concurrency is controlled through a reactive connection pool.
* **Asynchronous processing** – external communication does not block event processing.
* **Loose coupling** – business services do not need to know how notifications are delivered.
* **Reliable delivery** – notifications are persisted before delivery.
* **Retryability** – failed notifications can be processed again.
* **Extensibility** – additional notification channels can be introduced without changing the event-producing services.

The service therefore acts as a dedicated boundary between the platform's **domain events**, **reactive persistence layer**, and external **notification providers**.
