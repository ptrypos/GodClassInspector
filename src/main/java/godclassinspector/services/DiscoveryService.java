package godclassinspector.services;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

import godclassinspector.model.ClassDTO;

public interface DiscoveryService {
	IProject detectProject(ExecutionEvent event) throws Exception;
	List<ClassDTO> findAllJavaFiles(IProject project) throws Exception;
}