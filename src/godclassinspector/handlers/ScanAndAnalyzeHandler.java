package godclassinspector.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import godclassinspector.model.ScanResultsDTO;
import godclassinspector.model.SourceFileDTO;
import godclassinspector.services.AnalysisService;
import godclassinspector.services.AnalysisServiceImp;
import godclassinspector.services.DiscoveryService;
import godclassinspector.services.DiscoveryServiceImp;
import godclassinspector.ui.FoundFilesUI;

public class ScanAndAnalyzeHandler extends AbstractHandler {

	private final DiscoveryService discoveryService = new DiscoveryServiceImp();
	private final AnalysisService analysisService = new AnalysisServiceImp();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.scanFiles(event);
		this.analyzeFiles(event);
		return null;
	}

	private void scanFiles(ExecutionEvent event) {
		try {
            IProject project = discoveryService.detectProject(event);
            List<SourceFileDTO> projectDiscoveredFiles = discoveryService.findAllJavaFiles(project);

            IWorkbenchPage filesFoundWindow = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            FoundFilesUI foundFilesUI = (FoundFilesUI) filesFoundWindow.showView("godclassinspector.ui.foundFiles");

            if (foundFilesUI != null) {
            	foundFilesUI.setInput(projectDiscoveredFiles);
            }

            ScanResultsDTO.setFiles(projectDiscoveredFiles);
            ScanResultsDTO.setView(foundFilesUI);

            AnalyzeHandler.setEnabled(true);
            org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.services.IEvaluationService.class).requestEvaluation("selection");
        } catch (Exception e) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Scan Error", e.getMessage());
        }
	}

	private void analyzeFiles(ExecutionEvent event) {
		List<SourceFileDTO> projectDiscoveredFiles = ScanResultsDTO.getFiles();
        FoundFilesUI foundFilesUI = ScanResultsDTO.getView();

        try {
        	analysisService.checkGodClass(projectDiscoveredFiles);
			foundFilesUI.setInput(projectDiscoveredFiles);
		} catch (Exception e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Analyze Error", e.getMessage());
		}
	}
}
