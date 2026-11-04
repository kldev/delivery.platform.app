#!/usr/bin/env bash
# Runs the portal's e2e tests (Playwright, Chromium) against the running stack and writes their screenshots to
# docs/screens/portal/ - the pictures of docs/Portal.md. Start the stack first: docker compose up -d --build.
# PORTAL_URL / GATEWAY_URL override http://localhost:5080 / http://localhost:5000.
set -euo pipefail
cd "$(dirname "$0")/.."

# From the portal's folder: its global.json selects the Microsoft.Testing.Platform runner.
(
  cd dotnet/App.Platform.Portal
  dotnet build tests/App.Platform.Portal.E2E --nologo -v quiet
  PORTAL_SCREENS_TO_DOCS=1 dotnet test --project tests/App.Platform.Portal.E2E --no-build
)
ls -1 docs/screens/portal
