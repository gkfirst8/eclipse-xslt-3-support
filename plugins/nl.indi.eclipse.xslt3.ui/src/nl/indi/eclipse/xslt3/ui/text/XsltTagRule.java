package nl.indi.eclipse.xslt3.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class XsltTagRule implements IRule {

    private final IToken token;
    private final boolean xsltOnly;

    public XsltTagRule(IToken token, boolean xsltOnly) {
        this.token = token;
        this.xsltOnly = xsltOnly;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        StringBuilder consumed = new StringBuilder();

        int current = scanner.read();
        if (current != '<') {
            unread(scanner, consumed.length() + 1);
            return Token.UNDEFINED;
        }
        consumed.append((char) current);

        current = scanner.read();
        if (current == ICharacterScanner.EOF) {
            unread(scanner, consumed.length());
            return Token.UNDEFINED;
        }
        consumed.append((char) current);

        if (current == '?' || current == '!') {
            unread(scanner, consumed.length());
            return Token.UNDEFINED;
        }

        if (current == '/') {
            current = scanner.read();
            if (current == ICharacterScanner.EOF) {
                unread(scanner, consumed.length());
                return Token.UNDEFINED;
            }
            consumed.append((char) current);
        }

        if (!isNameStart(current)) {
            unread(scanner, consumed.length());
            return Token.UNDEFINED;
        }

        StringBuilder name = new StringBuilder();
        name.append((char) current);
        while (true) {
            current = scanner.read();
            if (current == ICharacterScanner.EOF) {
                break;
            }
            if (!isNamePart(current)) {
                scanner.unread();
                break;
            }
            consumed.append((char) current);
            name.append((char) current);
        }

        boolean xsltName = name.toString().startsWith("xsl:");
        if (xsltOnly == xsltName) {
            return token;
        }

        unread(scanner, consumed.length());
        return Token.UNDEFINED;
    }

    private boolean isNameStart(int character) {
        return Character.isLetter(character) || character == '_' || character == ':';
    }

    private boolean isNamePart(int character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == ':' || character == '-';
    }

    private void unread(ICharacterScanner scanner, int count) {
        for (int index = 0; index < count; index++) {
            scanner.unread();
        }
    }
}

