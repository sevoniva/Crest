#!/usr/bin/env python3
import argparse
import base64
import concurrent.futures
import hashlib
import hmac
import json
import math
import os
import random
import statistics
import sys
import threading
import time
import urllib.error
import urllib.request
from collections import Counter, defaultdict
from datetime import datetime


DEFAULT_CASES = [
    ("GET", "/doc.html", None, 2, "docs"),
    ("GET", "/swagger-ui.html", None, 1, "docs"),
    ("GET", "/v3/api-docs/swagger-config", None, 2, "docs"),
    ("GET", "/v3/api-docs", None, 1, "docs"),
    ("GET", "/v3/api-docs/1-visualization", None, 1, "docs"),
    ("GET", "/v3/api-docs/3-dataset", None, 1, "docs"),
    ("GET", "/de2api/sysParameter/defaultSettings", None, 8, "system"),
    ("GET", "/de2api/sysParameter/i18nOptions", None, 4, "system"),
    ("GET", "/de2api/sysParameter/requestTimeOut", None, 4, "system"),
    ("GET", "/de2api/menu/query", None, 6, "system"),
    ("GET", "/de2api/user/info", None, 4, "system"),
    ("GET", "/de2api/datasource/types", None, 4, "datasource"),
    ("POST", "/de2api/datasource/tree", {}, 8, "datasource"),
    ("POST", "/de2api/datasetTree/tree", {}, 8, "dataset"),
    ("POST", "/de2api/dataVisualization/tree", {"leaf": True, "busiFlag": "dashboard-dataV"}, 6, "visualization"),
    ("POST", "/de2api/dataVisualization/findRecent", {"type": "panel"}, 4, "visualization"),
    ("POST", "/de2api/relation/overview", {}, 8, "lineage"),
    ("POST", "/de2api/relation/resources/datasource", {}, 5, "lineage"),
    ("POST", "/de2api/relation/resources/dataset", {}, 5, "lineage"),
    ("POST", "/de2api/relation/resources/dv", {}, 5, "lineage"),
]

NO_PROXY_OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))

PATH_SAMPLE_VALUES = {
    "id": "985192741891870720",
    "resourceId": "985192741891870720",
    "dvId": "985192741891870720",
    "viewId": "985192540087128064",
    "viewIdSource": "985192540087128064",
    "viewIdTarget": "985192540087128064",
    "chartId": "985192540087128064",
    "fieldId": "1715053944934",
    "fieldType": "dimension",
    "datasourceId": "985188400292302848",
    "dsId": "985188400292302848",
    "datasetGroupId": "985189053949415424",
    "driverId": "1",
    "jarId": "1",
    "dsType": "mysql",
    "key": "basic.defaultSort",
    "keyWord": "Demo",
    "resourceTable": "core",
    "busiFlag": "dashboard",
    "type": "panel",
    "status": "ALL",
    "goPage": "1",
    "pageSize": "20",
    "file": "default",
    "fileId": "perf-probe",
}

PATH_PARAM_OVERRIDES = {
    "/de2api/exportCenter/download/{id}": {"id": "1255524262790434816"},
    "/de2api/exportCenter/generateDownloadUri/{id}": {"id": "1255524262790434816"},
    "/de2api/exportCenter/delete/{id}": {"id": "1255524262790434816"},
    "/de2api/exportCenter/retry/{id}": {"id": "1255524262790434816"},
}

BODY_OVERRIDES = {
    "/de2api/datasource/tree": {},
    "/de2api/datasetTree/tree": {},
    "/de2api/dataVisualization/tree": {"leaf": True, "busiFlag": "dashboard-dataV"},
    "/de2api/dataVisualization/interactiveTree": {"leaf": True, "busiFlag": "dashboard-dataV"},
    "/de2api/dataVisualization/findRecent": {"type": "panel"},
    "/de2api/dataVisualization/findById": {"id": 985192741891870720, "busiFlag": "dashboard", "resourceTable": "core"},
    "/de2api/dataVisualization/updatePublishStatus": {"id": 985192741891870720, "status": 1, "busiFlag": "dashboard", "resourceTable": "core"},
    "/de2api/store/query": {"type": "panel"},
    "/de2api/relation/overview": {},
    "/de2api/relation/resources/{type}": {},
    "/de2api/linkage/getViewLinkageGather": {"dvId": 985192741891870720, "resourceTable": "core"},
    "/de2api/linkage/getViewLinkageGatherArray": {"dvId": 985192741891870720, "resourceTable": "core"},
    "/de2api/linkJump/queryTargetVisualizationJumpInfo": {"dvId": 985192741891870720, "resourceTable": "core"},
    "/de2api/datasetField/getFunction": None,
    "/de2api/exportCenter/exportTasks/records": None,
    "/de2api/exportCenter/exportLimit": None,
    "/de2api/msg-center/count": None,
}

MUTATION_HINTS = (
    "save", "update", "delete", "del", "remove", "create", "upload", "sync",
    "execute", "retry", "move", "rename", "switch", "setdefault", "enable",
    "copy", "decompression", "publish", "recover", "export", "logout",
)

OPENAPI_SKIP_PATTERNS = (
    ("download", "binary download or generated file required"),
    ("upload", "multipart upload payload required"),
    ("decompression", "uploaded archive required"),
    ("exportlog", "browser render artifact required"),
    ("innerexport", "complete chart export payload required"),
    ("exportcenter/delete", "destructive export task operation"),
    ("exportcenter/retry", "stateful export task operation"),
    ("datasetsync/execute", "stateful async sync operation"),
    ("datasetsync/stop", "stateful async sync operation"),
    ("datasource/syncapi", "stateful datasource API sync operation"),
    ("store/execute", "stateful store operation"),
    ("share/", "share link token or exact share payload required"),
)

SAFE_ONLY_SKIP_PATTERNS = OPENAPI_SKIP_PATTERNS + (
    ("save", "state-changing operation excluded from soak load"),
    ("update", "state-changing operation excluded from soak load"),
    ("delete", "state-changing operation excluded from soak load"),
    ("remove", "state-changing operation excluded from soak load"),
    ("move", "state-changing operation excluded from soak load"),
    ("rename", "state-changing operation excluded from soak load"),
    ("copy", "state-changing operation excluded from soak load"),
    ("switch", "state-changing operation excluded from soak load"),
    ("recover", "state-changing operation excluded from soak load"),
    ("publish", "state-changing operation excluded from soak load"),
    ("create", "state-changing operation excluded from soak load"),
    ("logout", "session state operation excluded from soak load"),
)


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def admin_token(password: str) -> str:
    secret = hashlib.md5(password.encode()).hexdigest().encode()
    header = b64url(json.dumps({"alg": "HS256", "typ": "JWT"}, separators=(",", ":")).encode())
    payload = b64url(json.dumps({"uid": 1, "oid": 1}, separators=(",", ":")).encode())
    signing_input = f"{header}.{payload}".encode()
    sig = b64url(hmac.new(secret, signing_input, hashlib.sha256).digest())
    return f"{header}.{payload}.{sig}"


def request(base_url, case, token, timeout, allow_client_errors=False):
    method, path, body, _weight, group = case
    url = base_url.rstrip("/") + path
    data = None
    headers = {
        "User-Agent": "crest-load/1.0",
        "Accept": "application/json,text/html,*/*",
        "X-DE-TOKEN": token,
    }
    if body is not None:
        data = json.dumps(body, ensure_ascii=False).encode()
        headers["Content-Type"] = "application/json"
    started = time.perf_counter()
    status = 0
    size = 0
    err = ""
    try:
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        with NO_PROXY_OPENER.open(req, timeout=timeout) as resp:
            payload = resp.read()
            status = resp.status
            size = len(payload)
            content_type = resp.headers.get("Content-Type", "")
            if "application/json" in content_type and payload.startswith(b"{"):
                parsed = json.loads(payload)
                if (
                    not allow_client_errors
                    and isinstance(parsed, dict)
                    and "code" in parsed
                    and parsed.get("code") not in (0, "0", None)
                ):
                    err = json.dumps(parsed, ensure_ascii=False)[:240]
    except urllib.error.HTTPError as e:
        status = e.code
        payload = e.read()
        size = len(payload)
        if not (allow_client_errors and 400 <= status < 500):
            err = payload[:240].decode("utf-8", "ignore")
    except Exception as e:
        err = repr(e)
    elapsed_ms = (time.perf_counter() - started) * 1000
    return {
        "method": method,
        "path": path,
        "group": group,
        "status": status,
        "ok": ((200 <= status < 400) or (allow_client_errors and 400 <= status < 500)) and not err,
        "elapsed_ms": elapsed_ms,
        "size": size,
        "error": err,
    }


def load_cases(path):
    with open(path, "r", encoding="utf-8") as f:
        return [tuple(item) for item in json.load(f)]


def save_cases(path, cases):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(cases, f, ensure_ascii=False, indent=2)


def path_group(path):
    parts = path.strip("/").split("/")
    if len(parts) >= 2 and parts[0] == "de2api":
        return parts[1]
    if path.startswith("/v3/api-docs") or path in ("/doc.html", "/swagger-ui.html"):
        return "docs"
    return "openapi"


def replace_path_params(path):
    values = dict(PATH_SAMPLE_VALUES)
    values.update(PATH_PARAM_OVERRIDES.get(path, {}))
    for key, value in values.items():
        path = path.replace("{" + key + "}", value)
    return path


def generic_body(path, operation):
    if path in BODY_OVERRIDES:
        return BODY_OVERRIDES[path]
    if "requestBody" not in operation:
        return None
    content = operation.get("requestBody", {}).get("content", {})
    if not any("json" in k for k in content):
        return None
    lowered = path.lower()
    body = {}
    if "datasource" in lowered:
        body.update({"id": 985188400292302848, "name": "Demo", "type": "mysql", "nodeType": "leaf"})
    if "dataset" in lowered:
        body.update({"id": 985189053949415424, "name": "Demo dataset", "pid": 0, "nodeType": "leaf", "mode": 0, "info": "[]"})
    if "chart" in lowered:
        body.update({"id": 985192540087128064, "sceneId": 985192741891870720, "tableId": 985189053949415424, "type": "table", "render": "echarts", "resultCount": 1000})
    if "visualization" in lowered or "outerparams" in lowered or "link" in lowered:
        body.update({"id": 985192741891870720, "dvId": 985192741891870720, "resourceTable": "core", "busiFlag": "dashboard", "type": "panel"})
    if "field" in lowered:
        body.update({"id": 1715053944934, "datasetGroupId": 985189053949415424, "fieldType": "dimension"})
    if not body:
        body = {}
    return body


def openapi_cases(base_url, token, timeout, safe_only=False):
    req = urllib.request.Request(base_url.rstrip("/") + "/v3/api-docs", headers={"X-DE-TOKEN": token})
    with NO_PROXY_OPENER.open(req, timeout=timeout) as resp:
        spec = json.loads(resp.read())
    cases = []
    skipped = []
    for path, methods in sorted(spec.get("paths", {}).items()):
        for method, operation in methods.items():
            method = method.upper()
            lowered_path = path.lower()
            skip_patterns = SAFE_ONLY_SKIP_PATTERNS if safe_only else OPENAPI_SKIP_PATTERNS
            skip = next((reason for pattern, reason in skip_patterns if pattern in lowered_path), None)
            if skip:
                skipped.append({"method": method, "path": path, "reason": skip})
                continue
            if method not in ("GET", "POST"):
                skipped.append({"method": method, "path": path, "reason": "unsupported method"})
                continue
            concrete_path = replace_path_params(path)
            if "{" in concrete_path:
                skipped.append({"method": method, "path": path, "reason": "unknown path parameter"})
                continue
            body = generic_body(path, operation)
            summary = (operation.get("summary") or operation.get("operationId") or "").lower()
            mutation = any(h in path.lower() or h in summary for h in MUTATION_HINTS)
            if safe_only and mutation:
                skipped.append({"method": method, "path": path, "reason": "state-changing operation excluded from soak load"})
                continue
            group = "mutating" if mutation else path_group(path)
            cases.append((method, concrete_path, body, 1, group))
    return cases, skipped


def percentile(values, pct):
    if not values:
        return 0.0
    values = sorted(values)
    index = min(len(values) - 1, math.ceil(len(values) * pct / 100) - 1)
    return values[index]


def summarize(results, duration):
    latencies = [r["elapsed_ms"] for r in results]
    by_status = Counter(str(r["status"]) for r in results)
    errors = [r for r in results if not r["ok"]]
    by_group = defaultdict(list)
    by_path = defaultdict(list)
    for r in results:
        by_group[r["group"]].append(r)
        by_path[f'{r["method"]} {r["path"]}'].append(r)
    def block(items):
        lats = [r["elapsed_ms"] for r in items]
        return {
            "requests": len(items),
            "ok": sum(1 for r in items if r["ok"]),
            "errors": sum(1 for r in items if not r["ok"]),
            "avg_ms": statistics.mean(lats) if lats else 0,
            "p50_ms": percentile(lats, 50),
            "p95_ms": percentile(lats, 95),
            "p99_ms": percentile(lats, 99),
            "max_ms": max(lats) if lats else 0,
        }
    return {
        "requests": len(results),
        "duration_sec": duration,
        "rps": len(results) / duration if duration > 0 else 0,
        "status": dict(by_status),
        "latency": block(results),
        "groups": {k: block(v) for k, v in sorted(by_group.items())},
        "paths": {k: block(v) for k, v in sorted(by_path.items())},
        "sample_errors": errors[:20],
    }


def write_report(out_dir, summary, results, args):
    os.makedirs(out_dir, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    json_path = os.path.join(out_dir, f"crest-load-{stamp}.json")
    md_path = os.path.join(out_dir, f"crest-load-{stamp}.md")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump({"args": vars(args), "summary": summary, "results": results}, f, ensure_ascii=False, indent=2)
    lines = [
        "# Crest Load Test Report",
        "",
        f"- Base URL: `{args.base_url}`",
        f"- Duration: `{summary['duration_sec']:.1f}s`",
        f"- Concurrency: `{args.concurrency}`",
        f"- Requests: `{summary['requests']}`",
        f"- RPS: `{summary['rps']:.2f}`",
        f"- Status: `{summary['status']}`",
        f"- Profile: `{args.profile}`",
        f"- Safe only: `{args.safe_only}`",
        f"- Allow client errors: `{args.allow_client_errors}`",
        "",
        "## Overall",
        "",
        "| metric | value |",
        "| --- | ---: |",
    ]
    for k in ["avg_ms", "p50_ms", "p95_ms", "p99_ms", "max_ms"]:
        lines.append(f"| {k} | {summary['latency'][k]:.2f} |")
    lines += ["", "## Groups", "", "| group | requests | errors | avg ms | p95 ms | p99 ms | max ms |", "| --- | ---: | ---: | ---: | ---: | ---: | ---: |"]
    for group, item in summary["groups"].items():
        lines.append(f"| {group} | {item['requests']} | {item['errors']} | {item['avg_ms']:.2f} | {item['p95_ms']:.2f} | {item['p99_ms']:.2f} | {item['max_ms']:.2f} |")
    lines += ["", "## Slowest Paths", "", "| path | requests | errors | avg ms | p95 ms | max ms |", "| --- | ---: | ---: | ---: | ---: | ---: |"]
    slow = sorted(summary["paths"].items(), key=lambda kv: kv[1]["p95_ms"], reverse=True)[:20]
    for path, item in slow:
        lines.append(f"| `{path}` | {item['requests']} | {item['errors']} | {item['avg_ms']:.2f} | {item['p95_ms']:.2f} | {item['max_ms']:.2f} |")
    if summary["sample_errors"]:
        lines += ["", "## Sample Errors", ""]
        for e in summary["sample_errors"][:10]:
            lines.append(f"- `{e['method']} {e['path']}` status `{e['status']}`: {e['error'][:180]}")
    if getattr(args, "skipped", None):
        lines += ["", "## Skipped OpenAPI Operations", ""]
        for item in args.skipped[:50]:
            lines.append(f"- `{item['method']} {item['path']}`: {item['reason']}")
    with open(md_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    return json_path, md_path


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://127.0.0.1:8100")
    parser.add_argument("--duration", type=int, default=60)
    parser.add_argument("--concurrency", type=int, default=8)
    parser.add_argument("--timeout", type=float, default=20)
    parser.add_argument("--admin-password", default="admin")
    parser.add_argument("--out-dir", default="reports/perf")
    parser.add_argument("--profile", choices=["core", "openapi"], default="core")
    parser.add_argument("--safe-only", action="store_true")
    parser.add_argument("--preflight-ok", action="store_true")
    parser.add_argument("--cases-file")
    parser.add_argument("--save-cases")
    parser.add_argument("--progress-interval", type=int, default=60)
    parser.add_argument("--allow-client-errors", action="store_true")
    args = parser.parse_args()

    token = admin_token(args.admin_password)
    args.skipped = []
    if args.cases_file:
        cases = load_cases(args.cases_file)
    elif args.profile == "openapi":
        cases, skipped = openapi_cases(args.base_url, token, args.timeout, args.safe_only)
        args.skipped = skipped
        cases.extend(DEFAULT_CASES)
    else:
        cases = DEFAULT_CASES
    if args.preflight_ok:
        checked = []
        for case in cases:
            result = request(args.base_url, case, token, args.timeout, False)
            if result["ok"]:
                checked.append(case)
            else:
                args.skipped.append({
                    "method": case[0],
                    "path": case[1],
                    "reason": f"preflight failed status={result['status']} error={result['error'][:120]}",
                })
        cases = checked
    if args.save_cases:
        save_cases(args.save_cases, cases)
    weighted = []
    for case in cases:
        weighted.extend([case] * case[3])
    results = []
    lock = threading.Lock()
    progress = {"requests": 0, "errors": 0}
    stop_at = time.perf_counter() + args.duration

    def worker():
        local = []
        local_requests = 0
        local_errors = 0
        while time.perf_counter() < stop_at:
            item = request(args.base_url, random.choice(weighted), token, args.timeout, args.allow_client_errors)
            local.append(item)
            local_requests += 1
            if not item["ok"]:
                local_errors += 1
            if local_requests >= 100:
                with lock:
                    progress["requests"] += local_requests
                    progress["errors"] += local_errors
                local_requests = 0
                local_errors = 0
        if local_requests:
            with lock:
                progress["requests"] += local_requests
                progress["errors"] += local_errors
        with lock:
            results.extend(local)

    started = time.perf_counter()
    def progress_reporter():
        if args.progress_interval <= 0:
            return
        while time.perf_counter() < stop_at:
            time.sleep(min(args.progress_interval, max(0.1, stop_at - time.perf_counter())))
            elapsed = time.perf_counter() - started
            with lock:
                total = progress["requests"]
                errors = progress["errors"]
            rps = total / elapsed if elapsed > 0 else 0
            print(
                json.dumps(
                    {
                        "event": "progress",
                        "elapsed_sec": round(elapsed, 1),
                        "requests": total,
                        "errors": errors,
                        "rps": round(rps, 2),
                    },
                    ensure_ascii=False,
                ),
                flush=True,
            )

    progress_thread = threading.Thread(target=progress_reporter, daemon=True)
    progress_thread.start()
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        futures = [pool.submit(worker) for _ in range(args.concurrency)]
        for f in concurrent.futures.as_completed(futures):
            f.result()
    duration = time.perf_counter() - started
    summary = summarize(results, duration)
    json_path, md_path = write_report(args.out_dir, summary, results, args)
    print(json.dumps(summary["latency"], ensure_ascii=False, indent=2))
    print(f"report_json={json_path}")
    print(f"report_md={md_path}")
    return 1 if summary["latency"]["errors"] else 0


if __name__ == "__main__":
    sys.exit(main())
