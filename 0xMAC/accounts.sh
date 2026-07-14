#!/usr/bin/env bash
# 0xMAC Account Manager (CLI) — same ~/.oxrl./accounts JSON as Windows.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
exec python3 "$HERE/lib/ox_identity.py" "$@"
