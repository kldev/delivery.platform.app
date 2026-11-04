rootProject.name = "delivery-platform"

include(
    ":services:delivery-service:delivery-domain",
    ":services:delivery-service:delivery-web",
    ":services:ledger-service:ledger-domain",
    ":services:ledger-service:ledger-web",
    ":services:settlement-service:settlement-domain",
    ":services:settlement-service:settlement-web",
    ":services:reconciliation-service:reconciliation-domain",
    ":services:reconciliation-service:reconciliation-web",
    ":services:notification-service:notification-domain",
    ":services:notification-service:notification-web",

    ":libraries:common"    
)