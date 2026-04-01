package godclassinspector.services;

import java.util.List;
import java.util.Map;

import godclassinspector.model.SourceFileDTO;

public interface SuggestionsRefactoringService {
	Map<String, String> suggestRefactoring(List<SourceFileDTO> files) throws Exception;
}
