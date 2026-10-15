#!/usr/bin/env bash
# Preserve real dashboards' data before disposable benchmark containers stop.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p run-output
compose=(docker compose -f loadtests/compose.runner.yml)

"${compose[@]}" exec -T influxdb influx backup /tmp/benchmark-backup \
  --host http://localhost:8086 --token course7-local-benchmark-token
"${compose[@]}" exec -T influxdb tar -czf - -C /tmp benchmark-backup \
  > run-output/influx-backup.tgz

curl --fail --silent --show-error -X POST \
  http://127.0.0.1:19490/api/v1/admin/tsdb/snapshot \
  > run-output/prometheus-snapshot.json
snapshot=$(python3 -c 'import json; print(json.load(open("run-output/prometheus-snapshot.json"))["data"]["name"])')
[[ "$snapshot" =~ ^[A-Za-z0-9_-]+$ ]]
"${compose[@]}" exec -T prometheus tar -czf - -C /prometheus/snapshots "$snapshot" \
  > run-output/prometheus-snapshot.tgz
date -u +%FT%TZ > run-output/monitoring-exported-at.txt
