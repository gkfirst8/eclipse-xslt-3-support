package nl.indi.eclipse.xslt3.ui.text;

import org.eclipse.jface.text.rules.IWordDetector;

public class XsltWordDetector implements IWordDetector {

    @Override
    public boolean isWordStart(char character) {
        return Character.isLetter(character);
    }

    @Override
    public boolean isWordPart(char character) {
        return Character.isLetterOrDigit(character) || character == ':' || character == '-';
    }
}

