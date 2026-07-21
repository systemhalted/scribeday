#!/usr/bin/env bash
#
# Build the ScribeDay desktop app as a macOS .dmg (with a bundled Java +
# JavaFX runtime) using jpackage. Run this ON macOS.
#
# Usage:  ./build-mac.sh
# Note:   the produced .dmg is unsigned; macOS Gatekeeper will warn on first open
#         (right-click → Open, or sign/notarize separately for distribution).
#
set -euo pipefail

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
cd "$(dirname "$0")"

# Pick the JavaFX native classifier for this Mac's architecture.
if [ "$(uname -m)" = "arm64" ]; then
  PLATFORM="mac-aarch64"
else
  PLATFORM="mac"
fi

echo "==> Building jar (javafx.platform=$PLATFORM)"
mvn -q package -DskipTests "-Djavafx.platform=$PLATFORM"

echo "==> Staging jar"
rm -rf packaging/stage packaging/dist
mkdir -p packaging/stage packaging/dist
cp target/scribeday.jar packaging/stage/

echo "==> Running jpackage (.dmg)"
jpackage \
  --type dmg \
  --name "ScribeDay" \
  --app-version "$VERSION" \
  --description "ScribeDay — click a day to write and save an entry (SQLite)." \
  --vendor "Palak" \
  --input packaging/stage \
  --main-jar scribeday.jar \
  --main-class in.systemhalted.scribeday.Launcher \
  --dest packaging/dist

echo "==> Done:"
ls -1 packaging/dist/*.dmg
