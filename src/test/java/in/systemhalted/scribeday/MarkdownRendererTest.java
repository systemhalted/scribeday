package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void rendersHeadings() {
        assertTrue(renderer.toHtml("# Hello").contains("<h1>Hello</h1>"));
    }

    @Test
    void rendersBoldText() {
        assertTrue(renderer.toHtml("**bold**").contains("<strong>bold</strong>"));
    }

    @Test
    void rendersBulletLists() {
        String html = renderer.toHtml("- one\n- two");
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>one</li>"));
    }

    @Test
    void emptyOrNullProducesEmptyOutput() {
        assertEquals("", renderer.toHtml(""));
        assertEquals("", renderer.toHtml(null));
    }
}
