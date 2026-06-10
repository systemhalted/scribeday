package com.journal;

/**
 * Plain entry point that does not extend {@code javafx.application.Application}.
 *
 * <p>Launching a JavaFX {@code Application} subclass directly from a fat jar
 * trips the "missing JavaFX runtime components" check. Routing through this
 * non-Application class avoids that, so {@code java -jar} and the jpackage
 * launcher both work without a module path.
 */
public final class Launcher {
    public static void main(String[] args) {
        JournalApp.main(args);
    }
}
