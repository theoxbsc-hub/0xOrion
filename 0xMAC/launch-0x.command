#!/usr/bin/env bash
# Double-clickable Mac launcher (Terminal.app)
# Keep window open on failure so errors are visible.
cd "$(dirname "$0")" || exit 1
chmod +x ./*.sh ./*.command 2>/dev/null || true
./launch-0x.sh "$@"
status=$?
if [[ $status -ne 0 ]]; then
  echo
  echo "Launcher exited with code $status."
  read -r -p "Press Enter to close…"
fi
exit "$status"
