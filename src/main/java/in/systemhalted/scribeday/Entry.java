package in.systemhalted.scribeday;

/**
 * A journal entry's editable fields. {@code title} and {@code mood} may be {@code null}.
 */
public record Entry(String title, String content, Mood mood) {

    /** An entry with no mood recorded. */
    public Entry(String title, String content) {
        this(title, content, null);
    }
}
