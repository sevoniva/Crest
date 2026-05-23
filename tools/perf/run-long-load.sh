#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8100}"
DURATION="${DURATION:-7800}"
CONCURRENCY="${CONCURRENCY:-12}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"
OUT_DIR="${OUT_DIR:-reports/perf}"
PROFILE="${PROFILE:-openapi}"
SAFE_ONLY="${SAFE_ONLY:-true}"
ALLOW_CLIENT_ERRORS="${ALLOW_CLIENT_ERRORS:-false}"
CASES_FILE="${CASES_FILE:-reports/perf/stable-openapi-cases.json}"
PROGRESS_INTERVAL="${PROGRESS_INTERVAL:-60}"

mkdir -p "${OUT_DIR}"

echo "Crest long load test"
echo "Base URL: ${BASE_URL}"
echo "Duration: ${DURATION}s"
echo "Concurrency: ${CONCURRENCY}"
echo "Profile: ${PROFILE}"
echo "Safe only: ${SAFE_ONLY}"
echo "Cases file: ${CASES_FILE}"
echo "Output: ${OUT_DIR}"

client_error_flag=()
if [[ "${ALLOW_CLIENT_ERRORS}" == "true" ]]; then
  client_error_flag=(--allow-client-errors)
fi
safe_only_flag=()
if [[ "${SAFE_ONLY}" == "true" ]]; then
  safe_only_flag=(--safe-only)
fi

cases_file_args=()
if [[ -f "${CASES_FILE}" ]]; then
  cases_file_args=(--cases-file "${CASES_FILE}")
else
  cases_file_args=(--profile "${PROFILE}" "${safe_only_flag[@]}" --preflight-ok --save-cases "${CASES_FILE}")
fi

nohup python3 -u tools/perf/crest_load.py \
  --base-url "${BASE_URL}" \
  --duration "${DURATION}" \
  --concurrency "${CONCURRENCY}" \
  "${cases_file_args[@]}" \
  "${client_error_flag[@]}" \
  --progress-interval "${PROGRESS_INTERVAL}" \
  --admin-password "${ADMIN_PASSWORD}" \
  --out-dir "${OUT_DIR}" \
  > "${OUT_DIR}/long-load-$(date +%Y%m%d-%H%M%S).log" 2>&1 &

pid=$!
echo "${pid}" > "${OUT_DIR}/long-load.pid"
echo "Started PID ${pid}"
echo "Tail logs with: tail -f ${OUT_DIR}/long-load-*.log"
