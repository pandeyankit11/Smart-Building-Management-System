#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$ROOT_DIR/build/classes"

rm -rf "$ROOT_DIR/build"
mkdir -p "$BUILD_DIR"

find "$ROOT_DIR/src" -name '*.java' -print0 |
  xargs -0 javac -Xlint:all -d "$BUILD_DIR"

echo "Build successful: $BUILD_DIR"
