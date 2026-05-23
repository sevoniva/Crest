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


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def admin_token(password: str) -> str:
    secret = hashlib.md5(password.encode()).hexdigest().encode()
    header = b64url(json.dumps({"alg": "HS256", "typ": "JWT"}, separators=(",", ":")).encode())
    payload = b64url(json.dumps({"uid": 1, "oid": 1}, separators=(",", ":")).encode())
    signing_input = f"{header}.{payload}".encode()
    sig = b64url(hmac.new(secret, signing_input, hashlib.sha256).digest())
    return f"{header}.{payload}.{sig}"


def request(base_url, case, token, timeout):
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
                if isinstance(parsed, dict) and "code" in parsed and parsed.get("code") not in (0, "0", None):
                    err = json.dumps(parsed, ensure_ascii=False)[:240]
    except urllib.error.HTTPError as e:
        status = e.code
        payload = e.read()
        size = len(payload)
        err = payload[:240].decode("utf-8", "ignore")
    except Exception as e:
        err = repr(e)
    elapsed_ms = (time.perf_counter() - started) * 1000
    return {
        "method": method,
        "path": path,
        "group": group,
        "status": status,
        "ok": 200 <= status < 400 and not err,
        "elapsed_ms": elapsed_ms,
        "size": size,
        "error": err,
    }


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
    args = parser.parse_args()

    token = admin_token(args.admin_password)
    weighted = []
    for case in DEFAULT_CASES:
        weighted.extend([case] * case[3])
    results = []
    lock = threading.Lock()
    stop_at = time.perf_counter() + args.duration

    def worker():
        local = []
        while time.perf_counter() < stop_at:
            local.append(request(args.base_url, random.choice(weighted), token, args.timeout))
        with lock:
            results.extend(local)

    started = time.perf_counter()
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
