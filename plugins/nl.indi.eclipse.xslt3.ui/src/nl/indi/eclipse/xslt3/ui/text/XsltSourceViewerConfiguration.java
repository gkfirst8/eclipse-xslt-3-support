package nl.indi.eclipse.xslt3.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class XsltSourceViewerConfiguration extends TextSourceViewerConfiguration {

    private final ColorManager colorManager;

    public XsltSourceViewerConfiguration(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    @Override
    public String[] getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        return new String[] {IDocument.DEFAULT_CONTENT_TYPE};
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        DefaultDamagerRepairer repairer = new DefaultDamagerRepairer(new XsltCodeScanner(colorManager));
        reconciler.setDamager(repairer, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(repairer, IDocument.DEFAULT_CONTENT_TYPE);
        return reconciler;
    }

    @Override
    public IContentAssistant getContentAssistant(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        ContentAssistant assistant = new ContentAssistant();
        assistant.setContentAssistProcessor(new XsltContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(250);
        return assistant;
    }
}

