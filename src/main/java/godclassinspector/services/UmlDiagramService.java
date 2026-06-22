package godclassinspector.services;

import java.io.FileNotFoundException;
import java.util.List;

import godclassinspector.models.UmlClassDTO;
import godclassinspector.models.ClassDTO;

public interface UmlDiagramService {
	List<UmlClassDTO> extractClassesFeatures(List<ClassDTO> projectFiles) throws FileNotFoundException;
}
