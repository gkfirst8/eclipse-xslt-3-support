package nl.indi.eclipse.xslt3.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import nl.indi.eclipse.xslt3.core.XsltValidationJob;
import nl.indi.eclipse.xslt3.ui.outline.XsltOutlinePage;
import nl.indi.eclipse.xslt3.ui.reference.FunctionReferenceView;
import nl.indi.eclipse.xslt3.ui.text.ColorManager;
import nl.indi.eclipse.xslt3.ui.text.XsltSourceViewerConfiguration;

public class XsltTextEditor extends TextEditor {

    private final ColorManager colorManager;
    private final ISelectionChangedListener editorSelectionListener;
    private XsltOutlinePage outlinePage;

    public XsltTextEditor() {
        colorManager = new ColorManager();
        editorSelectionListener = event -> {
            if (outlinePage == null) {
                return;
            }
            if (event.getSelection() instanceof ITextSelection textSelection) {
                outlinePage.syncSelectionToOffset(textSelection.getOffset());
            }
        };
        setSourceViewerConfiguration(new XsltSourceViewerConfiguration(colorManager));
        setDocumentProvider(new FileDocumentProvider());
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        installSelectionListener();
        syncOutlineSelection();
        showFunctionReferenceViewAsync();
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
        if (getEditorInput() instanceof FileEditorInput fileEditorInput) {
            XsltValidationJob.schedule(fileEditorInput.getFile());
        }
        refreshOutline();
        syncOutlineSelection();
    }

    @Override
    public void dispose() {
        uninstallSelectionListener();
        if (outlinePage != null) {
            outlinePage.disposePage();
        }
        colorManager.dispose();
        super.dispose();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IContentOutlinePage.class) {
            if (outlinePage == null) {
                outlinePage = new XsltOutlinePage(this);
                outlinePage.setInput(getDocument());
                syncOutlineSelection();
            }
            return (T) outlinePage;
        }
        return super.getAdapter(adapter);
    }

    public void revealLine(int lineNumber) {
        IDocument document = getDocument();
        if (document == null || lineNumber <= 0) {
            return;
        }

        try {
            IRegion lineInformation = document.getLineInformation(lineNumber - 1);
            selectAndReveal(lineInformation.getOffset(), lineInformation.getLength());
        } catch (BadLocationException exception) {
            // Ignore invalid locations and keep the editor responsive.
        }
    }

    public void revealRange(int offset, int length) {
        IDocument document = getDocument();
        if (document == null || offset < 0) {
            return;
        }

        int safeOffset = Math.min(offset, document.getLength());
        int safeLength = Math.max(length, 0);
        selectAndReveal(safeOffset, safeLength);
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        refreshOutline();
        syncOutlineSelection();
    }

    private void installSelectionListener() {
        ISelectionProvider selectionProvider = getSelectionProvider();
        if (selectionProvider instanceof IPostSelectionProvider postSelectionProvider) {
            postSelectionProvider.addPostSelectionChangedListener(editorSelectionListener);
        } else if (selectionProvider != null) {
            selectionProvider.addSelectionChangedListener(editorSelectionListener);
        }
    }

    private void uninstallSelectionListener() {
        ISelectionProvider selectionProvider = getSelectionProvider();
        if (selectionProvider instanceof IPostSelectionProvider postSelectionProvider) {
            postSelectionProvider.removePostSelectionChangedListener(editorSelectionListener);
        } else if (selectionProvider != null) {
            selectionProvider.removeSelectionChangedListener(editorSelectionListener);
        }
    }

    private void refreshOutline() {
        if (outlinePage != null) {
            outlinePage.setInput(getDocument());
        }
    }

    private void syncOutlineSelection() {
        if (outlinePage == null) {
            return;
        }

        ISelectionProvider selectionProvider = getSelectionProvider();
        if (selectionProvider == null) {
            return;
        }

        if (selectionProvider.getSelection() instanceof ITextSelection textSelection) {
            outlinePage.syncSelectionToOffset(textSelection.getOffset());
        }
    }

    public IDocument getDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    public IFile getFile() {
        if (getEditorInput() instanceof FileEditorInput fileEditorInput) {
            return fileEditorInput.getFile();
        }
        return null;
    }

    private void showFunctionReferenceViewAsync() {
        if (getSite() == null || getSite().getShell() == null) {
            return;
        }

        Display display = getSite().getShell().getDisplay();
        if (display == null || display.isDisposed()) {
            return;
        }

        display.asyncExec(() -> {
            if (getSite() == null || getSite().getPage() == null) {
                return;
            }

            try {
                getSite().getPage().showView(FunctionReferenceView.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
            } catch (PartInitException exception) {
                // Ignore view startup issues and keep the editor usable.
            }
        });
    }
}
