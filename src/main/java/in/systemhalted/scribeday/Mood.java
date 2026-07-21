package in.systemhalted.scribeday;

import java.util.Locale;

/**
 * How the day felt, on a five-point scale. Stored per entry (nullable — mood is
 * optional) and shown on the calendar in place of the plain entry dot. Glyphs are
 * plain text; each mood gets its color from a {@code mood-*} CSS class.
 */
public enum Mood {
    GREAT("Great"),
    GOOD("Good"),
    OK("OK"),
    LOW("Low"),
    BAD("Bad");

    private final String displayName;

    Mood(String displayName) {
        this.displayName = displayName;
    }

    /** Human-readable name for pickers and tooltips. */
    public String displayName() {
        return displayName;
    }

    /** Text marker shown on calendar day cells (colored via {@link #styleClass()}). */
    public String glyph() {
        return "●";
    }

    /** CSS style class carrying this mood's color, e.g. {@code mood-great}. */
    public String styleClass() {
        return "mood-" + name().toLowerCase(Locale.ROOT);
    }

    /** Parse a stored mood name; {@code null} when the input is null, blank, or unrecognized. */
    public static Mood fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return Mood.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
