package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EntryTemplateTest {

    @Test
    void everyTemplateHasNameAndBody() {
        for (EntryTemplate t : EntryTemplate.values()) {
            assertFalse(t.displayName().isBlank());
            assertFalse(t.body().isBlank());
        }
    }

    @Test
    void displayNamesAreUnique() {
        long distinct = Arrays.stream(EntryTemplate.values())
                .map(EntryTemplate::displayName).distinct().count();
        assertEquals(EntryTemplate.values().length, distinct);
    }

    @Test
    void bodiesRenderAsMarkdown() {
        MarkdownRenderer renderer = new MarkdownRenderer();
        for (EntryTemplate t : EntryTemplate.values()) {
            assertNotNull(renderer.toHtml(t.body()));
        }
    }
}
