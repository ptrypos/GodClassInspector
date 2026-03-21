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
import godclassinspector.services.AnalysisService;
import godclassinspector.services.AnalysisServiceImp;
import godclassinspector.services.DiscoveryService;
import godclassinspector.services.DiscoveryServiceImp;
import godclassinspector.ui.FilesFoundUI;

public class DiscoverHandler extends AbstractHandler {

    private final DiscoveryService discoveryService = new DiscoveryServiceImp();
    private final AnalysisService analysisService = new AnalysisServiceImp();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            IProject project = discoveryService.detectProject(event);
            List<SourceFileDTO> files = discoveryService.findAllJavaFiles(project);
            
            String scanCompletedMessage = "Project: " + project.getName() + "\nFiles Found: " + files.size();
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Scan Complete", scanCompletedMessage);
            
            IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            FilesFoundUI view = (FilesFoundUI) page.showView("godclassinspector.ui.foundFiles");
            
            
            // To be deleted this is just for test. When the handler of analization is done move it there.
            
            for (SourceFileDTO file : files) {
            	analysisService.calculateMetrics(file);
            }
            
            for (SourceFileDTO file : files) {
            	System.out.println(file.getTightClassCohesion());
            }
            
            if (view != null) {
            	view.setInput(files);
            }
        } catch (Exception e) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Scan Error", e.getMessage());
        }
        return null;
    }
}