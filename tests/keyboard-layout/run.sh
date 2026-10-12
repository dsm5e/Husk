#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
xcrun swiftc "$ROOT/src/app/Husk/KeyboardLayout.swift" "$ROOT/tests/keyboard-layout/main.swift" -o "$TMP/keyboard-layout"
"$TMP/keyboard-layout"
