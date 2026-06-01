package godclassinspector.services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import godclassinspector.model.ClassDTO;

@RunWith(MockitoJUnitRunner.class)
public class DiscoveryServiceImpTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @InjectMocks
    private DiscoveryServiceImp discoveryService;

    @Mock
    private IEvaluationContext mockContext;

    @Mock
    private IProject mockProject;

    @Mock
    private IJavaProject mockJavaProject;

    @Mock
    private IPackageFragmentRoot mockRoot;

    @Mock
    private IResource mockResource;

    private File tempRootDir;

    @Before
    public void setUp() throws Exception {
        tempRootDir = File.createTempFile("mockProject", "");
        tempRootDir.delete();
        tempRootDir.mkdir();
    }

    @After
    public void tearDown() {
        deleteDirectory(tempRootDir);
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        if (directoryToBeDeleted == null || !directoryToBeDeleted.exists()) {
            return;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    @Test
    public void testDetectProject_Success() throws Exception {
        IStructuredSelection selection = new StructuredSelection(mockProject);
        
        when(mockContext.getVariable("selection")).thenReturn(selection);
        lenient().when(mockContext.getVariable("activeMenuSelection")).thenReturn(selection);
        ExecutionEvent event = new ExecutionEvent(null, Collections.emptyMap(), null, mockContext);

        when(mockProject.isOpen()).thenReturn(true);
        lenient().when(mockProject.hasNature(anyString())).thenReturn(true);
        lenient().when(mockProject.getAdapter(IProject.class)).thenReturn(mockProject);
        lenient().when(mockProject.getAdapter(IJavaProject.class)).thenReturn(mockJavaProject);
        lenient().when(mockJavaProject.getProject()).thenReturn(mockProject);

        IProject result = discoveryService.detectProject(event);
        
        assertEquals(mockProject, result);
    }

    @Test(expected = Exception.class)
    public void testDetectProject_ClosedProject() throws Exception {
        IStructuredSelection selection = new StructuredSelection(mockProject);
        
        when(mockContext.getVariable("selection")).thenReturn(selection);
        lenient().when(mockContext.getVariable("activeMenuSelection")).thenReturn(selection);
        ExecutionEvent event = new ExecutionEvent(null, Collections.emptyMap(), null, mockContext);

        when(mockProject.isOpen()).thenReturn(false);
        lenient().when(mockProject.hasNature(anyString())).thenReturn(true);
        lenient().when(mockProject.getAdapter(IProject.class)).thenReturn(mockProject);

        discoveryService.detectProject(event);
    }

    @Test(expected = Exception.class)
    public void testDetectProject_EmptySelection() throws Exception {
        when(mockContext.getVariable("selection")).thenReturn(StructuredSelection.EMPTY);
        lenient().when(mockContext.getVariable("activeMenuSelection")).thenReturn(StructuredSelection.EMPTY);
        ExecutionEvent event = new ExecutionEvent(null, Collections.emptyMap(), null, mockContext);

        discoveryService.detectProject(event);
    }

    @Test
    public void testFindAllJavaFiles_Success() throws Exception {
        File srcDir = new File(tempRootDir, "src");
        srcDir.mkdir();
        File packageDir = new File(srcDir, "com" + File.separator + "example");
        packageDir.mkdirs();
        
        File javaFile = new File(packageDir, "MyClass.java");
        javaFile.createNewFile();
        File textFile = new File(packageDir, "readme.txt");
        textFile.createNewFile();

        try (MockedStatic<JavaCore> javaCoreMock = mockStatic(JavaCore.class)) {
            javaCoreMock.when(() -> JavaCore.create(mockProject)).thenReturn(mockJavaProject);
            when(mockJavaProject.getPackageFragmentRoots()).thenReturn(new IPackageFragmentRoot[]{mockRoot});
            when(mockRoot.getKind()).thenReturn(IPackageFragmentRoot.K_SOURCE);
            when(mockRoot.getResource()).thenReturn(mockResource);
            
            Path realLocation = new Path(srcDir.getAbsolutePath());
            Path realPath = new Path("src/main/java");
            
            when(mockResource.getLocation()).thenReturn(realLocation);
            when(mockRoot.getPath()).thenReturn(realPath);

            List<ClassDTO> result = discoveryService.findAllJavaFiles(mockProject);
            
            assertEquals(1, result.size());
            assertEquals("MyClass", result.get(0).getClassName());
            assertEquals("com.example", result.get(0).getParentPackageName());
        }
    }

    @Test
    public void testFindAllJavaFiles_FallbackPath() throws Exception {
        File srcDir = new File(tempRootDir, "custom_src");
        srcDir.mkdir();
        File javaFile = new File(srcDir, "FallbackClass.java");
        javaFile.createNewFile();

        try (MockedStatic<JavaCore> javaCoreMock = mockStatic(JavaCore.class)) {
            javaCoreMock.when(() -> JavaCore.create(mockProject)).thenReturn(mockJavaProject);
            when(mockJavaProject.getPackageFragmentRoots()).thenReturn(new IPackageFragmentRoot[]{mockRoot});
            when(mockRoot.getKind()).thenReturn(IPackageFragmentRoot.K_SOURCE);
            when(mockRoot.getResource()).thenReturn(mockResource);
            
            Path realLocation = new Path(srcDir.getAbsolutePath());
            Path realPath = new Path("custom_src");
            
            when(mockResource.getLocation()).thenReturn(realLocation);
            when(mockRoot.getPath()).thenReturn(realPath);

            List<ClassDTO> result = discoveryService.findAllJavaFiles(mockProject);
            
            assertEquals(1, result.size());
            assertEquals("FallbackClass", result.get(0).getClassName());
            assertEquals("", result.get(0).getParentPackageName());
        }
    }

    @Test(expected = Exception.class)
    public void testFindAllJavaFiles_JavaModelException() throws Exception {
        try (MockedStatic<JavaCore> javaCoreMock = mockStatic(JavaCore.class)) {
            javaCoreMock.when(() -> JavaCore.create(mockProject)).thenReturn(mockJavaProject);
            when(mockJavaProject.getPackageFragmentRoots()).thenThrow(new JavaModelException(new RuntimeException(), 0));

            discoveryService.findAllJavaFiles(mockProject);
        }
    }
}