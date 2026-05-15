package godclassinspector.services;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

import godclassinspector.model.SourceFileDTO;

public interface DiscoveryService {
	IProject detectProject(ExecutionEvent event) throws Exception;
	List<SourceFileDTO> findAllJavaFiles(IProject project) throws Exception;
}