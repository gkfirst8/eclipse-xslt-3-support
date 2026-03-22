package nl.indi.eclipse.xslt3.ui.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class FunctionReferenceResolverTest {

    private final FunctionReferenceResolver resolver = new FunctionReferenceResolver();

    @Test
    void resolvesUnprefixedCoreFunctionCalls() {
        String text = "normalize-space('  too   much  space  ')";

        Optional<String> lookupKey = resolver.resolveLookupKey(text, text.indexOf("space"));

        assertEquals(Optional.of("fn:normalize-space"), lookupKey);
    }

    @Test
    void resolvesPrefixedLibraryFunctionCalls() {
        String text = "map:get($demo:lookup, 'beta')";

        Optional<String> lookupKey = resolver.resolveLookupKey(text, text.indexOf("get"));

        assertEquals(Optional.of("map:get"), lookupKey);
    }

    @Test
    void ignoresVariablesAndMarkupNames() {
        String variableText = "$items";
        String markupText = "<xsl:template match=\"/\">";

        assertTrue(resolver.resolveLookupKey(variableText, variableText.length()).isEmpty());
        assertTrue(resolver.resolveLookupKey(markupText, markupText.indexOf("template")).isEmpty());
    }

    @Test
    void resolvesCaretPositionsImmediatelyAfterTheFunctionName() {
        String text = "array:size(['a', 'b', 'c'])";
        int offset = text.indexOf('(');

        Optional<String> lookupKey = resolver.resolveLookupKey(text, offset);

        assertEquals(Optional.of("array:size"), lookupKey);
    }

    @Test
    void resolvesFunctionsAtTheStartOfQuotedXPathAttributes() {
        String text = "select=\"string-join($items ! string(.), '|')\"";

        Optional<String> lookupKey = resolver.resolveLookupKey(text, text.indexOf("join"));

        assertEquals(Optional.of("fn:string-join"), lookupKey);
    }
}
