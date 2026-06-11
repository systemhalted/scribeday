#!/usr/bin/env bash
#
# Build the Calendar Journal desktop app as a macOS .dmg (with a bundled Java +
# JavaFX runtime) using jpackage. Run this ON macOS.
#
# Usage:  ./build-mac.sh
# Note:   the produced .dmg is unsigned; macOS Gatekeeper will warn on first open
#         (right-click → Open, or sign/notarize separately for distribution).
#
set -euo pipefail

VERSION="1.0.0"
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
cp target/calendar-journal.jar packaging/stage/

echo "==> Running jpackage (.dmg)"
jpackage \
  --type dmg \
  --name "Calendar Journal" \
  --app-version "$VERSION" \
  --description "Calendar journal — click a day to write and save an entry (SQLite)." \
  --vendor "Palak" \
  --input packaging/stage \
  --main-jar calendar-journal.jar \
  --main-class com.journal.Launcher \
  --dest packaging/dist

echo "==> Done:"
ls -1 packaging/dist/*.dmg
