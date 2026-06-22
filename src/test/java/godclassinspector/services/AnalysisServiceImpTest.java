package godclassinspector.services;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import godclassinspector.models.MetricsThresholdDTO;
import godclassinspector.models.ClassDTO;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisServiceImpTest {

    @InjectMocks
    private DetectionServiceImp analysisService;

    private ClassDTO sourceFile;
    private File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("TestClass", ".java");
        Files.write(tempFile.toPath(), "public class TestClass { private int x; public void method() { x = 1; } }".getBytes());
        sourceFile = new ClassDTO(tempFile, "com.example");
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testCheckGodClass_WithGodClass() throws Exception {
        int originalWmc = MetricsThresholdDTO.getWmcThreshold();
        int originalAtfd = MetricsThresholdDTO.getAtfdThreshold();
        double originalTcc = MetricsThresholdDTO.getTccThreshold();

        try {
            MetricsThresholdDTO.setWmcThreshold(0);
            MetricsThresholdDTO.setAtfdThreshold(-1);
            MetricsThresholdDTO.setTccThreshold(1.0);

            List<ClassDTO> files = new ArrayList<>();
            files.add(sourceFile);
            
            analysisService.checkGodClass(files);
            
            assertTrue(sourceFile.isGodClass());
        } finally {
            MetricsThresholdDTO.setWmcThreshold(originalWmc);
            MetricsThresholdDTO.setAtfdThreshold(originalAtfd);
            MetricsThresholdDTO.setTccThreshold(originalTcc);
        }
    }

    @Test
    public void testCheckGodClass_WithNormalClass() throws Exception {
        List<ClassDTO> files = new ArrayList<>();
        files.add(sourceFile);
        
        analysisService.checkGodClass(files);
        
        assertFalse(sourceFile.isGodClass());
    }

    @Test
    public void testCheckGodClass_WithEmptyList() throws Exception {
        List<ClassDTO> files = new ArrayList<>();
        
        analysisService.checkGodClass(files);
        
        assertTrue(files.isEmpty());
    }

    @Test(expected = Exception.class)
    public void testCheckGodClass_WithParseProblem() throws Exception {
        Files.write(tempFile.toPath(), "public class { invalid code".getBytes());
        List<ClassDTO> files = new ArrayList<>();
        files.add(sourceFile);
        
        analysisService.checkGodClass(files);
    }

    @Test(expected = Exception.class)
    public void testCheckGodClass_WithMissingFile() throws Exception {
        tempFile.delete();
        List<ClassDTO> files = new ArrayList<>();
        files.add(sourceFile);
        
        analysisService.checkGodClass(files);
    }

    @Test
    public void testGetMethodToFields_WithValidMethods() {
        String sampleJavaCode = "public class TestClass {\n private String field1;\n public void method1() { field1 = \"test\"; }\n }";
        CompilationUnit cu = StaticJavaParser.parse(sampleJavaCode);
        
        Map<MethodDeclaration, Set<String>> result = analysisService.getMethodToFields(cu);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void testGetMethodToFields_WithExcludedMethods() {
        String sampleJavaCode = "public class TestClass {\n private String field1;\n public String getField1() { return field1; }\n public static void method2() { }\n }";
        CompilationUnit cu = StaticJavaParser.parse(sampleJavaCode);
        
        Map<MethodDeclaration, Set<String>> result = analysisService.getMethodToFields(cu);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}