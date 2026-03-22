package godclassinspector.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import godclassinspector.model.ScanResultsDTO;
import godclassinspector.model.SourceFileDTO;
import godclassinspector.services.AnalysisService;
import godclassinspector.services.AnalysisServiceImp;

public class AnalyzeHandler extends AbstractHandler {

	private final AnalysisService analysisService = new AnalysisServiceImp();
	
	private static boolean enabled = false;
	
	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        List<SourceFileDTO> projectDiscoveredFiles = ScanResultsDTO.getFiles();
        
        try {
			for (int i = 0; i < projectDiscoveredFiles.size(); i++) {
				analysisService.calculateMetrics(projectDiscoveredFiles.get(i));
				
				System.out.println(projectDiscoveredFiles.get(i).toString());
			}
        	
			System.out.println();
		} catch (Exception e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Analyze Error", e.getMessage());
		}
        
        return null;
    }
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public static void setEnabled(boolean enabledStatus) {
		enabled = enabledStatus;
	}
}
