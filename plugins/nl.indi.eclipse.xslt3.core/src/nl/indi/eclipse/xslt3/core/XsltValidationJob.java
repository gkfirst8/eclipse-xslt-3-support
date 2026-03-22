package nl.indi.eclipse.xslt3.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public final class XsltValidationJob {

    private XsltValidationJob() {
    }

    public static void schedule(IFile file) {
        if (file == null) {
            return;
        }

        WorkspaceJob job = new WorkspaceJob("Validate XSLT 3.0 stylesheet") {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                new XsltValidationService().validate(file, monitor);
                return Status.OK_STATUS;
            }
        };
        job.setRule(file);
        job.setSystem(true);
        job.schedule();
    }
}

