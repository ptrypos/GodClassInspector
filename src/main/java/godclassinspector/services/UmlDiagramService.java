package godclassinspector.services;

import java.io.FileNotFoundException;
import java.util.List;

import godclassinspector.model.UmlClassDTO;
import godclassinspector.model.ClassDTO;

public interface UmlDiagramService {
	List<UmlClassDTO> extractClassesFeatures(List<ClassDTO> projectFiles) throws FileNotFoundException;
}
