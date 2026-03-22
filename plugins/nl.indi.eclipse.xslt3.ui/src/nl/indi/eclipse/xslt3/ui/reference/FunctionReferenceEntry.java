package nl.indi.eclipse.xslt3.ui.reference;

public record FunctionReferenceEntry(
    String lookupKey,
    String title,
    String signature,
    String summary,
    String details
) {
}
