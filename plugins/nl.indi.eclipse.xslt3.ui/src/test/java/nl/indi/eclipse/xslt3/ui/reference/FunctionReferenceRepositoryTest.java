package nl.indi.eclipse.xslt3.ui.reference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FunctionReferenceRepositoryTest {

    @Test
    void loadsBundledFunctionEntries() {
        FunctionReferenceRepository repository = FunctionReferenceRepository.getInstance();

        assertTrue(repository.findByLookupKey("fn:normalize-space").isPresent());
        assertTrue(repository.findByLookupKey("map:get").isPresent());
        assertFalse(repository.getStyleSheet().isBlank());
    }
}
