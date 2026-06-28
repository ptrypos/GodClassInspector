package godclassinspector.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import godclassinspector.models.ClassDTO;

@RunWith(MockitoJUnitRunner.class)
public class SuggestionsServiceImpTest {

	@InjectMocks
	private SuggestionsServiceImp suggestionsService;

	private ClassDTO godClass;
	private ClassDTO normalClass;
	private File godFile;
	private File normalFile;

	@Before
	public void setUp() throws Exception {
		godFile = File.createTempFile("GodClass", ".java");

		String complexCode = "public class GodClass {\n" + "    private int x;\n" + "    private int y;\n"
				+ "    public void complexMethod() {\n" + "        if (true) {\n"
				+ "            for (int i=0; i<10; i++) {\n" + "                while (false) { x = 1; }\n"
				+ "            }\n" + "        }\n" + "    }\n" + "    public void methodA() { x = 1; }\n"
				+ "    public void methodB() { y = 1; }\n" + "    public void methodC() { y = 1; }\n" + "}";
		Files.write(godFile.toPath(), complexCode.getBytes());

		godClass = new ClassDTO(godFile, "godclassinspector.services");
		godClass.setGodClass(true);
		godClass.setTightClassCohesion(0.1);

		Map<String, Double> laaMap = new HashMap<>();
		laaMap.put("complexMethod", 0.1);
		godClass.setLocalityOfAttributeAccess(laaMap);

		Map<String, Integer> fdpMap = new HashMap<>();
		fdpMap.put("complexMethod", 1);
		godClass.setForeignDataProviders(fdpMap);

		normalFile = File.createTempFile("NormalClass", ".java");
		Files.write(normalFile.toPath(), "public class NormalClass { public void method() {} }".getBytes());

		normalClass = new ClassDTO(normalFile, "godclassinspector.services");
		normalClass.setGodClass(false);
	}

	@After
	public void tearDown() {
		if (godFile != null && godFile.exists()) {
			godFile.delete();
		}
		if (normalFile != null && normalFile.exists()) {
			normalFile.delete();
		}
	}

	@Test
	public void testSuggestRefactoring_WithGodClasses() throws Exception {
		List<ClassDTO> files = new ArrayList<>();
		files.add(godClass);
		files.add(normalClass);

		Map<String, Map<String, String>> result = suggestionsService.suggestRefactoring(files);

		assertNotNull(result);
		assertTrue(result.containsKey(godClass.getClassName()));
		assertFalse(result.containsKey(normalClass.getClassName()));

		Map<String, String> suggestions = result.get(godClass.getClassName());
		assertTrue(suggestions.containsKey("Extract Method"));
		assertTrue(suggestions.containsKey("Extract Class"));
	}

	@Test
	public void testSuggestRefactoring_WithNormalClassesOnly() throws Exception {
		List<ClassDTO> files = new ArrayList<>();
		files.add(normalClass);

		Map<String, Map<String, String>> result = suggestionsService.suggestRefactoring(files);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testSuggestRefactoring_WithEmptyFileList() throws Exception {
		List<ClassDTO> files = new ArrayList<>();

		Map<String, Map<String, String>> result = suggestionsService.suggestRefactoring(files);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test(expected = Exception.class)
	public void testSuggestRefactoring_WithParseProblem() throws Exception {
		Files.write(godFile.toPath(), "public class { syntax error".getBytes());
		List<ClassDTO> files = new ArrayList<>();
		files.add(godClass);

		suggestionsService.suggestRefactoring(files);
	}
}