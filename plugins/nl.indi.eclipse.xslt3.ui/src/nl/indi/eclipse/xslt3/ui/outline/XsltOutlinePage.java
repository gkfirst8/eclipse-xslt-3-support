package nl.indi.eclipse.xslt3.ui.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import nl.indi.eclipse.xslt3.ui.editor.XsltTextEditor;

public class XsltOutlinePage extends ContentOutlinePage {

    private static final Object[] NO_CHILDREN = new Object[0];

    private final XsltTextEditor editor;
    private final XsltOutlineParser parser = new XsltOutlineParser();
    private final IDocumentListener documentListener;
    private IDocument document;
    private XsltOutlineNode rootNode = XsltOutlineNode.root(0);
    private boolean disposed;
    private boolean synchronizingFromEditor;
    private boolean initialExpansionApplied;
    private int pendingSelectionOffset = -1;

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
        viewer.setContentProvider(new OutlineContentProvider());
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

    public void syncSelectionToOffset(int offset) {
        if (disposed) {
            return;
        }
        pendingSelectionOffset = Math.max(offset, -1);
        syncSelection();
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
            if (synchronizingFromEditor) {
                return;
            }
            if (event.getSelection() instanceof IStructuredSelection selection) {
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof XsltOutlineNode node && !node.isCategory()) {
                    editor.revealRange(node.startOffset(), 0);
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

        Set<String> expandedNodeIds = expandedNodeIds(viewer);
        rootNode = parser.parse(document);
        viewer.setInput(rootNode);
        restoreExpansion(viewer, expandedNodeIds);
        syncSelection();
    }

    private Set<String> expandedNodeIds(TreeViewer viewer) {
        return Arrays.stream(viewer.getExpandedElements())
            .filter(XsltOutlineNode.class::isInstance)
            .map(XsltOutlineNode.class::cast)
            .map(XsltOutlineNode::id)
            .collect(Collectors.toSet());
    }

    private void restoreExpansion(TreeViewer viewer, Set<String> expandedNodeIds) {
        if (!initialExpansionApplied) {
            viewer.expandToLevel(2);
            initialExpansionApplied = true;
            return;
        }

        List<XsltOutlineNode> nodesToExpand = new ArrayList<>();
        collectExpandedNodes(rootNode, expandedNodeIds, nodesToExpand);
        viewer.setExpandedElements(nodesToExpand.toArray());
    }

    private void collectExpandedNodes(
        XsltOutlineNode currentNode,
        Set<String> expandedNodeIds,
        List<XsltOutlineNode> nodesToExpand
    ) {
        if (expandedNodeIds.contains(currentNode.id())) {
            nodesToExpand.add(currentNode);
        }
        for (XsltOutlineNode child : currentNode.children()) {
            collectExpandedNodes(child, expandedNodeIds, nodesToExpand);
        }
    }

    private void syncSelection() {
        if (disposed || pendingSelectionOffset < 0) {
            return;
        }

        TreeViewer viewer = getTreeViewer();
        if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) {
            return;
        }

        XsltOutlineNode matchingNode = findDeepestNode(rootNode, pendingSelectionOffset);
        Object currentSelection = null;
        if (viewer.getSelection() instanceof IStructuredSelection selection) {
            currentSelection = selection.getFirstElement();
        }
        if (matchingNode == currentSelection || (matchingNode != null && matchingNode.equals(currentSelection))) {
            return;
        }

        synchronizingFromEditor = true;
        try {
            if (matchingNode == null) {
                viewer.setSelection(StructuredSelection.EMPTY);
                return;
            }

            expandParents(viewer, matchingNode);
            viewer.setSelection(new StructuredSelection(matchingNode), true);
        } finally {
            synchronizingFromEditor = false;
        }
    }

    private void expandParents(TreeViewer viewer, XsltOutlineNode node) {
        XsltOutlineNode current = node.parent();
        while (current != null && current.parent() != null) {
            viewer.expandToLevel(current, 1);
            current = current.parent();
        }
    }

    private XsltOutlineNode findDeepestNode(XsltOutlineNode currentNode, int offset) {
        for (XsltOutlineNode child : currentNode.children()) {
            if (!child.containsOffset(offset)) {
                continue;
            }

            XsltOutlineNode deeperNode = findDeepestNode(child, offset);
            if (deeperNode != null) {
                return deeperNode;
            }
            if (!child.isCategory()) {
                return child;
            }
        }

        return null;
    }

    private static final class OutlineContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof XsltOutlineNode node) {
                return node.children().toArray();
            }
            return NO_CHILDREN;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof XsltOutlineNode node) {
                return node.children().toArray();
            }
            return NO_CHILDREN;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof XsltOutlineNode node) {
                return node.parent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return element instanceof XsltOutlineNode node && node.hasChildren();
        }
    }
}
