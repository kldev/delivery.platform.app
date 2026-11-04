import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';


export const options = {
    scenarios: {
        notifications: {
            executor: 'ramping-arrival-rate',
            startRate: 50,
            timeUnit: '1s',
            preAllocatedVUs: 50,
            maxVUs: 500,

            stages: [
                { target: 100, duration: '30s' },
                { target: 200, duration: '30s' },
                { target: 500, duration: '30s' },
                { target: 1000, duration: '30s' },
                { target: 0, duration: '10s' },
            ],
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: [
            'p(95)<500',
            'p(99)<1000',
        ],
    },
};

const BASE_URL = 'http://localhost:8005';

export default function () {    
    const eventId = uuidv4()

    const payload = JSON.stringify({
        eventId: eventId,
        eventType: 'DeliveryCreated',
        recipient: { email: `user-${__VU}-${__ITER}@example.com` },
        channel: 'EMAIL',
        payload: {
            deliveryId: crypto.randomUUID(),
            message: 'Performance test notification',
        },
    });

    const response = http.post(
        `${BASE_URL}/api/notifications`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
            tags: {
                endpoint: 'create-notification',
            },
        }
    );

    check(response, {
        'status is 2xx': (r) => r.status >= 200 && r.status < 300,
    });
}