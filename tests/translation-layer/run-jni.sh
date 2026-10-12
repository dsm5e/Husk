#!/bin/sh
# Host regression for the JNI fallback, on Apple Silicon macOS.
set -eu
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
SRC="$ROOT/src/translation-layer-next"
cc -std=gnu11 -O1 -g -I"$SRC" -Wl,-dead_strip \
    "$ROOT/tests/translation-layer/jni_missing_class_test.c" "$SRC/husk-tl-jni.c" \
    -o "$OUT/jni_missing_class_test"
"$OUT/jni_missing_class_test"
