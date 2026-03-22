package nl.indi.eclipse.xslt3.ui.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class XsltOutlineNode {

    public enum Kind {
        ROOT("Root", false),
        MODES("Modes", true),
        MODE("mode", false),
        TEMPLATES("Templates", true),
        TEMPLATE("template", false),
        FUNCTIONS("Functions", true),
        FUNCTION("function", false),
        VARIABLES_AND_PARAMS("Variables and Params", true),
        VARIABLE("variable", false),
        PARAM("param", false),
        ACCUMULATORS("Accumulators", true),
        ACCUMULATOR("accumulator", false),
        PACKAGES("Packages", true),
        PACKAGE("package", false);

        private final String displayLabel;
        private final boolean category;

        Kind(String displayLabel, boolean category) {
            this.displayLabel = displayLabel;
            this.category = category;
        }

        public String displayLabel() {
            return displayLabel;
        }

        public boolean isCategory() {
            return category;
        }
    }

    private final String id;
    private final Kind kind;
    private final String label;
    private final int lineNumber;
    private final int startOffset;
    private int endOffset;
    private XsltOutlineNode parent;
    private final List<XsltOutlineNode> children = new ArrayList<>();

    private XsltOutlineNode(String id, Kind kind, String label, int lineNumber, int startOffset, int endOffset) {
        this.id = id;
        this.kind = kind;
        this.label = label;
        this.lineNumber = lineNumber;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public static XsltOutlineNode root(int documentLength) {
        return new XsltOutlineNode("root", Kind.ROOT, Kind.ROOT.displayLabel(), 1, 0, Math.max(documentLength, 0));
    }

    public static XsltOutlineNode category(Kind kind, int documentLength) {
        return new XsltOutlineNode(
            "category:" + kind.name(),
            kind,
            kind.displayLabel(),
            1,
            0,
            Math.max(documentLength, 0)
        );
    }

    public static XsltOutlineNode declaration(
        Kind kind,
        String label,
        int lineNumber,
        int startOffset,
        int endOffset
    ) {
        return new XsltOutlineNode(
            kind.name() + ":" + label + ":" + Integer.valueOf(startOffset),
            kind,
            label,
            lineNumber,
            startOffset,
            endOffset
        );
    }

    public String id() {
        return id;
    }

    public Kind kind() {
        return kind;
    }

    public String label() {
        return label;
    }

    public int lineNumber() {
        return lineNumber;
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public XsltOutlineNode parent() {
        return parent;
    }

    public List<XsltOutlineNode> children() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(XsltOutlineNode child) {
        child.parent = this;
        children.add(child);
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = Math.max(endOffset, startOffset);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean isCategory() {
        return kind.isCategory();
    }

    public boolean containsOffset(int offset) {
        return offset >= startOffset && offset <= endOffset;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof XsltOutlineNode node)) {
            return false;
        }
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
