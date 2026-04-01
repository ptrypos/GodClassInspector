package godclassinspector.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import godclassinspector.model.ClassDTO;
import godclassinspector.model.ScanResultsDTO;
import godclassinspector.model.SourceFileDTO;
import godclassinspector.services.AnalysisService;
import godclassinspector.services.AnalysisServiceImp;
import godclassinspector.services.SuggestionsRefactoringService;
import godclassinspector.services.SuggestionsRefactoringServiceImp;
import godclassinspector.services.UmlDiagramService;
import godclassinspector.services.UmlDiagramServiceImp;
import godclassinspector.ui.FoundFilesUI;
import godclassinspector.ui.UmlDiagramUI;

public class AnalyzeHandler extends AbstractHandler {

	private final AnalysisService analysisService = new AnalysisServiceImp();
	private final UmlDiagramService umlDiagramService = new UmlDiagramServiceImp();
	private final SuggestionsRefactoringService suggestionsRefactoring = new SuggestionsRefactoringServiceImp();

	private static boolean enabled = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<SourceFileDTO> projectDiscoveredFilesList = ScanResultsDTO.getFiles();
		FoundFilesUI foundFilesUI = ScanResultsDTO.getView();

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		IWorkbenchPage page = window.getActivePage();

		try {
			analysisService.checkGodClass(projectDiscoveredFilesList);
			List<ClassDTO> projectClassesList = umlDiagramService.extractClassesFeatures(projectDiscoveredFilesList);
			//Map<String, String> suggestions = suggestionsRefactoring.suggestRefactoring(projectDiscoveredFilesList);

			foundFilesUI.setInput(projectDiscoveredFilesList);

			UmlDiagramUI umlView = (UmlDiagramUI) page.showView("godclassinspector.ui.umlDiagram");
			if (umlView != null) {
			    umlView.generateUML(projectClassesList);
			}

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