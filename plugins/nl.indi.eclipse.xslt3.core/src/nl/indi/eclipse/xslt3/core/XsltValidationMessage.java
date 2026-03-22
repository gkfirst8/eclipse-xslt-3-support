package nl.indi.eclipse.xslt3.core;

public record XsltValidationMessage(
    int severity,
    String message,
    Integer lineNumber,
    Integer columnNumber,
    String systemId
) {
}

