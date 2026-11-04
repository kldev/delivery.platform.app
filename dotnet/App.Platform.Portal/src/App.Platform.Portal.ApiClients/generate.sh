#!/usr/bin/env bash
# Regenerates the C# clients (Kiota) of the platform's services from openapi/<service>.json - the documents the
# gateway serves at /api-docs/<service>/swagger.json, with the gateway's paths (/api/delivery/deliveries, ...), so the
# clients call the gateway, never a service directly. The documents are kept in git: a change of an API shows up in
# review next to the regenerated client.
#
#   ./generate.sh                fetch the documents from the gateway (GATEWAY_URL, default http://localhost:5000),
#                                then regenerate Generated/
#   ./generate.sh --no-fetch     regenerate Generated/ from the committed documents only
#
# Needs the stack running for the fetch: docker compose up -d (repo root).
set -euo pipefail

cd "$(dirname "$0")"

SERVICES=(delivery payment settlement ledger reconciliation notification)
GATEWAY_URL="${GATEWAY_URL:-http://localhost:5000}"

if [[ "${1:-}" != "--no-fetch" ]]; then
  for service in "${SERVICES[@]}"; do
    curl -fsS "$GATEWAY_URL/api-docs/$service/swagger.json" -o "openapi/$service.json"
    python3 prepare_spec.py "openapi/$service.json"
    echo "openapi/$service.json fetched from $GATEWAY_URL"
  done
fi

dotnet tool restore >/dev/null
for service in "${SERVICES[@]}"; do
  class="$(tr '[:lower:]' '[:upper:]' <<< "${service:0:1}")${service:1}Api"
  namespace="App.Platform.Portal.ApiClients.$(tr '[:lower:]' '[:upper:]' <<< "${service:0:1}")${service:1}"
  dotnet kiota generate \
    --language csharp \
    --openapi "openapi/$service.json" \
    --class-name "$class" \
    --namespace-name "$namespace" \
    --output "Generated/$class" \
    --exclude-backward-compatible \
    --clean-output \
    --log-level Warning
done
echo "Generated/ regenerated"
