#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAIN_CLASSES="$ROOT_DIR/build/classes"
TEST_CLASSES="$ROOT_DIR/build/test-classes"

"$ROOT_DIR/build.sh"
mkdir -p "$TEST_CLASSES"

find "$ROOT_DIR/test" -name '*.java' -print0 |
  xargs -0 javac -Xlint:all -cp "$MAIN_CLASSES" -d "$TEST_CLASSES"

java -ea -cp "$MAIN_CLASSES:$TEST_CLASSES" com.smartbuilding.SmartBuildingSystemTest

SMOKE_DIR="$(mktemp -d)"
(
  cd "$SMOKE_DIR"
  printf 'invalid\n14\n' |
    java -cp "$MAIN_CLASSES" com.smartbuilding.SmartBuildingApp >/dev/null
)

echo "Console smoke test passed"
