"""Run the unchanged JMeter stages, one variant per dedicated Linux runner."""
import collections
from array import array
import csv
import gzip
import hashlib
import json
import math
import os
from pathlib import Path
import platform
import re
import subprocess
import tarfile
import threading
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "results"
OUT.mkdir(exist_ok=True)
LETTER = os.environ.get("VARIANT", "A")
MODULE, PORT, MANAGEMENT = {
    "A": ("variant-a-jersey", 19401, 19501),
    "C": ("variant-c-mvc", 19402, 19502),
    "D": ("variant-d-data-rest", 19403, 19503),
}[LETTER]
SMOKE = os.environ.get("SMOKE", "false").lower() == "true"
STOP = threading.Event()
SCENARIOS = ["READ-heavy", "JOIN-filter", "MIXED", "HEAVY-body"]
ARCHIVE_SHA512 = "5978a1a35edb5a7d428e270564ff49d2b1b257a65e17a759d259a9283fc17093e522fe46f474a043864aea6910683486340706d745fcdf3db1505fd71e689083"


def request(path, method="GET", body=None):
    data = None if body is None else json.dumps(body).encode()
    query = urllib.request.Request(f"http://127.0.0.1:{PORT}{path}", data=data,
        method=method, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(query, timeout=30) as response:
        content = response.read().decode()
        return {"status": response.status, "body": json.loads(content) if content else None}


def validate():
    results = []
    category = request("/categories", "POST", {"code": "RUNNER-CHECK", "name": "Vérification"})
    assert category["status"] == 201, category
    results.append(category)
    cid = category["body"]["id"]
    payload = {"sku": "RUNNER-CHECK", "name": "Clavier", "price": 25, "stock": 10,
        "description": "x" * 850, "categoryId": cid,
        "category": f"http://localhost:{PORT}/categories/{cid}"}
    item = request("/items", "POST", payload)
    assert item["status"] == 201, item
    results.append(item)
    iid = item["body"]["id"]
    for path in [f"/items/{iid}", f"/items?categoryId={cid}&size=50", f"/categories/{cid}/items?size=50"]:
        result = request(path)
        assert result["status"] == 200, result
        results.append(result)
    payload["price"] = 30
    result = request(f"/items/{iid}", "PUT", payload)
    assert result["status"] in (200, 204), result
    results.append(result)
    for path in [f"/items/{iid}", f"/categories/{cid}"]:
        result = request(path, "DELETE")
        assert result["status"] in (200, 204), result
        results.append(result)
    (OUT / "http-validation.json").write_text(json.dumps(results, indent=2), encoding="utf-8")


def monitor():
    with (OUT / "jvm-samples.csv").open("w", newline="") as output:
        writer = csv.writer(output)
        writer.writerow(["timestamp", "variant", "cpu", "heap_bytes", "gc_pause_seconds_total", "threads", "hikari_active", "hikari_max"])
        while not STOP.is_set():
            try:
                with urllib.request.urlopen(f"http://127.0.0.1:{MANAGEMENT}/actuator/prometheus", timeout=5) as response:
                    text = response.read().decode()
                metrics = {}
                for line in text.splitlines():
                    if line and not line.startswith("#"):
                        key, value = line.rsplit(" ", 1)
                        metrics[key] = float(value)
                def total(prefix, contains=""):
                    return sum(v for k, v in metrics.items() if k.startswith(prefix) and contains in k)
                writer.writerow([time.time(), LETTER, total("process_cpu_usage"),
                    total("jvm_memory_used_bytes{", 'area="heap"'), total("jvm_gc_pause_seconds_sum"),
                    total("jvm_threads_live_threads"), total("hikaricp_connections_active"), total("hikaricp_connections_max")])
                output.flush()
            except (OSError, ValueError) as error:
                print("Metrics temporarily unavailable:", error, flush=True)
            STOP.wait(5)


def percentile(values, fraction):
    return values[max(0, math.ceil(len(values) * fraction) - 1)]


def summarize(path, scenario, writer):
    groups = {}
    with path.open(newline="") as source:
        for sample in csv.DictReader(source):
            stage = re.search(r"(\d+) utilisateurs", sample["threadName"])
            users = stage.group(1) if stage else "unknown"
            elapsed = int(sample["elapsed"])
            started = int(sample["timeStamp"])
            ended = started + elapsed
            for key in [(users, "ALL"), (users, sample["label"])]:
                if key not in groups:
                    groups[key] = {"durations": array("I"), "start": started,
                        "end": ended, "errors": 0, "total_ms": 0}
                group = groups[key]
                group["durations"].append(elapsed)
                group["start"] = min(group["start"], started)
                group["end"] = max(group["end"], ended)
                group["errors"] += sample["success"].lower() != "true"
                group["total_ms"] += elapsed
    assert groups, f"No actual samples: {path}"
    for (users, endpoint), group in groups.items():
        durations = sorted(group["durations"])
        count = len(durations)
        errors = group["errors"]
        writer.writerow([LETTER, scenario, users, endpoint, count, errors,
            round(100 * errors / count, 4), round(count * 1000 / max(1, group["end"] - group["start"]), 3),
            round(group["total_ms"] / count, 3), percentile(durations, .5),
            percentile(durations, .95), percentile(durations, .99), max(durations)])
    with path.open("rb") as source, gzip.open(str(path) + ".gz", "wb", compresslevel=6) as target:
        while chunk := source.read(1024 * 1024):
            target.write(chunk)
    path.unlink()


def main():
    archive = Path("/tmp/apache-jmeter-5.6.3.tgz")
    urllib.request.urlretrieve("https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz", archive)
    assert hashlib.sha512(archive.read_bytes()).hexdigest() == ARCHIVE_SHA512, "JMeter checksum mismatch"
    with tarfile.open(archive) as package:
        package.extractall("/tmp", filter="data")
    jmeter = "/tmp/apache-jmeter-5.6.3/bin/ApacheJMeter.jar"
    command = ["java", "-Xms64m", "-Xmx160m", "-XX:+UseG1GC", "-jar",
        f"{MODULE}/target/{MODULE}-1.0.0.jar", "--logging.level.root=WARN",
        "--logging.level.org.springframework=WARN", "--debug=false"]
    metadata = {"variant": LETTER, "mode": "smoke" if SMOKE else "full",
        "platform": platform.platform(), "visible_cpus": os.cpu_count(),
        "runtime_budget_bytes": 8 * 1024**3, "worker_limit_bytes": 5632 * 1024**2,
        "postgres_limit_bytes": 1024**3, "influx_limit_bytes": 1024**3,
        "prometheus_limit_bytes": 256 * 1024**2, "grafana_limit_bytes": 256 * 1024**2,
        "app_command": command, "jmeter_heap": "192m", "jmeter_sha512": ARCHIVE_SHA512,
        "github_run_id": os.environ.get("GITHUB_RUN_ID"), "started_unix": time.time()}
    (OUT / "environment.json").write_text(json.dumps(metadata, indent=2))
    with (OUT / "application.log").open("w") as app_log:
        application = subprocess.Popen(command, cwd=ROOT, stdout=app_log, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 150
            while True:
                try:
                    with urllib.request.urlopen(f"http://127.0.0.1:{MANAGEMENT}/actuator/health", timeout=3) as response:
                        assert json.load(response)["status"] == "UP"
                    break
                except (OSError, AssertionError):
                    if application.poll() is not None or time.monotonic() >= deadline:
                        raise RuntimeError("Application did not become healthy; see application.log")
                    time.sleep(2)
            validate()
            print(f"RUNNER_HEALTHY variant={LETTER} CRUD/filter/relation verified", flush=True)
            (OUT / "runner-healthy.json").write_text(json.dumps({"variant": LETTER, "timestamp": time.time(), "crud": "passed"}))
            monitor_thread = threading.Thread(target=monitor)
            monitor_thread.start()
            with (OUT / "summary.csv").open("w", newline="") as table:
                writer = csv.writer(table)
                writer.writerow(["variant", "scenario", "users", "endpoint", "requests", "errors", "error_pct", "throughput_rps", "mean_ms", "p50_ms", "p95_ms", "p99_ms", "max_ms"])
                for scenario in SCENARIOS:
                    result = OUT / f"{LETTER}-{scenario}.jtl"
                    if result.exists() or Path(str(result) + ".gz").exists():
                        raise RuntimeError(f"Refusing to overwrite previous samples: {result}")
                    args = ["java", "-Xmx192m", "-Xss512k", "-Djava.awt.headless=true", "-jar", jmeter,
                        "-n", "-t", f"loadtests/{scenario}.jmx", "-l", str(result),
                        "-j", str(OUT / f"{scenario}-jmeter.log"), f"-Jvariant={LETTER}", f"-Jport={PORT}"]
                    if SMOKE:
                        args += ["-Jduration=8", "-JrampUp=1", "-Jthreads1=2", "-Jthreads2=2", "-Jthreads3=2"]
                    print("START", LETTER, scenario, time.strftime("%H:%M:%S"), flush=True)
                    with (OUT / f"{scenario}-summary.log").open("w") as log:
                        subprocess.run(args, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT, check=True)
                    summarize(result, scenario, writer)
                    table.flush()
                    print("DONE", LETTER, scenario, time.strftime("%H:%M:%S"), flush=True)
            metadata["finished_unix"] = time.time()
            (OUT / "environment.json").write_text(json.dumps(metadata, indent=2))
        finally:
            STOP.set()
            application.terminate()
            application.wait(timeout=30)


if __name__ == "__main__":
    main()
