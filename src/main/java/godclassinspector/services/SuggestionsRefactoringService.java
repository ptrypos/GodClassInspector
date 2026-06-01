package godclassinspector.services;

import java.util.List;
import java.util.Map;

import godclassinspector.model.ClassDTO;

public interface SuggestionsRefactoringService {
	Map<String, Map<String, String>> suggestRefactoring(List<ClassDTO> files) throws Exception;
}
