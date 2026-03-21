package godclassinspector.handlers;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import godclassinspector.model.SourceFileDTO;
import godclassinspector.services.ProjectDiscoveryService;
import godclassinspector.services.ProjectDiscoveryServiceImpl;
import godclassinspector.ui.FilesFoundUI;

public class GodClassInspectorHandler extends AbstractHandler {

    private final ProjectDiscoveryService projectDiscoveryService = new ProjectDiscoveryServiceImpl();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            IProject project = projectDiscoveryService.detectProject(event);
            List<SourceFileDTO> files = projectDiscoveryService.findAllJavaFiles(project);
            
            String scanCompletedMessage = "Project: " + project.getName() + "\nFiles Found: " + files.size();
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Scan Complete", scanCompletedMessage);
            
            IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            FilesFoundUI view = (FilesFoundUI) page.showView("godclassinspector.ui.foundFiles");
            
            if (view != null) {
            	view.setInput(files);
            }
        } catch (Exception e) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Scan Error", e.getMessage());
        }
        return null;
    }
}