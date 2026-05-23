#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8100}"
DURATION="${DURATION:-7200}"
CONCURRENCY="${CONCURRENCY:-16}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"
OUT_DIR="${OUT_DIR:-reports/perf}"

mkdir -p "${OUT_DIR}"

echo "Crest long load test"
echo "Base URL: ${BASE_URL}"
echo "Duration: ${DURATION}s"
echo "Concurrency: ${CONCURRENCY}"
echo "Output: ${OUT_DIR}"

nohup python3 tools/perf/crest_load.py \
  --base-url "${BASE_URL}" \
  --duration "${DURATION}" \
  --concurrency "${CONCURRENCY}" \
  --admin-password "${ADMIN_PASSWORD}" \
  --out-dir "${OUT_DIR}" \
  > "${OUT_DIR}/long-load-$(date +%Y%m%d-%H%M%S).log" 2>&1 &

pid=$!
echo "${pid}" > "${OUT_DIR}/long-load.pid"
echo "Started PID ${pid}"
echo "Tail logs with: tail -f ${OUT_DIR}/long-load-*.log"
