#!/usr/bin/env bash
# Double-clickable Mac Account Manager (Terminal / Finder)
cd "$(dirname "$0")" || exit 1
exec ./add-account.sh
