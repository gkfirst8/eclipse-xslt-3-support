package nl.indi.eclipse.xslt3.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class XsltContentAssistProcessor implements IContentAssistProcessor {

    private static final String[] ELEMENTS = {
        "xsl:stylesheet",
        "xsl:package",
        "xsl:template",
        "xsl:function",
        "xsl:variable",
        "xsl:param",
        "xsl:mode",
        "xsl:iterate",
        "xsl:next-iteration",
        "xsl:on-completion",
        "xsl:try",
        "xsl:catch",
        "xsl:accumulator",
        "xsl:include",
        "xsl:import",
        "xsl:value-of"
    };

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        IDocument document = viewer.getDocument();
        String prefix = extractPrefix(document, offset);
        List<ICompletionProposal> proposals = new ArrayList<>();

        for (String element : ELEMENTS) {
            if (prefix.isBlank() || element.startsWith(prefix)) {
                proposals.add(new CompletionProposal(
                    element,
                    offset - prefix.length(),
                    prefix.length(),
                    element.length()
                ));
            }
        }

        return proposals.toArray(ICompletionProposal[]::new);
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return new IContextInformation[0];
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] {'<', ':'};
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return new char[0];
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    private String extractPrefix(IDocument document, int offset) {
        try {
            int start = offset;
            while (start > 0) {
                char current = document.getChar(start - 1);
                if (Character.isLetterOrDigit(current) || current == ':' || current == '-') {
                    start--;
                    continue;
                }
                break;
            }
            return document.get(start, offset - start);
        } catch (BadLocationException exception) {
            return "";
        }
    }
}

