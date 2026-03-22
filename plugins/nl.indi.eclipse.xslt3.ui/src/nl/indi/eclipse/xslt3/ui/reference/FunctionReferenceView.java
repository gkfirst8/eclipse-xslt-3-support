package nl.indi.eclipse.xslt3.ui.reference;

import java.util.Optional;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import nl.indi.eclipse.xslt3.ui.editor.XsltTextEditor;

public class FunctionReferenceView extends ViewPart implements ISelectionListener, IPartListener2 {

    public static final String VIEW_ID = "nl.indi.eclipse.xslt3.ui.views.functionReference";

    private static final String DEFAULT_PLACEHOLDER =
        "Place the caret on a built-in XSLT/XPath function call to show the bundled reference.";
    private static final String INACTIVE_EDITOR_MESSAGE =
        "The Function Reference view tracks the active XSLT 3 editor.";

    private final FunctionReferenceRepository repository = FunctionReferenceRepository.getInstance();
    private final FunctionReferenceResolver resolver = new FunctionReferenceResolver();

    private Browser browser;
    private StyledText fallbackText;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        createContentControl(parent);
        getSite().getPage().addPostSelectionListener(this);
        getSite().getPage().addPartListener(this);
        renderPlaceholder(DEFAULT_PLACEHOLDER);
        refreshFromActivePart(getSite().getPage().getActivePart());
    }

    @Override
    public void dispose() {
        if (getSite() != null && getSite().getPage() != null) {
            getSite().getPage().removePostSelectionListener(this);
            getSite().getPage().removePartListener(this);
        }
        super.dispose();
    }

    @Override
    public void setFocus() {
        if (browser != null && !browser.isDisposed()) {
            browser.setFocus();
            return;
        }
        if (fallbackText != null && !fallbackText.isDisposed()) {
            fallbackText.setFocus();
        }
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part instanceof XsltTextEditor editor && selection instanceof ITextSelection textSelection) {
            renderReference(editor, textSelection.getOffset());
        }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        refreshFromActivePart(partRef.getPart(false));
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        refreshFromActivePart(partRef.getPart(false));
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false) instanceof XsltTextEditor) {
            refreshFromActivePart(getSite().getPage().getActivePart());
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

    private void createContentControl(Composite parent) {
        try {
            browser = new Browser(parent, SWT.NONE);
        } catch (SWTError | SWTException exception) {
            fallbackText = new StyledText(parent, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
            fallbackText.setWordWrap(true);
        }
    }

    private void refreshFromActivePart(IWorkbenchPart part) {
        if (part instanceof XsltTextEditor editor) {
            ISelectionProvider selectionProvider = editor.getSelectionProvider();
            if (selectionProvider != null && selectionProvider.getSelection() instanceof ITextSelection textSelection) {
                renderReference(editor, textSelection.getOffset());
                return;
            }
            renderReference(editor, 0);
            return;
        }

        if (part instanceof IEditorPart) {
            renderPlaceholder(INACTIVE_EDITOR_MESSAGE);
        }
    }

    private void renderReference(XsltTextEditor editor, int offset) {
        IDocument document = editor.getDocument();
        if (document == null) {
            renderPlaceholder(DEFAULT_PLACEHOLDER);
            return;
        }

        Optional<FunctionReferenceEntry> entry = resolver.resolveLookupKey(document.get(), offset)
            .flatMap(repository::findByLookupKey);

        if (entry.isPresent()) {
            renderEntry(entry.get());
            return;
        }

        renderPlaceholder(DEFAULT_PLACEHOLDER);
    }

    private void renderEntry(FunctionReferenceEntry entry) {
        String html = """
            <html>
              <head>
                <meta charset="UTF-8"/>
                <style>%s</style>
              </head>
              <body>
                <article class="reference-card">
                  <div class="eyebrow">Bundled XSLT/XPath Reference</div>
                  <h1>%s</h1>
                  <pre class="signature">%s</pre>
                  <p class="summary">%s</p>
                  <p class="details">%s</p>
                </article>
              </body>
            </html>
            """.formatted(
            repository.getStyleSheet(),
            escapeHtml(entry.title()),
            escapeHtml(entry.signature()),
            escapeHtml(entry.summary()),
            escapeHtml(entry.details())
        );

        if (browser != null && !browser.isDisposed()) {
            browser.setText(html);
            return;
        }

        if (fallbackText != null && !fallbackText.isDisposed()) {
            fallbackText.setText("""
                %s

                %s

                %s

                %s
                """.formatted(
                entry.title(),
                entry.signature(),
                entry.summary(),
                entry.details()
            ));
        }
    }

    private void renderPlaceholder(String message) {
        String html = """
            <html>
              <head>
                <meta charset="UTF-8"/>
                <style>%s</style>
              </head>
              <body>
                <article class="reference-card reference-card--placeholder">
                  <div class="eyebrow">Bundled XSLT/XPath Reference</div>
                  <h1>Function Reference</h1>
                  <p class="summary">%s</p>
                </article>
              </body>
            </html>
            """.formatted(repository.getStyleSheet(), escapeHtml(message));

        if (browser != null && !browser.isDisposed()) {
            browser.setText(html);
            return;
        }

        if (fallbackText != null && !fallbackText.isDisposed()) {
            fallbackText.setText("Function Reference\n\n" + message);
        }
    }

    private String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
