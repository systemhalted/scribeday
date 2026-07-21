package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MoodTest {

    @Test
    void fromNameParsesEachValueCaseInsensitively() {
        assertEquals(Mood.GREAT, Mood.fromName("GREAT"));
        assertEquals(Mood.GOOD, Mood.fromName("good"));
        assertEquals(Mood.OK, Mood.fromName(" Ok "));
    }

    @Test
    void fromNameReturnsNullForNullOrUnknown() {
        assertNull(Mood.fromName(null));
        assertNull(Mood.fromName(""));
        assertNull(Mood.fromName("ecstatic"));
    }

    @Test
    void everyMoodHasGlyphDisplayNameAndStyleClass() {
        for (Mood m : Mood.values()) {
            assertFalse(m.glyph().isBlank());
            assertFalse(m.displayName().isBlank());
            assertFalse(m.styleClass().isBlank());
        }
    }

    @Test
    void styleClassesAreUnique() {
        long distinct = java.util.Arrays.stream(Mood.values()).map(Mood::styleClass).distinct().count();
        assertEquals(Mood.values().length, distinct);
    }
}
