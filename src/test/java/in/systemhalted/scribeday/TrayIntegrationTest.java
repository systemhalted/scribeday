package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class TrayIntegrationTest {

    @Test
    void availabilityCheckNeverThrows() {
        // Headless CI, missing desktop trays, and Wayland quirks must all
        // degrade to "not available", never to an exception.
        assertDoesNotThrow(TrayIntegration::isAvailable);
    }
}
