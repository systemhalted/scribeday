# Build the ScribeDay desktop app as a Windows .msi (with a bundled Java +
# JavaFX runtime) using jpackage. Run this ON Windows in PowerShell.
#
# Usage:    .\build-windows.ps1
# Requires: JDK 21 (jpackage) and the WiX Toolset v3 on PATH (the .msi backend).
#           Install WiX with:  choco install wixtoolset
# Note:     the produced .msi is unsigned; SmartScreen may warn on first run.

$ErrorActionPreference = "Stop"
$Version = "1.0.0"
Set-Location -Path $PSScriptRoot

Write-Host "==> Building jar (javafx.platform=win)"
mvn -q package -DskipTests "-Djavafx.platform=win"

Write-Host "==> Staging jar"
Remove-Item -Recurse -Force packaging\stage, packaging\dist -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force packaging\stage, packaging\dist | Out-Null
Copy-Item target\scribeday.jar packaging\stage\

Write-Host "==> Running jpackage (.msi)"
jpackage `
  --type msi `
  --name "ScribeDay" `
  --app-version $Version `
  --description "ScribeDay - click a day to write and save an entry (SQLite)." `
  --vendor "Palak" `
  --input packaging\stage `
  --main-jar scribeday.jar `
  --main-class in.systemhalted.scribeday.Launcher `
  --win-menu --win-shortcut --win-dir-chooser `
  --dest packaging\dist

Write-Host "==> Done:"
Get-ChildItem packaging\dist\*.msi | Select-Object -ExpandProperty Name
