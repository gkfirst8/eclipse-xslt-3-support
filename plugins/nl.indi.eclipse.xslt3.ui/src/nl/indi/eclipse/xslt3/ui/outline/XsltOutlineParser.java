package nl.indi.eclipse.xslt3.ui.outline;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import nl.indi.eclipse.xslt3.ui.outline.XsltOutlineNode.Kind;

final class XsltOutlineParser {

    private static final String XSL_NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform";
    private static final Pattern DECLARATION_PATTERN = Pattern.compile(
        "<xsl:(template|function|variable|param|mode|accumulator|package)\\b([^>]*)"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile("(name|match)\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final List<Kind> CATEGORY_ORDER = List.of(
        Kind.MODES,
        Kind.TEMPLATES,
        Kind.FUNCTIONS,
        Kind.VARIABLES_AND_PARAMS,
        Kind.ACCUMULATORS,
        Kind.PACKAGES
    );

    public XsltOutlineNode parse(IDocument document) {
        String text = document == null ? "" : document.get();
        int documentLength = text.length();

        try {
            return parseXml(document, text, documentLength);
        } catch (XMLStreamException exception) {
            return parseFallback(document, text, documentLength);
        }
    }

    private XsltOutlineNode parseXml(IDocument document, String text, int documentLength) throws XMLStreamException {
        XsltOutlineNode root = XsltOutlineNode.root(documentLength);
        Map<Kind, XsltOutlineNode> categories = createCategories(documentLength);
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        disableExternalEntities(inputFactory);

        Deque<ElementFrame> frames = new ArrayDeque<>();
        try (StringReader reader = new StringReader(text)) {
            XMLStreamReader xml = inputFactory.createXMLStreamReader(reader);
            while (xml.hasNext()) {
                int event = xml.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    handleStartElement(document, documentLength, xml, categories, frames);
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    handleEndElement(document, documentLength, xml, frames);
                }
            }
        }

        attachNonEmptyCategories(root, categories);
        return root;
    }

    private void handleStartElement(
        IDocument document,
        int documentLength,
        XMLStreamReader xml,
        Map<Kind, XsltOutlineNode> categories,
        Deque<ElementFrame> frames
    ) {
        ElementFrame parentFrame = frames.peek();
        String namespaceUri = xml.getNamespaceURI();
        String localName = xml.getLocalName();
        Location location = xml.getLocation();
        int lineNumber = safeLineNumber(location);
        int startOffset = resolveStartOffset(document, documentLength, location, lineNumber);
        XsltOutlineNode outlineNode = null;

        if (isXsltElement(namespaceUri)) {
            if (isDocumentRootPackage(localName, parentFrame) || isTopLevelDeclaration(localName, parentFrame)) {
                outlineNode = createDeclarationNode(xml, declarationKind(localName), lineNumber, startOffset, documentLength);
                categories.get(categoryKind(outlineNode.kind())).addChild(outlineNode);
            } else if (isLocalDeclaration(localName)) {
                XsltOutlineNode container = enclosingCallable(frames);
                if (container != null) {
                    outlineNode = createDeclarationNode(xml, declarationKind(localName), lineNumber, startOffset, documentLength);
                    container.addChild(outlineNode);
                }
            }
        }

        frames.push(new ElementFrame(namespaceUri, localName, outlineNode));
    }

    private void handleEndElement(
        IDocument document,
        int documentLength,
        XMLStreamReader xml,
        Deque<ElementFrame> frames
    ) {
        ElementFrame frame = frames.poll();
        if (frame == null || frame.outlineNode() == null) {
            return;
        }

        int endOffset = resolveEndOffset(document, documentLength, xml.getLocation(), frame.outlineNode().startOffset());
        frame.outlineNode().setEndOffset(endOffset);
    }

    private XsltOutlineNode parseFallback(IDocument document, String text, int documentLength) {
        XsltOutlineNode root = XsltOutlineNode.root(documentLength);
        Map<Kind, XsltOutlineNode> categories = createCategories(documentLength);
        List<XsltOutlineNode> declarations = new ArrayList<>();
        String[] lines = text.split("\\R", -1);

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            Matcher declarationMatcher = DECLARATION_PATTERN.matcher(line);
            if (declarationMatcher.find()) {
                Kind kind = declarationKind(declarationMatcher.group(1));
                String label = buildFallbackLabel(kind, declarationMatcher.group(2));
                int lineOffset = lineOffset(document, index + 1);
                XsltOutlineNode node = XsltOutlineNode.declaration(
                    kind,
                    label,
                    index + 1,
                    lineOffset + declarationMatcher.start(),
                    documentLength
                );
                categories.get(categoryKind(kind)).addChild(node);
                declarations.add(node);
            }
        }

        for (int index = 0; index < declarations.size(); index++) {
            XsltOutlineNode node = declarations.get(index);
            int endOffset = index + 1 < declarations.size()
                ? declarations.get(index + 1).startOffset() - 1
                : documentLength;
            node.setEndOffset(endOffset);
        }

        attachNonEmptyCategories(root, categories);
        return root;
    }

    private Map<Kind, XsltOutlineNode> createCategories(int documentLength) {
        Map<Kind, XsltOutlineNode> categories = new EnumMap<>(Kind.class);
        for (Kind kind : CATEGORY_ORDER) {
            categories.put(kind, XsltOutlineNode.category(kind, documentLength));
        }
        return categories;
    }

    private void attachNonEmptyCategories(XsltOutlineNode root, Map<Kind, XsltOutlineNode> categories) {
        for (Kind kind : CATEGORY_ORDER) {
            XsltOutlineNode category = categories.get(kind);
            if (category.hasChildren()) {
                root.addChild(category);
            }
        }
    }

    private void disableExternalEntities(XMLInputFactory inputFactory) {
        setFactoryProperty(inputFactory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        setFactoryProperty(inputFactory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    }

    private void setFactoryProperty(XMLInputFactory inputFactory, String propertyName, Object value) {
        try {
            inputFactory.setProperty(propertyName, value);
        } catch (IllegalArgumentException exception) {
            // Ignore unsupported StAX properties and continue with parser defaults.
        }
    }

    private boolean isXsltElement(String namespaceUri) {
        return XSL_NAMESPACE_URI.equals(namespaceUri);
    }

    private boolean isTopLevelDeclaration(String localName, ElementFrame parentFrame) {
        if (parentFrame == null || !parentFrame.isXsltRoot()) {
            return false;
        }
        return switch (localName) {
            case "mode", "template", "function", "variable", "param", "accumulator", "package" -> true;
            default -> false;
        };
    }

    private boolean isDocumentRootPackage(String localName, ElementFrame parentFrame) {
        return parentFrame == null && "package".equals(localName);
    }

    private boolean isLocalDeclaration(String localName) {
        return "param".equals(localName) || "variable".equals(localName);
    }

    private XsltOutlineNode enclosingCallable(Deque<ElementFrame> frames) {
        for (ElementFrame frame : frames) {
            XsltOutlineNode outlineNode = frame.outlineNode();
            if (outlineNode == null) {
                continue;
            }
            if (outlineNode.kind() == Kind.TEMPLATE || outlineNode.kind() == Kind.FUNCTION) {
                return outlineNode;
            }
        }
        return null;
    }

    private XsltOutlineNode createDeclarationNode(
        XMLStreamReader xml,
        Kind kind,
        int lineNumber,
        int startOffset,
        int documentLength
    ) {
        return XsltOutlineNode.declaration(kind, buildLabel(xml, kind), lineNumber, startOffset, documentLength);
    }

    private String buildLabel(XMLStreamReader xml, Kind kind) {
        String name = xml.getAttributeValue(null, "name");
        if (name != null && !name.isBlank()) {
            return kind.displayLabel() + ": " + name;
        }

        if (kind == Kind.TEMPLATE) {
            String match = xml.getAttributeValue(null, "match");
            if (match != null && !match.isBlank()) {
                return kind.displayLabel() + ": " + match;
            }
        }

        return kind.displayLabel();
    }

    private String buildFallbackLabel(Kind kind, String attributes) {
        Matcher nameMatcher = NAME_PATTERN.matcher(attributes);
        if (nameMatcher.find()) {
            return kind.displayLabel() + ": " + nameMatcher.group(2);
        }
        return kind.displayLabel();
    }

    private Kind declarationKind(String localName) {
        return switch (localName) {
            case "mode" -> Kind.MODE;
            case "template" -> Kind.TEMPLATE;
            case "function" -> Kind.FUNCTION;
            case "variable" -> Kind.VARIABLE;
            case "param" -> Kind.PARAM;
            case "accumulator" -> Kind.ACCUMULATOR;
            case "package" -> Kind.PACKAGE;
            default -> throw new IllegalArgumentException("Unsupported XSLT outline element: " + localName);
        };
    }

    private Kind categoryKind(Kind declarationKind) {
        return switch (declarationKind) {
            case MODE -> Kind.MODES;
            case TEMPLATE -> Kind.TEMPLATES;
            case FUNCTION -> Kind.FUNCTIONS;
            case VARIABLE, PARAM -> Kind.VARIABLES_AND_PARAMS;
            case ACCUMULATOR -> Kind.ACCUMULATORS;
            case PACKAGE -> Kind.PACKAGES;
            default -> throw new IllegalArgumentException("Unsupported outline category for: " + declarationKind);
        };
    }

    private int safeLineNumber(Location location) {
        if (location == null || location.getLineNumber() <= 0) {
            return 1;
        }
        return location.getLineNumber();
    }

    private int resolveStartOffset(IDocument document, int documentLength, Location location, int lineNumber) {
        int reportedOffset = location == null ? -1 : location.getCharacterOffset();
        if (reportedOffset >= 0 && reportedOffset <= documentLength) {
            return reportedOffset;
        }
        return lineOffset(document, lineNumber);
    }

    private int resolveEndOffset(IDocument document, int documentLength, Location location, int minimumOffset) {
        int reportedOffset = location == null ? -1 : location.getCharacterOffset();
        if (reportedOffset >= 0 && reportedOffset <= documentLength) {
            return Math.max(reportedOffset, minimumOffset);
        }
        int lineNumber = safeLineNumber(location);
        return Math.max(lineOffset(document, lineNumber), minimumOffset);
    }

    private int lineOffset(IDocument document, int lineNumber) {
        if (document == null) {
            return 0;
        }

        int safeLineNumber = Math.max(lineNumber, 1);
        try {
            return document.getLineOffset(safeLineNumber - 1);
        } catch (BadLocationException exception) {
            return 0;
        }
    }

    private record ElementFrame(String namespaceUri, String localName, XsltOutlineNode outlineNode) {

        private boolean isXsltRoot() {
            return XSL_NAMESPACE_URI.equals(namespaceUri) && ("stylesheet".equals(localName) || "package".equals(localName));
        }
    }
}
