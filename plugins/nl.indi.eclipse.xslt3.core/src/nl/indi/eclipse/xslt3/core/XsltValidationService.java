package nl.indi.eclipse.xslt3.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sf.saxon.TransformerFactoryImpl;

public class XsltValidationService {

    public void validate(IFile file, IProgressMonitor monitor) {
        if (file == null || !file.isAccessible()) {
            return;
        }

        try {
            file.deleteMarkers(XsltProblem.MARKER_TYPE, true, IFile.DEPTH_ZERO);
        } catch (CoreException exception) {
            return;
        }

        List<XsltValidationMessage> messages = collectMessages(file);
        for (XsltValidationMessage message : deduplicate(messages)) {
            createMarker(file, message);
        }
    }

    private List<XsltValidationMessage> collectMessages(IFile file) {
        List<XsltValidationMessage> messages = new ArrayList<>();
        CollectingErrorListener errorListener = new CollectingErrorListener(messages);

        try (InputStream inputStream = file.getContents()) {
            TransformerFactoryImpl transformerFactory = new TransformerFactoryImpl();
            transformerFactory.setErrorListener(errorListener);

            Source source = new StreamSource(inputStream, file.getLocationURI().toString());
            transformerFactory.newTemplates(source);
        } catch (Exception exception) {
            if (messages.isEmpty()) {
                messages.add(new XsltValidationMessage(
                    IMarker.SEVERITY_ERROR,
                    sanitizeMessage(exception.getMessage()),
                    Integer.valueOf(1),
                    null,
                    file.getLocationURI().toString()
                ));
            }
        }

        return messages;
    }

    private List<XsltValidationMessage> deduplicate(List<XsltValidationMessage> messages) {
        Set<String> seen = new LinkedHashSet<>();
        List<XsltValidationMessage> result = new ArrayList<>();
        for (XsltValidationMessage message : messages) {
            String key = message.severity()
                + "|" + message.lineNumber()
                + "|" + message.columnNumber()
                + "|" + message.systemId()
                + "|" + message.message();
            if (seen.add(key)) {
                result.add(message);
            }
        }
        return result;
    }

    private void createMarker(IFile file, XsltValidationMessage message) {
        try {
            IMarker marker = file.createMarker(XsltProblem.MARKER_TYPE);
            marker.setAttribute(IMarker.SEVERITY, message.severity());
            marker.setAttribute(IMarker.MESSAGE, buildMarkerMessage(message));
            if (message.lineNumber() != null && message.lineNumber().intValue() > 0) {
                marker.setAttribute(IMarker.LINE_NUMBER, message.lineNumber().intValue());
            }
        } catch (CoreException exception) {
            // Ignore marker creation failures and keep the editor usable.
        }
    }

    private String buildMarkerMessage(XsltValidationMessage message) {
        if (message.systemId() == null) {
            return message.message();
        }
        return message.message() + " [" + message.systemId() + "]";
    }

    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "XSLT validation failed.";
        }
        return message;
    }

    private static final class CollectingErrorListener implements ErrorListener {

        private final List<XsltValidationMessage> messages;

        private CollectingErrorListener(List<XsltValidationMessage> messages) {
            this.messages = messages;
        }

        @Override
        public void warning(TransformerException exception) {
            add(IMarker.SEVERITY_WARNING, exception);
        }

        @Override
        public void error(TransformerException exception) {
            add(IMarker.SEVERITY_ERROR, exception);
        }

        @Override
        public void fatalError(TransformerException exception) {
            add(IMarker.SEVERITY_ERROR, exception);
        }

        private void add(int severity, TransformerException exception) {
            Integer lineNumber = null;
            Integer columnNumber = null;
            String systemId = null;
            if (exception.getLocator() != null) {
                if (exception.getLocator().getLineNumber() > 0) {
                    lineNumber = Integer.valueOf(exception.getLocator().getLineNumber());
                }
                if (exception.getLocator().getColumnNumber() > 0) {
                    columnNumber = Integer.valueOf(exception.getLocator().getColumnNumber());
                }
                systemId = exception.getLocator().getSystemId();
            }

            messages.add(new XsltValidationMessage(
                severity,
                sanitizeExceptionMessage(exception),
                lineNumber,
                columnNumber,
                systemId
            ));
        }

        private String sanitizeExceptionMessage(TransformerException exception) {
            String message = exception.getMessageAndLocation();
            if (message == null || message.isBlank()) {
                message = exception.getMessage();
            }
            if (message == null || message.isBlank()) {
                return "XSLT validation failed.";
            }
            return message;
        }
    }
}
