package nl.indi.eclipse.xslt3.ui.reference;

import java.util.Locale;
import java.util.Optional;

public final class FunctionReferenceResolver {

    public Optional<String> resolveLookupKey(String documentText, int offset) {
        if (documentText == null || documentText.isBlank()) {
            return Optional.empty();
        }

        for (int candidateOffset : candidateOffsets(documentText, offset)) {
            Optional<String> lookupKey = resolveAt(documentText, candidateOffset);
            if (lookupKey.isPresent()) {
                return lookupKey;
            }
        }

        return Optional.empty();
    }

    private int[] candidateOffsets(String documentText, int offset) {
        int boundedOffset = Math.max(0, Math.min(offset, documentText.length()));
        int previousOffset = Math.max(0, boundedOffset - 1);
        if (previousOffset == boundedOffset) {
            return new int[] {boundedOffset};
        }
        return new int[] {boundedOffset, previousOffset};
    }

    private Optional<String> resolveAt(String documentText, int offset) {
        int tokenIndex = tokenIndexAt(documentText, offset);
        if (tokenIndex < 0) {
            return Optional.empty();
        }

        int start = tokenIndex;
        while (start > 0 && isTokenCharacter(documentText.charAt(start - 1))) {
            start--;
        }

        int end = tokenIndex + 1;
        while (end < documentText.length() && isTokenCharacter(documentText.charAt(end))) {
            end++;
        }

        String token = documentText.substring(start, end);
        if (!containsLetter(token)) {
            return Optional.empty();
        }

        if (!isFunctionCall(documentText, end)) {
            return Optional.empty();
        }

        if (isRejectedContext(documentText, start)) {
            return Optional.empty();
        }

        return Optional.of(normalizeLookupKey(token));
    }

    private int tokenIndexAt(String documentText, int offset) {
        if (offset < documentText.length() && isTokenCharacter(documentText.charAt(offset))) {
            return offset;
        }
        if (offset > 0 && isTokenCharacter(documentText.charAt(offset - 1))) {
            return offset - 1;
        }
        return -1;
    }

    private boolean isFunctionCall(String documentText, int tokenEnd) {
        int index = tokenEnd;
        while (index < documentText.length() && Character.isWhitespace(documentText.charAt(index))) {
            index++;
        }
        return index < documentText.length() && documentText.charAt(index) == '(';
    }

    private boolean isRejectedContext(String documentText, int tokenStart) {
        int index = tokenStart - 1;
        while (index >= 0 && Character.isWhitespace(documentText.charAt(index))) {
            index--;
        }
        if (index < 0) {
            return false;
        }

        char previous = documentText.charAt(index);
        return previous == '<'
            || previous == '/'
            || previous == '@'
            || previous == '$';
    }

    private boolean isTokenCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == ':' || character == '-' || character == '_';
    }

    private boolean containsLetter(String token) {
        for (int index = 0; index < token.length(); index++) {
            if (Character.isLetter(token.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeLookupKey(String token) {
        String normalized = token.toLowerCase(Locale.ROOT);
        if (normalized.contains(":")) {
            return normalized;
        }
        return "fn:" + normalized;
    }
}
