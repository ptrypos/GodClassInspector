package godclassinspector.services;

import java.io.FileNotFoundException;
import java.util.List;

import godclassinspector.model.ClassDTO;
import godclassinspector.model.SourceFileDTO;

public interface UmlDiagramService {
	List<ClassDTO> extractClassesFeatures(List<SourceFileDTO> projectFiles) throws FileNotFoundException;
}
