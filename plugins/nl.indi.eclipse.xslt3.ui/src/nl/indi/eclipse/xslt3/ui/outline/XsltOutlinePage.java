package nl.indi.eclipse.xslt3.ui.outline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import nl.indi.eclipse.xslt3.ui.editor.XsltTextEditor;

public class XsltOutlinePage extends ContentOutlinePage {

    private static final Object[] NO_CHILDREN = new Object[0];
    private static final Pattern DECLARATION_PATTERN = Pattern.compile(
        "<xsl:(template|function|variable|param|mode|accumulator|package)\\b([^>]*)"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile("(name|match)\\s*=\\s*['\"]([^'\"]+)['\"]");

    private final XsltTextEditor editor;
    private final IDocumentListener documentListener;
    private IDocument document;
    private boolean disposed;

    public XsltOutlinePage(XsltTextEditor editor) {
        this.editor = editor;
        this.documentListener = new IDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                refreshAsync();
            }

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
        };
    }

    @Override
    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        super.createControl(parent);
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new FlatOutlineContentProvider());
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XsltOutlineNode node) {
                    return node.label();
                }
                return super.getText(element);
            }
        });
        viewer.addSelectionChangedListener(selectionChangedListener());
        refreshViewer();
    }

    public void setInput(IDocument newDocument) {
        if (disposed) {
            return;
        }
        if (document == newDocument) {
            refreshViewer();
            return;
        }

        if (document != null) {
            document.removeDocumentListener(documentListener);
        }
        document = newDocument;
        if (document != null) {
            document.addDocumentListener(documentListener);
        }
        refreshViewer();
    }

    public void disposePage() {
        disposed = true;
        if (document != null) {
            document.removeDocumentListener(documentListener);
            document = null;
        }
    }

    private ISelectionChangedListener selectionChangedListener() {
        return event -> {
            if (event.getSelection() instanceof IStructuredSelection selection) {
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof XsltOutlineNode node) {
                    editor.revealLine(node.lineNumber());
                }
            }
        };
    }

    private void refreshAsync() {
        if (disposed) {
            return;
        }
        Display display = Display.getDefault();
        if (display == null || display.isDisposed()) {
            return;
        }
        display.asyncExec(this::refreshViewer);
    }

    private void refreshViewer() {
        if (disposed) {
            return;
        }
        TreeViewer viewer = getTreeViewer();
        if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) {
            return;
        }
        viewer.setInput(parseNodes());
        viewer.expandAll();
    }

    private List<XsltOutlineNode> parseNodes() {
        List<XsltOutlineNode> nodes = new ArrayList<>();
        if (document == null) {
            return nodes;
        }

        String[] lines = document.get().split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            Matcher declarationMatcher = DECLARATION_PATTERN.matcher(lines[index]);
            if (!declarationMatcher.find()) {
                continue;
            }

            String declarationType = declarationMatcher.group(1);
            String attributes = declarationMatcher.group(2);
            String label = declarationType;

            Matcher nameMatcher = NAME_PATTERN.matcher(attributes);
            if (nameMatcher.find()) {
                label = declarationType + ": " + nameMatcher.group(2);
            }

            nodes.add(new XsltOutlineNode(label, index + 1));
        }

        return nodes;
    }

    private static final class FlatOutlineContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Collection<?> collection) {
                return collection.toArray();
            }
            return NO_CHILDREN;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            return NO_CHILDREN;
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return false;
        }
    }
}
