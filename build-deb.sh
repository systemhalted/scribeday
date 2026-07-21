#!/usr/bin/env bash
#
# Build the ScribeDay desktop app as an installable .deb (with a bundled
# Java runtime) using jpackage.
#
# Usage:   ./build-deb.sh
# Install: sudo apt install ./packaging/dist/scribeday_<version>_amd64.deb
# Remove:  sudo apt remove scribeday
#
set -euo pipefail

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
cd "$(dirname "$0")"

echo "==> Building jar with Maven"
mvn -q package

echo "==> Staging jar"
rm -rf packaging/stage packaging/dist
mkdir -p packaging/stage packaging/dist
cp target/scribeday.jar packaging/stage/

echo "==> Running jpackage (.deb, bundled runtime)"
jpackage \
  --type deb \
  --name "ScribeDay" \
  --linux-package-name scribeday \
  --app-version "$VERSION" \
  --description "ScribeDay — click a day to write and save an entry (SQLite)." \
  --vendor "Palak" \
  --input packaging/stage \
  --main-jar scribeday.jar \
  --main-class in.systemhalted.scribeday.Launcher \
  --icon packaging/icon.png \
  --linux-shortcut \
  --linux-menu-group "Office" \
  --dest packaging/dist

echo "==> Done:"
ls -1 packaging/dist/*.deb
echo
echo "Install with:  sudo apt install ./$(ls packaging/dist/*.deb)"
