package nl.indi.eclipse.xslt3.ui.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

public class XsltCodeScanner extends RuleBasedScanner {

    public XsltCodeScanner(ColorManager colorManager) {
        IToken defaultToken = token(colorManager, XsltColorConstants.DEFAULT, SWT.NORMAL);
        IToken commentToken = token(colorManager, XsltColorConstants.COMMENT, SWT.NORMAL);
        IToken stringToken = token(colorManager, XsltColorConstants.STRING, SWT.NORMAL);
        IToken xmlTagToken = token(colorManager, XsltColorConstants.XML_TAG, SWT.BOLD);
        IToken xsltTagToken = token(colorManager, XsltColorConstants.XSLT_TAG, SWT.BOLD);
        IToken attributeToken = token(colorManager, XsltColorConstants.ATTRIBUTE, SWT.NORMAL);

        WordRule attributeRule = new WordRule(new XsltWordDetector(), Token.UNDEFINED, true);
        for (String attributeName : attributeNames()) {
            attributeRule.addWord(attributeName, attributeToken);
        }

        setRules(new IRule[] {
            new MultiLineRule("<!--", "-->", commentToken),
            new SingleLineRule("\"", "\"", stringToken, '\\'),
            new SingleLineRule("'", "'", stringToken, '\\'),
            new XsltTagRule(xsltTagToken, true),
            new XsltTagRule(xmlTagToken, false),
            attributeRule,
            new WhitespaceRule(character -> Character.isWhitespace(character))
        });
        setDefaultReturnToken(defaultToken);
    }

    private IToken token(ColorManager colorManager, org.eclipse.swt.graphics.RGB rgb, int style) {
        return new Token(new TextAttribute(colorManager.getColor(rgb), null, style));
    }

    private Set<String> attributeNames() {
        return new HashSet<>(Arrays.asList(
            "as",
            "expand-text",
            "href",
            "match",
            "mode",
            "name",
            "namespace",
            "on-no-match",
            "package-version",
            "select",
            "test",
            "use-when",
            "version"
        ));
    }
}

