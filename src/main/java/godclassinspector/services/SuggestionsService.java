package godclassinspector.services;

import java.util.List;
import java.util.Map;

import godclassinspector.models.ClassDTO;

public interface SuggestionsService {
	Map<String, Map<String, String>> suggestRefactoring(List<ClassDTO> files) throws Exception;
}
