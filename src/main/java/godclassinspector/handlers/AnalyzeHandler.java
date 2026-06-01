package godclassinspector.handlers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import godclassinspector.model.UmlClassDTO;
import godclassinspector.model.ScanResultsDTO;
import godclassinspector.model.ClassDTO;
import godclassinspector.services.DetectionService;
import godclassinspector.services.DetectionServiceImp;
import godclassinspector.services.SuggestionsRefactoringService;
import godclassinspector.services.SuggestionsRefactoringServiceImp;
import godclassinspector.services.UmlDiagramService;
import godclassinspector.services.UmlDiagramServiceImp;
import godclassinspector.ui.FoundFilesUI;
import godclassinspector.ui.RefactoringSuggestionsUI;
import godclassinspector.ui.UmlDiagramUI;

public class AnalyzeHandler extends AbstractHandler {

	private final DetectionService detectionService = new DetectionServiceImp();
	private final UmlDiagramService umlDiagramService = new UmlDiagramServiceImp();
	private final SuggestionsRefactoringService suggestionsRefactoring = new SuggestionsRefactoringServiceImp();

	private static boolean enabled = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<ClassDTO> projectDiscoveredFilesList = ScanResultsDTO.getFiles();
		FoundFilesUI foundFilesUI = ScanResultsDTO.getView();

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();

		try {
			detectionService.checkGodClass(projectDiscoveredFilesList);

			Map<String, Map<String, String>> suggestionsMap = suggestionsRefactoring
					.suggestRefactoring(projectDiscoveredFilesList);
			
			List<UmlClassDTO> projectClassesList = umlDiagramService
				.extractClassesFeatures(projectDiscoveredFilesList);

			displayResults(
				page,
				foundFilesUI,
				projectDiscoveredFilesList,
				projectClassesList,
				suggestionsMap,
				event
			);

		} catch (Exception e) {
			MessageDialog.openError(
				HandlerUtil.getActiveShell(event),
				"Analyze Error",
				"An error occurred during analysis: " + e.getMessage()
			);
		}

		return null;
	}

	private void displayResults(IWorkbenchPage page, FoundFilesUI foundFilesUI,
			List<ClassDTO> projectDiscoveredFilesList, List<UmlClassDTO> projectClassesList,
			Map<String, Map<String, String>> suggestionsMap, ExecutionEvent event) throws PartInitException {

		foundFilesUI.setInput(projectDiscoveredFilesList);

		UmlDiagramUI umlView = (UmlDiagramUI) page.showView("godclassinspector.ui.umlDiagram");
		if (umlView != null) {
			umlView.generateUML(projectClassesList);
		}

		RefactoringSuggestionsUI suggestionsView = (RefactoringSuggestionsUI) page.showView("godclassinspector.ui.refactoringSuggestions");
		if (suggestionsView != null) {
			suggestionsView.setInput(suggestionsMap);
		} else {
			MessageDialog.openWarning(
				HandlerUtil.getActiveShell(event),
				"View Not Found",
				"Refactoring Suggestions view could not be opened. " +
				"Ensure it is registered in plugin.xml"
			);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabledStatus) {
		enabled = enabledStatus;
	}
}