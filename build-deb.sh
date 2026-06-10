#!/usr/bin/env bash
#
# Build the Calendar Journal desktop app as an installable .deb (with a bundled
# Java runtime) using jpackage.
#
# Usage:   ./build-deb.sh
# Install: sudo apt install ./packaging/dist/calendar-journal_<version>_amd64.deb
# Remove:  sudo apt remove calendar-journal
#
set -euo pipefail

VERSION="1.0.0"
cd "$(dirname "$0")"

echo "==> Building jar with Maven"
mvn -q package

echo "==> Staging jar"
rm -rf packaging/stage packaging/dist
mkdir -p packaging/stage packaging/dist
cp target/calendar-journal.jar packaging/stage/

echo "==> Running jpackage (.deb, bundled runtime)"
jpackage \
  --type deb \
  --name "Calendar Journal" \
  --linux-package-name calendar-journal \
  --app-version "$VERSION" \
  --description "Calendar journal — click a day to write and save an entry (SQLite)." \
  --vendor "Palak" \
  --input packaging/stage \
  --main-jar calendar-journal.jar \
  --main-class com.journal.Main \
  --icon packaging/icon.png \
  --linux-shortcut \
  --linux-menu-group "Office" \
  --dest packaging/dist

echo "==> Done:"
ls -1 packaging/dist/*.deb
echo
echo "Install with:  sudo apt install ./$(ls packaging/dist/*.deb)"
