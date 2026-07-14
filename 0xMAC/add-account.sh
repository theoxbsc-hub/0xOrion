#!/usr/bin/env bash
# 0xMAC Account Manager — GUI by default (same role as Windows Add-Account.bat).
# Usage:
#   ./add-account.sh          → GUI
#   ./add-account.sh --cli    → terminal menu
#   ./accounts.sh list|add…   → CLI commands
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
chmod +x "$HERE"/*.sh "$HERE"/*.command 2>/dev/null || true

if [[ "${1:-}" == "--cli" || "${1:-}" == "cli" ]]; then
  shift || true
  exec python3 "$HERE/lib/ox_identity.py" menu "$@"
fi

# Prefer GUI; fall back to CLI if Tk is missing
if python3 -c "import tkinter" 2>/dev/null; then
  exec python3 "$HERE/lib/account_manager_gui.py" "$@"
fi

echo "tkinter not found — opening CLI menu instead."
echo "Install:  brew install python-tk@3.12   (or use system python3 that includes Tk)"
echo
exec python3 "$HERE/lib/ox_identity.py" menu "$@"
