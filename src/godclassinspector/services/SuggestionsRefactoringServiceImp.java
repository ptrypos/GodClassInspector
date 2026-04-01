package godclassinspector.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import godclassinspector.model.MetricsThresholdDTO;
import godclassinspector.model.SourceFileDTO;

public class SuggestionsRefactoringServiceImp implements SuggestionsRefactoringService {

	private final AnalysisService analysisService = new AnalysisServiceImp();

	@Override
	public Map<String, String> suggestRefactoring(List<SourceFileDTO> files) throws Exception {
		Map<String, String> refactoringSuggestions = new HashMap<>();

		for (SourceFileDTO file : files) {
			if (!file.isGodClass()) {
				continue;
			}

			List<String> suggestions = new ArrayList<>();

			String hfdaSuggestion = suggestionsForHfda(file);
			if (!hfdaSuggestion.isEmpty()) {
				suggestions.add(hfdaSuggestion);
			}

			String wmcSuggestion = suggestionsForWmc(file);
			if (!wmcSuggestion.isEmpty()) {
				suggestions.add(wmcSuggestion);
			}

			String tccSuggestion = suggestionsForTcc(file);
			if (!tccSuggestion.isEmpty()) {
				suggestions.add(tccSuggestion);
			}

			String consolidatedSuggestion = String.join(";", suggestions);
			if (!consolidatedSuggestion.isEmpty()) {
				String className = file.getFileName().replace(".java", "");
				refactoringSuggestions.put(className, consolidatedSuggestion);
			}
		}

		return refactoringSuggestions;
	}

	private String suggestionsForWmc(SourceFileDTO file) {
		String suggestions = "";

		double wmcThreshold = MetricsThresholdDTO.getWmcThreshold();
		double wmcValue = file.getWeightedMethodCount();
		boolean isWmcHigh = wmcValue > wmcThreshold;

		if (!isWmcHigh) {
			return suggestions;
		}

		suggestions = "WMC: The class has " + wmcValue + " methods. Group related methods into separate classes.";

		return suggestions;
	}

	private String suggestionsForHfda(SourceFileDTO file) {
		String suggestions = "";

		int atfdThreshold = MetricsThresholdDTO.getAtfdThreshold();
		int atfdValue = file.getAccessToForeignData();
		boolean isAtfdHigh = atfdValue > atfdThreshold;

		if (!isAtfdHigh) {
			return suggestions;
		}

		suggestions = "ATFD: Accesses " + atfdValue + " foreign classes. Move logic closer to data.";

		return suggestions;
	}

	private String suggestionsForTcc(SourceFileDTO file) throws Exception {
		String suggestions = "";

		double tccThreshold = MetricsThresholdDTO.getTccThreshold();
		double tccValue = file.getTightClassCohesion();
		boolean isTccLow = tccValue < tccThreshold;

		if (!isTccLow) {
			return suggestions;
		}

		String className = file.getFileName().replace(".java", "");

		try {
			File sourceFile = new File(file.getAbsolutePath());
			CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile);
			Map<MethodDeclaration, Set<String>> methodToFields = analysisService.getMethodToFields(compilationUnit);
			List<List<String>> methodGroups = groupMethodsBySharedFields(methodToFields);

			boolean hasMultipleGroups = methodGroups.size() > 1;

			if (!hasMultipleGroups) {
				return suggestions;
			}

			List<String> groupSuggestions = new ArrayList<>();
			for (List<String> group : methodGroups) {
				groupSuggestions.add(buildCohesionSuggestion(group, className));
			}
			suggestions = String.join(" ", groupSuggestions);

		} catch (ParseProblemException e) {
			throw new Exception("Problem on parsing the file.");
		}

		return suggestions;
	}

	private List<List<String>> groupMethodsBySharedFields(Map<MethodDeclaration, Set<String>> methodToFields) {
		List<MethodDeclaration> methods = new ArrayList<>(methodToFields.keySet());
		int numberOfMethods = methods.size();

		int[] parent = new int[numberOfMethods];
		for (int i = 0; i < numberOfMethods; i++) {
			parent[i] = i;
		}

		for (int i = 0; i < numberOfMethods; i++) {
			for (int j = i + 1; j < numberOfMethods; j++) {
				Set<String> fieldsA = methodToFields.get(methods.get(i));
				Set<String> fieldsB = methodToFields.get(methods.get(j));
				boolean shareField = fieldsA.stream().anyMatch(fieldsB::contains);
				if (shareField) {
					union(parent, i, j);
				}
			}
		}

		Map<Integer, List<String>> groupMap = new LinkedHashMap<>();
		for (int i = 0; i < numberOfMethods; i++) {
			int root = find(parent, i);
			String methodName = methods.get(i).getNameAsString();
			groupMap.computeIfAbsent(root, k -> new ArrayList<>()).add(methodName);
		}

		return new ArrayList<>(groupMap.values());
	}

	private String buildCohesionSuggestion(List<String> methodGroup, String className) {
		String methodList = String.join(", ", methodGroup);
		return "TCC: Methods [" + methodList + "] can be extracted to a new class.";
	}

	private int find(int[] parent, int i) {
		if (parent[i] != i) {
			parent[i] = find(parent, parent[i]);
		}
		return parent[i];
	}

	private void union(int[] parent, int i, int j) {
		int rootI = find(parent, i);
		int rootJ = find(parent, j);
		if (rootI != rootJ) {
			parent[rootI] = rootJ;
		}
	}
}