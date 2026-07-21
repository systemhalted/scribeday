package in.systemhalted.scribeday;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javafx.scene.Scene;

/**
 * A UI theme, backed by one or more CSS stylesheets on the classpath. Dark layers
 * its overrides on top of the shared base stylesheet.
 */
public enum Theme {
    LIGHT(List.of("/in/systemhalted/scribeday/app.css")),
    DARK(List.of("/in/systemhalted/scribeday/app.css", "/in/systemhalted/scribeday/dark.css"));

    private final List<String> stylesheets;

    Theme(List<String> stylesheets) {
        this.stylesheets = stylesheets;
    }

    /** Replace the scene's stylesheets with this theme's. */
    public void applyTo(Scene scene) {
        List<String> urls = stylesheets.stream()
                .map(Theme.class::getResource)
                .filter(Objects::nonNull)
                .map(java.net.URL::toExternalForm)
                .toList();
        scene.getStylesheets().setAll(urls);
    }

    /** Parse a stored theme name, falling back when null or unrecognized. */
    public static Theme fromName(String name, Theme fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return Theme.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
