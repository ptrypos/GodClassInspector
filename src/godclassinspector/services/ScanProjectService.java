package godclassinspector.services;

import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import godclassinspector.model.SourceFileDTO;

public interface ScanProjectService {
    IProject detectProject(ExecutionEvent event) throws Exception;
    String getProjectSourceFolderPath(IProject project);
    List<SourceFileDTO> findAllJavaFiles(IProject project);
}