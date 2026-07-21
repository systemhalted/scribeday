# ScribeDay

A recreation of a college-era fun project: a desktop calendar where clicking a day
opens a notepad-style editor to write and save a journal entry. Entries live in a
local SQLite database — no cloud, no accounts, your journal stays on your machine.

The original was written in **Java Swing**; that first version is preserved on the
`childhood` branch. This branch is the **JavaFX rewrite** that grows it into a
polished, shippable product (see [ROADMAP.md](ROADMAP.md)).

## Features

- Month calendar with previous/next navigation; today is highlighted.
- Click a day to write; entries autosave, with title, word count, and live Markdown preview.
- Mood per entry, shown color-coded on the calendar; templates for a quick start.
  Moods run traffic-light style from green to red — Great (deep green), Good
  (light green), OK (amber), Low (orange), Bad (red); hover a calendar day or a
  mood button for the name.
- Writing streaks and entry count in a status bar; "On this day" resurfaces past years.
- Full-text search (Ctrl+F) and an agenda list view.
- Backup & restore, automatic weekly backups, and Markdown export.
- Opt-in daily reminder (fires while the app is open) and optional close-to-tray.
- One entry per day, stored in SQLite in a stable per-user location that survives reinstalls.

## Tech stack

- **Java 21**, **JavaFX 21** (UI), **SQLite** via `sqlite-jdbc` (storage)
- **Maven** build; **JUnit 5** tests
- Native installers via **jpackage**

## Run

From source (recommended for development):

```bash
mvn javafx:run
```

> The first run downloads the JavaFX plugin's dependencies (one-time), then opens
> the window and blocks until you close it.

Or build and run the packaged jar (what the installed app runs):

```bash
mvn package
java -jar target/scribeday.jar
```

## Test

```bash
mvn test
```

## Build a desktop installer

Each installer bundles its own Java + JavaFX runtime. `jpackage` only builds for
the OS it runs on, so build each on its native platform:

```bash
./build-deb.sh          # Linux  → packaging/dist/*.deb   (needs fakeroot)
./build-mac.sh          # macOS  → packaging/dist/*.dmg
.\build-windows.ps1     # Windows → packaging/dist/*.msi  (needs the WiX Toolset)
```

Install the Debian package and launch **ScribeDay** from your app menu:

```bash
sudo apt install ./packaging/dist/scribeday_1.0.0_amd64.deb
# uninstall: sudo apt remove scribeday
```

### Releases

Pushing a `v*` tag runs `.github/workflows/release.yml`, which builds installers
on native runners — Linux `.deb`, Windows `.msi`, and macOS `.dmg` for both Apple
Silicon and Intel — generates a `SHA256SUMS.txt`, and attaches everything to the
GitHub Release automatically.

## Where your data lives

The journal database is stored in a per-user data directory (created on first run):

| OS | Location |
|----|----------|
| Linux | `~/.local/share/ScribeDay/journal.db` (or `$XDG_DATA_HOME/ScribeDay`) |
| macOS | `~/Library/Application Support/ScribeDay/journal.db` |
| Windows | `%APPDATA%\ScribeDay\journal.db` |

A legacy `~/journal.db` from the earliest version is migrated automatically on first run.

## Project layout

```
src/main/java/in/systemhalted/scribeday/
  Launcher.java            # plain main() entry point (fat-jar / jpackage friendly)
  JournalApp.java          # JavaFX Application — wires DAO to the UI
  CalendarView.java        # month grid: navigation, entry dots, today highlight
  JournalEditorDialog.java # the editor: text area + Save / Delete / Cancel
  JournalDao.java          # all SQLite access; versioned schema + migrations + FTS
  AppPaths.java            # resolves the per-user data directory
src/test/java/in/systemhalted/scribeday/
  JournalDaoTest.java      # schema, migration, and search tests
build-deb.sh               # builds the .deb via jpackage
ROADMAP.md                 # product roadmap (v1.0 → v2.0)
```

## Roadmap

This is an evolving personal product. See [ROADMAP.md](ROADMAP.md) for the planned
releases — writing experience, search & organization, data safety, and reminders.
