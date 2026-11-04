import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:5000';

const flowSuccess = new Counter('flow_success');
const flowFailure = new Counter('flow_failure');

const MAX_RETRIES = 10;
const DEFAULT_RETRY_AFTER_SECONDS = 1;

export const options = {
    scenarios: {
        delivery_flow: {
            executor: 'shared-iterations',
            vus: 50,
            iterations: 50,
            maxDuration: '2m',
        },
    },

    thresholds: {
        checks: ['rate>0.95'],
        flow_success: ['count>0'],
        http_req_failed: ['rate<0.05'],
    },
};

function request(method, url, body = null) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Api-Key': 'Rmc37uQiNA25qxhJuYw68wre3lSBHpkA7zyLI',
            'X-Idempotency-Key': uuidv4(),  // payment service
            'Idempotency-Key': uuidv4(),    // delivery service
        },
    };

    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
        const response = http.request(
            method,
            `${BASE_URL}${url}`,
            body,
            params,
        );

        if (response.status !== 429) {
            return response;
        }

        if (attempt >= MAX_RETRIES) {
            console.warn(
                `[RATE LIMIT] ${method} ${url} -> 429, ` +
                `max retries (${MAX_RETRIES}) reached`
            );

            return response;
        }

        const retryAfter = parseRetryAfter(response);

        console.warn(
            `[RATE LIMIT] ${method} ${url} -> 429, ` +
            `retry ${attempt + 1}/${MAX_RETRIES} ` +
            `in ${retryAfter}s`
        );

        for (let remaining = retryAfter; remaining > 0; remaining--) {
            console.log(
                `[RATE LIMIT] ${method} ${url} -> ` +
                `retry in ${remaining}s ` +
                `(attempt ${attempt + 1}/${MAX_RETRIES})`
            );

            sleep(1);
        }
    }
}

function parseRetryAfter(response) {
    const value = response.headers['Retry-After'];
    console.info(response.headers)

    if (!value) {
        console.warn(
            '[RATE LIMIT] 429 without Retry-After header, ' +
            `using default ${DEFAULT_RETRY_AFTER_SECONDS}s`
        );

        return DEFAULT_RETRY_AFTER_SECONDS;
    }

    const seconds = Number.parseInt(value, 10);

    if (Number.isNaN(seconds) || seconds < 1) {
        console.warn(
            `[RATE LIMIT] Invalid Retry-After="${value}", ` +
            `using default ${DEFAULT_RETRY_AFTER_SECONDS}s`
        );

        return DEFAULT_RETRY_AFTER_SECONDS;
    }

    return seconds;
}

// ---------------------------------------------------------
// PAYMENT
// ---------------------------------------------------------

function getPendingPayment(deliveryId) {
    const response = request(
        'GET',
        `/api/payment/payments?status=Pending&delivryId=${deliveryId ?? ""}`,
    );

    const valid = check(response, {
        'get pending payments - status 200': r =>
            r.status === 200,
    });

    if (!valid) {
        return null;
    }

    const payments = response.json();

    if (!payments || payments.length === 0) {
        return null;
    }

    return payments;
}

function waitForPayment(deliveryId, timeout = 30000) {
    const start = Date.now();

    while (Date.now() - start < timeout) {
        const payments = getPendingPayment(deliveryId);

        if (payments == null || payments.length == 0) {        
             sleep(1);
            continue
        }

       const payment = payments.find(
            item => item.deliveryId === deliveryId,
        );

        if (payment) {
            return payment;
        }

        sleep(1);
    }

    return null;
}

// ---------------------------------------------------------
// ASSIGNED DELIVERY
// ---------------------------------------------------------

function getDeliveries(deliveryId, status) {
    const response = request(
        'GET',
        `/api/delivery/deliveries?status=${status}&deliveryId=${deliveryId ?? null}`,
    );

    const valid = check(response, {
        'get assigned deliveries - status 200': r =>
            r.status === 200,

        'get assigned deliveries - has list': r =>
            Array.isArray(r.json().content),
    });

    if (!valid) {
        return [];
    }

    return response.json().content;
}

function waitForCompletedDelivery(deliveryId, timeout = 30000) {
    const start = Date.now();

    while (Date.now() - start < timeout) {
        const deliveries = getDeliveries(deliveryId, 'DELIVERED');

        const delivery = deliveries.find(
            item => item.id === deliveryId,
        );

        if (delivery) {
            return delivery;
        }

        sleep(1);
    }

    return null;
}

function waitForAssignedDelivery(deliveryId, timeout = 30000) {
    const start = Date.now();

    while (Date.now() - start < timeout) {
        const deliveries = getDeliveries(deliveryId, 'ASSIGNED');

        const delivery = deliveries.find(
            item => item.id === deliveryId,
        );

        if (delivery) {
            return delivery;
        }

        sleep(1);
    }

    return null;
}

// ---------------------------------------------------------
// LEDGER
// ---------------------------------------------------------

function getLedgerEntries() {
    const response = request(
        'GET',
        '/api/ledger/ledger-entries',
    );

    check(response, {
        'get ledger entries - status 200': r =>
            r.status === 200,
    });

    return response;
}

// ---------------------------------------------------------
// RECONCOLIATION
// ---------------------------------------------------------


function getReconciliationEntries() {
       const response = request(
        'GET',
        '/api/reconciliation/reconciliations',
    );

    check(response, {
        'get reconciliation entries - status 200': r =>
            r.status === 200,
    });

    return response;
}

// ---------------------------------------------------------
// SETUP
// ---------------------------------------------------------

export function setup() {
    const response = request(
        'GET',
        '/api/delivery/drivers',
    );

    const valid = check(response, {
        'get drivers - status 200': r =>
            r.status === 200,

        'get drivers - has list': r =>
            Array.isArray(r.json()),
    });

    if (!valid) {
        throw new Error('Failed to get drivers');
    }

    const drivers = response.json();

    if (drivers.length === 0) {
         const phones = [
            '+48123456701',
            '+48123456702',
            '+48123456703',
            '+48123456704',
            '+48123456705',
            '+48123456706',
            '+48123456707',
            '+48123456708',
            '+48123456709',
            '+48123456710',
        ];


        console.log(`No drivers found. Creating ${phones.length} drivers...`);

      
        for (let i = 0; i < phones.length; i++) {
            const createDriverResponse = request(
                'POST',
                '/api/delivery/drivers',
                JSON.stringify({
                    firstName: `TestDriver${i + 1}`,
                    lastName: 'K6',
                    phoneNumber: phones[i],
                    email: `test.driver${i+1}@fake.io`
                }),
            );

            const created = check(createDriverResponse, {
                'create driver - status 201': r =>
                    r.status === 201,

                'create driver - has id': r =>
                    r.json('id') !== undefined,
            });

            if (!created) {
                throw new Error(
                    `Failed to create driver ${i + 1}`,
                );
            }
        }

        console.log(`{phones.length} drivers created`);
    } else {
        console.log(
            `Drivers already exist (${drivers.length}). Skipping creation.`,
        );
    }
}

// ---------------------------------------------------------
// DELIVERY FLOW
// ---------------------------------------------------------

export default function () {
    const suffix = `${__VU}-${__ITER}-${Date.now()}`;

    console.log(
        `Starting delivery flow ${suffix}`,
    );

    // ---------------------------------------------------------
    // 1. CREATE DELIVERY
    // ---------------------------------------------------------
    const currencies = ['PLN', 'EUR', 'GBP'];

    const currency = currencies[Math.floor(Math.random() * currencies.length)];
    const distanceKm = 20 + Number((Math.random() * 100).toFixed(2));

    const deliveryResponse = request(
        'POST',
        '/api/delivery/deliveries',
        JSON.stringify({
            deliveryAddress: `Test Delivery ${suffix}`,
            distanceKm,
            pickupAddress: `Test Pickup ${suffix}`,
            currency,
        }),
    );

    const deliveryCreated = check(deliveryResponse, {
        'create delivery - status 200/201': r =>
            r.status === 200 || r.status === 201,

        'create delivery - has id': r =>
            r.json('id') !== undefined,
    });

    if (!deliveryCreated) {
        flowFailure.add(1);
        return;
    }

    const deliveryId = deliveryResponse.json('id');

    console.log(
        `Delivery created: ${deliveryId}`,
    );

    // ---------------------------------------------------------
    // 2. CONFIRM DELIVERY
    // ---------------------------------------------------------

    const confirmResponse = request(
        'POST',
        `/api/delivery/deliveries/${deliveryId}/confirm`,
    );

    const confirmed = check(confirmResponse, {
        'confirm delivery - status 200': r =>
            r.status === 200,
    });

    if (!confirmed) {
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 3. WAIT FOR PAYMENT
    // ---------------------------------------------------------

    const payment = waitForPayment(deliveryId);

    const paymentFound = check(payment || {}, {
        'payment created': p =>
            p !== null,

        'payment has id': p =>
            p && p.id !== undefined,
    });

    if (!paymentFound) {
        console.error(
            `Payment not found for delivery ${deliveryId}`,
        );

        flowFailure.add(1);
        return;
    }

    const paymentId = payment.id;

    console.log(
        `Payment created: ${paymentId}`,
    );

    // ---------------------------------------------------------
    // 4. ACCEPT PAYMENT
    // ---------------------------------------------------------

    const acceptPaymentResponse = request(
        'PUT',
        `/api/payment/payments/${paymentId}/accept`,
    );

    const paymentAccepted = check(
        acceptPaymentResponse,
        {
            'accept payment - status 204': r =>
                r.status === 204,
        },
    );

    if (!paymentAccepted) {
        console.error(acceptPaymentResponse);
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 5. WAIT FOR DELIVERY ASSIGNMENT
    // ---------------------------------------------------------

    const assignedDelivery =
        waitForAssignedDelivery(deliveryId);

    const deliveryAssigned = check(
        assignedDelivery || {},
        {
            'delivery assigned': d =>
                d !== null,

            'assigned delivery has id': d =>
                d && d.id !== undefined,

            'assigned delivery has correct id': d =>
                d && d.id === deliveryId,
            
            'assigned delivery has ASSIGNED status': d =>
            d && d.status === 'ASSIGNED',
        },
    );

    if (!deliveryAssigned) {
        console.error(
            `Delivery ${deliveryId} was not assigned`,
        );

        flowFailure.add(1);
        return;
    }

    console.log(
        `Delivery assigned: ${deliveryId}`,
    );

    // ---------------------------------------------------------
    // 6. PICKUP DELIVERY
    // ---------------------------------------------------------

    const pickupResponse = request(
        'POST',
        `/api/delivery/deliveries/${deliveryId}/pickup`,
    );

    const pickedUp = check(pickupResponse, {
        'pickup delivery - status 200': r =>
            r.status === 200,
    });

    if (!pickedUp) {
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 7. START DELIVERY
    // ---------------------------------------------------------

    const startResponse = request(
        'POST',
        `/api/delivery/deliveries/${deliveryId}/start`,
    );

    const started = check(startResponse, {
        'start delivery - status 200': r =>
            r.status === 200,
    });

    if (!started) {
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 8. COMPLETE DELIVERY
    // ---------------------------------------------------------

    const completeResponse = request(
        'POST',
        `/api/delivery/deliveries/${deliveryId}/complete`,
    );

    const completed = check(completeResponse, {
        'complete delivery - status 200': r =>
            r.status === 200,
    });

    if (!completed) {
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 9. GET DELIVERIES
    // ---------------------------------------------------------

    const deliveriesResponse = request(
        'GET',
        '/api/delivery/deliveries',
    );

    check(deliveriesResponse, {
        'get deliveries - status 200': r =>
            r.status === 200,
    });

    // ---------------------------------------------------------
    // 10. GET PAYMENTS
    // ---------------------------------------------------------

    const paymentsResponse = request(
        'GET',
        '/api/payment/payments',
    );

    check(paymentsResponse, {
        'get payments - status 200': r =>
            r.status === 200,
    });

   
    waitForCompletedDelivery(deliveryId)
   
    sleep(1)
    // ---------------------------------------------------------
    // 11. GET LEDGER
    // ---------------------------------------------------------

    const ledgerResponse = getLedgerEntries();

    if (ledgerResponse.status !== 200) {
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 12. Complete PAYMENT
    // ---------------------------------------------------------

    const completePaymenttResponse = request(
        'POST',
        `/api/payment/payments/external`,
        JSON.stringify(
        {
            paymentId: payment.id,
            transactionId: `TXT-` + payment.id,
            amount: (Math.random() < 0.5)  ? 7.0000 : 9.000,
            provider: "K6-TEST"
        })
    );

    const paymentComplete = check(
        completePaymenttResponse,
        {
            'complete payment - status 200': r =>
                r.status === 200,
        },
    );

    if (!paymentComplete) {
        console.error(completePaymenttResponse);
        flowFailure.add(1);
        return;
    }

    // ---------------------------------------------------------
    // 13. GET Reconciliations
    // ---------------------------------------------------------

    const reconciliationResponse = getReconciliationEntries();

    if (reconciliationResponse.status !== 200) {
        flowFailure.add(1);
        return;
    }


    // ---------------------------------------------------------
    // FLOW COMPLETED
    // ---------------------------------------------------------

    flowSuccess.add(1);

    console.log(
        `Flow completed successfully: ` +
        `delivery=${deliveryId}, ` +
        `payment=${paymentId}`,
    );
}