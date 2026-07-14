#!/usr/bin/env bash
# ============================================================
#  0xMAC build helper
#
#  Full client builds normally run on Windows (build.ps1) because
#  they start from the vendor Orion client_patched.jar.
#
#  On Mac you typically:
#    1) Copy 0x-client.jar (+ agent) from a Windows build into ../dist/
#    2) Or set SOURCE_JAR and run this script if the vendor jar is available
# ============================================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
DIST="$ROOT/dist"
SOURCE_JAR="${SOURCE_JAR:-}"

echo "==> 0xMAC build helper"
echo "    repo root: $ROOT"

if [[ -z "$SOURCE_JAR" ]]; then
  for cand in \
    "$ROOT/vendor/client_patched.jar" \
    "$HOME/Desktop/Orion x64/client_patched.jar" \
    "$HOME/Downloads/client_patched.jar"
  do
    if [[ -f "$cand" ]]; then
      SOURCE_JAR="$cand"
      break
    fi
  done
fi

if [[ -z "$SOURCE_JAR" || ! -f "$SOURCE_JAR" ]]; then
  cat <<'EOF'

No vendor client_patched.jar found.

Mac workflow (recommended):
  1. On Windows:  powershell -File build.ps1
  2. Copy to this Mac:
       dist/0x-client.jar
       dist/ox-incognito-agent.jar
  3. From 0xMAC:  ./launch-0x.sh

Optional: place the vendor jar at vendor/client_patched.jar and re-run
with SOURCE_JAR set, or install PowerShell Core and run ../build.ps1.

Also building the agent (does not need the vendor jar)...
EOF
  "$HERE/build-agent.sh"
  exit 0
fi

if ! command -v pwsh >/dev/null 2>&1; then
  echo "PowerShell Core (pwsh) not found — cannot run build.ps1 on Mac."
  echo "Install: brew install --cask powershell"
  echo "Falling back to agent-only build."
  "$HERE/build-agent.sh"
  exit 0
fi

echo "==> Running Windows build.ps1 via pwsh (SOURCE_JAR=$SOURCE_JAR)"
pwsh -NoProfile -File "$ROOT/build.ps1" -SourceJar "$SOURCE_JAR"
"$HERE/build-agent.sh"
echo "Done. Launch with: $HERE/launch-0x.sh"
