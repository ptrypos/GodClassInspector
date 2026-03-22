package godclassinspector.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import godclassinspector.model.SourceFileDTO;

public class DiscoveryServiceImp implements DiscoveryService {

    @Override
    public IProject detectProject(ExecutionEvent event) throws Exception {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            IProject project = Adapters.adapt(element, IProject.class);
            
            if (project != null && project.isOpen()) {
                return project;
            }
        }
        throw new Exception("Invalid project selection. Please select the project root directory in \"Project Explorer\" window.");
    }

    @Override
    public List<SourceFileDTO> findAllJavaFiles(IProject project) throws Exception {
        List<SourceFileDTO> foundFiles = new ArrayList<>();
        String sourcePath = this.getProjectSourceFolderPath(project);
        if (sourcePath != null) {
        	this.detectJavaFiles(new File(sourcePath), sourcePath, foundFiles);
        }
        return foundFiles;
    }

    private void detectJavaFiles(File root, String sourcePath, List<SourceFileDTO> foundFiles) {
        File[] files = root.listFiles();
        
        if (files == null) {
        	return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
            	this.detectJavaFiles(file, sourcePath, foundFiles);
            } else if (file.getName().endsWith(".java")) {
            	String packageParent = this.getJavaFileParentPackage(sourcePath, file.getAbsolutePath(), file.getName());
            	foundFiles.add(new SourceFileDTO(file, packageParent));
            }
        }
    }
    
    private String getProjectSourceFolderPath(IProject project) throws Exception {
        IJavaProject javaProject = JavaCore.create(project);
        String fallback = null;
        try {
            for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    String absolutePath = root.getResource().getLocation().toOSString();
                    if (root.getPath().toString().contains("src/main/java")) {
                        return absolutePath;
                    }
                    if (fallback == null) fallback = absolutePath;
                }
            }
        } catch (JavaModelException e) {
            throw new Exception("Failed to access java project source folder");
        }
        return fallback;
    }

    private String getJavaFileParentPackage(String sourcePath, String filePath, String fileName) {
        String fileRelativePath = filePath.replace(sourcePath, "");
        fileRelativePath = fileRelativePath.replace(File.separator + fileName, "");
        
        if (fileRelativePath.startsWith(File.separator)) {
        	fileRelativePath = fileRelativePath.substring(1);
        }
        
        return fileRelativePath.replace(File.separator, ".");
    }
}