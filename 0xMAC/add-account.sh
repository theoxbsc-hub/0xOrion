#!/usr/bin/env bash
# 0xMAC Account Manager — GUI by default (same role as Windows Add-Account.bat).
# Usage:
#   ./add-account.sh          → GUI
#   ./add-account.sh --cli    → terminal menu
#   ./accounts.sh list|add…   → CLI commands
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
chmod +x "$HERE"/*.sh "$HERE"/*.command 2>/dev/null || true

# Prefer Homebrew Python+Tk — Apple CLT/system Tk aborts on some Sequoia builds
# ("macOS 15 (1507) or later required…") and the .command window closes instantly.
pick_python() {
  local cand
  for cand in \
    /opt/homebrew/bin/python3.12 \
    /opt/homebrew/bin/python3.13 \
    /opt/homebrew/bin/python3.11 \
    /opt/homebrew/bin/python3 \
    /usr/local/bin/python3.12 \
    /usr/local/bin/python3 \
    python3
  do
    command -v "$cand" >/dev/null 2>&1 || [[ -x "$cand" ]] || continue
    [[ -x "$cand" ]] || cand="$(command -v "$cand")"
    # Must be able to CREATE a Tk window, not merely import the module
    if "$cand" - <<'PY' >/dev/null 2>&1
import tkinter as tk
r = tk.Tk()
r.withdraw()
r.destroy()
PY
    then
      echo "$cand"
      return 0
    fi
  done
  return 1
}

PY=""
if PY="$(pick_python)"; then
  :
else
  PY="$(command -v python3 || true)"
fi

if [[ "${1:-}" == "--cli" || "${1:-}" == "cli" ]]; then
  shift || true
  exec "$PY" "$HERE/lib/ox_identity.py" menu "$@"
fi

if [[ -n "$PY" ]] && "$PY" - <<'PY' >/dev/null 2>&1
import tkinter as tk
r = tk.Tk()
r.withdraw()
r.destroy()
PY
then
  exec "$PY" "$HERE/lib/account_manager_gui.py" "$@"
fi

echo "============================================================"
echo "  Tk GUI unavailable on this Python — using CLI instead."
echo "============================================================"
echo
echo "  Apple/system Tk often crashes on macOS Sequoia."
echo "  Fix (recommended):"
echo "    brew install python-tk@3.12"
echo "  then re-run:  ./add-account.sh"
echo
echo "  Or keep using CLI:  ./add-account.sh --cli"
echo
exec "${PY:-python3}" "$HERE/lib/ox_identity.py" menu "$@"
