package godclassinspector.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import godclassinspector.model.UmlClassDTO;
import godclassinspector.model.ClassDTO;

@RunWith(MockitoJUnitRunner.class)
public class UmlDiagramServiceImpTest {

	@InjectMocks
	private UmlDiagramServiceImp umlDiagramService;

	private List<File> tempFiles;

	@Before
	public void setUp() {
		tempFiles = new ArrayList<>();
	}

	@After
	public void tearDown() {
		for (File file : tempFiles) {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	@Test
	public void testExtractClassesFeatures_WithValidSourceFiles() throws Exception {
		File tempFile = File.createTempFile("TestClass", ".java");
		tempFiles.add(tempFile);
		String content = "package com.example;\npublic class TestClass {\n" + "public String publicField;\n"
				+ "private int privateField;\n" + "protected double protectedField;\n"
				+ "public void publicMethod() {}\n" + "private void privateMethod() {}\n"
				+ "protected void protectedMethod() {}\n" + "}";
		Files.write(tempFile.toPath(), content.getBytes());

		ClassDTO sourceFile = new ClassDTO(tempFile, "com.example");
		sourceFile.setGodClass(true);
		List<ClassDTO> files = new ArrayList<>();
		files.add(sourceFile);

		List<UmlClassDTO> result = umlDiagramService.extractClassesFeatures(files);

		assertNotNull(result);
		assertEquals(1, result.size());
		UmlClassDTO classDto = result.get(0);
		assertEquals(sourceFile.getClassName(), classDto.getClassName());
		assertEquals("com.example", classDto.getPackageName());
		assertEquals(3, classDto.getFields().size());
		assertEquals(3, classDto.getMethods().size());
		assertTrue(classDto.getFields().get(0).contains("+"));
		assertTrue(classDto.getFields().get(1).contains("-"));
		assertTrue(classDto.getFields().get(2).contains("#"));
		assertTrue(classDto.isGodClass());
	}

	@Test
	public void testExtractClassesFeatures_WithInterface() throws Exception {
		File tempFile = File.createTempFile("TestInterface", ".java");
		tempFiles.add(tempFile);
		String content = "package com.example;\npublic interface TestInterface { void method1(); }";
		Files.write(tempFile.toPath(), content.getBytes());

		ClassDTO sourceFile = new ClassDTO(tempFile, "com.example");
		List<ClassDTO> files = new ArrayList<>();
		files.add(sourceFile);

		List<UmlClassDTO> result = umlDiagramService.extractClassesFeatures(files);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.get(0).isInterface());
	}

	@Test
	public void testExtractClassesFeatures_WithAbstractClassAndInheritance() throws Exception {
		File tempFile = File.createTempFile("TestAbstract", ".java");
		tempFiles.add(tempFile);
		String content = "package com.example;\npublic abstract class TestAbstract extends BaseClass { abstract void method1(); }";
		Files.write(tempFile.toPath(), content.getBytes());

		ClassDTO sourceFile = new ClassDTO(tempFile, "com.example");
		List<ClassDTO> files = new ArrayList<>();
		files.add(sourceFile);

		List<UmlClassDTO> result = umlDiagramService.extractClassesFeatures(files);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.get(0).isAbstract());
		assertEquals("BaseClass", result.get(0).getSuperClassName());
	}

	@Test
	public void testExtractClassesFeatures_WithEmptyFileList() throws Exception {
		List<ClassDTO> files = new ArrayList<>();

		List<UmlClassDTO> result = umlDiagramService.extractClassesFeatures(files);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testExtractClassesFeatures_WithStaticMembers() throws Exception {
		File tempFile = File.createTempFile("StaticTest", ".java");
		tempFiles.add(tempFile);
		String content = "package com.example;\npublic class StaticTest { public static String staticField; public static void staticMethod() {} }";
		Files.write(tempFile.toPath(), content.getBytes());

		ClassDTO sourceFile = new ClassDTO(tempFile, "com.example");
		List<ClassDTO> files = new ArrayList<>();
		files.add(sourceFile);

		List<UmlClassDTO> result = umlDiagramService.extractClassesFeatures(files);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.get(0).getFields().get(0).contains("{static}"));
		assertTrue(result.get(0).getMethods().get(0).contains("{static}"));
	}
}