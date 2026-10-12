#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
JAVAC="${HUSK_JDK:+$HUSK_JDK/bin/}javac"
JAVA="${HUSK_JDK:+$HUSK_JDK/bin/}java"
python3 - "$ROOT" "$TMP" <<'PY'
from pathlib import Path
import sys
root, tmp = map(Path, sys.argv[1:])
framework = root / 'src/java-framework'
for name, directories in [('stubs', [framework / 'stubs']),
                          ('sources', [framework / 'src', framework / 'gen-src', root / 'tests/java-framework'])]:
    paths = sorted(p for d in directories for p in d.rglob('*.java'))
    (tmp / (name + '.args')).write_text('\n'.join('"' + str(p) + '"' for p in paths))
PY
"$JAVAC" -nowarn --release 8 -encoding UTF-8 -d "$TMP/stubs" @"$TMP/stubs.args"
"$JAVAC" -nowarn --release 8 -encoding UTF-8 -cp "$TMP/stubs" -d "$TMP/classes" @"$TMP/sources.args"
"$JAVA" -cp "$TMP/classes:$TMP/stubs" ContextWrapperTest
"$JAVA" -cp "$TMP/classes:$TMP/stubs" husk.KeyboardDismissGestureTest
"$JAVA" -cp "$TMP/classes:$TMP/stubs" husk.TextEditMenuTest
