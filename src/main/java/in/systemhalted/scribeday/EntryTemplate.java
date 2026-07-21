package in.systemhalted.scribeday;

/**
 * Built-in Markdown starting points for an entry, offered by the editor's
 * Template menu to take the edge off a blank page.
 */
public enum EntryTemplate {
    GRATITUDE("Gratitude", """
            ## Grateful for

            1.
            2.
            3.

            ## Today's highlight

            """),
    DAILY_STANDUP("Daily standup", """
            ## Yesterday

            ## Today

            ## Blockers

            """),
    FREE_WRITE("Free write", """
            ## On my mind

            """);

    private final String displayName;
    private final String body;

    EntryTemplate(String displayName, String body) {
        this.displayName = displayName;
        this.body = body;
    }

    /** Human-readable name for the Template menu. */
    public String displayName() {
        return displayName;
    }

    /** The Markdown skeleton inserted into the editor. */
    public String body() {
        return body;
    }
}
