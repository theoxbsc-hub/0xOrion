#!/usr/bin/env bash
# Double-clickable Mac Account Manager (Terminal / Finder)
# Keep window open on failure so errors are visible.
cd "$(dirname "$0")" || exit 1
chmod +x ./*.sh ./*.command 2>/dev/null || true
./add-account.sh "$@"
status=$?
if [[ $status -ne 0 ]]; then
  echo
  echo "Account Manager exited with code $status."
  read -r -p "Press Enter to close…"
fi
exit "$status"
