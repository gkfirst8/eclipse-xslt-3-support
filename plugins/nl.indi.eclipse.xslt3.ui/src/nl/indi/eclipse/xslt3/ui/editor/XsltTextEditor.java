package nl.indi.eclipse.xslt3.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import nl.indi.eclipse.xslt3.core.XsltValidationJob;
import nl.indi.eclipse.xslt3.ui.outline.XsltOutlinePage;
import nl.indi.eclipse.xslt3.ui.text.ColorManager;
import nl.indi.eclipse.xslt3.ui.text.XsltSourceViewerConfiguration;

public class XsltTextEditor extends TextEditor {

    private final ColorManager colorManager;
    private XsltOutlinePage outlinePage;

    public XsltTextEditor() {
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new XsltSourceViewerConfiguration(colorManager));
        setDocumentProvider(new FileDocumentProvider());
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
        if (getEditorInput() instanceof FileEditorInput fileEditorInput) {
            XsltValidationJob.schedule(fileEditorInput.getFile());
        }
        refreshOutline();
    }

    @Override
    public void dispose() {
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

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        refreshOutline();
    }

    private void refreshOutline() {
        if (outlinePage != null) {
            outlinePage.setInput(getDocument());
        }
    }

    private IDocument getDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    public IFile getFile() {
        if (getEditorInput() instanceof FileEditorInput fileEditorInput) {
            return fileEditorInput.getFile();
        }
        return null;
    }
}
