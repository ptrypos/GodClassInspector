package godclassinspector.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;

import godclassinspector.model.MetricsThresholdDTO;
import godclassinspector.model.SourceFileDTO;

public class SuggestionsRefactoringServiceImp implements SuggestionsRefactoringService {

	private static double OVERLAP_FIELDS_METHODS = 0.30;

	private final AnalysisService analysisService = new AnalysisServiceImp();

	@Override
	public Map<String, Map<String, String>> suggestRefactoring(List<SourceFileDTO> files) throws Exception {
		Map<String, Map<String, String>> refactoringSuggestions = new HashMap<>();

		for (SourceFileDTO file : files) {
			if (!file.isGodClass()) {
				continue;
			}

			Map<String, String> typeToSuggestions = new HashMap<>();

			String extractMethodSuggestions = getExtractMethodSuggestions(file);
			if (!extractMethodSuggestions.isEmpty()) {
				typeToSuggestions.put("Extract Method", extractMethodSuggestions);
			}

			String moveMethodSuggestions = getMoveMethodSuggestions(file);
			if (!moveMethodSuggestions.isEmpty()) {
				typeToSuggestions.put("Move Method", moveMethodSuggestions);
			}

			String extractClassSuggestions = getExtractClassSuggestions(file);
			if (!extractClassSuggestions.isEmpty()) {
				typeToSuggestions.put("Extract Class", extractClassSuggestions);
			}

			if (!typeToSuggestions.isEmpty()) {
				refactoringSuggestions.put(file.getClassName(), typeToSuggestions);
			}
		}

		return refactoringSuggestions;
	}

	private String getExtractMethodSuggestions(SourceFileDTO file) throws Exception {
		File sourceFile = new File(file.getAbsolutePath());
		CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile);

		List<String> suggestions = new ArrayList<>();
		Map<String, Integer> methodComplexity = calculateMethodComplexity(compilationUnit);
		int extractMethodThreshold = MetricsThresholdDTO.getExtractMethodThreshold();

		for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
			// Skip excluded methods (getters, setters, etc.)
			if (isExcludedMethod(method.getNameAsString())) {
				continue;
			}

			String methodName = method.getNameAsString();
			Integer complexity = methodComplexity.get(methodName);

			if (complexity != null && complexity >= extractMethodThreshold) {
				// Identify complex code blocks within the method
				List<String> complexBlocks = identifyComplexCodeBlocks(method);

				if (!complexBlocks.isEmpty()) {
					String suggestion = getExtractMethodSuggestionAsString(methodName, complexity, complexBlocks);
					suggestions.add(suggestion);
				}
			}
		}

		return String.join(" | ", suggestions);
	}

	private Map<String, Integer> calculateMethodComplexity(CompilationUnit compilationUnit) {
		Map<String, Integer> methodComplexity = new HashMap<>();

		for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
			int complexity = 1;

			complexity += method.findAll(IfStmt.class).size();
			complexity += method.findAll(ForStmt.class).size();
			complexity += method.findAll(ForEachStmt.class).size();
			complexity += method.findAll(WhileStmt.class).size();
			complexity += method.findAll(DoStmt.class).size();
			complexity += method.findAll(CatchClause.class).size();
			complexity += method.findAll(SwitchEntry.class).size();

			complexity += method.findAll(BinaryExpr.class, binaryExpression ->
				binaryExpression.getOperator() == BinaryExpr.Operator.AND ||
				binaryExpression.getOperator() == BinaryExpr.Operator.OR
			).size();

			methodComplexity.put(method.getNameAsString(), complexity);
		}

		return methodComplexity;
	}

	private List<String> identifyComplexCodeBlocks(MethodDeclaration method) {
		List<String> complexBlocks = new ArrayList<>();
		int blockCounter = 0;

		List<IfStmt> ifStatements = method.findAll(IfStmt.class);
		for (IfStmt ifStmt : ifStatements) {
			int nestedDepth = calculateNestingDepth(ifStmt);
			if (nestedDepth > 1) {
				blockCounter++;
				complexBlocks.add("Nested conditional block #" + blockCounter);
			}
		}

		List<ForStmt> forLoops = method.findAll(ForStmt.class);
		for (ForStmt forStmt : forLoops) {
			int statementsInLoop = forStmt.getBody().findAll(Statement.class).size();
			if (statementsInLoop > 2) {
				blockCounter++;
				complexBlocks.add("Complex loop logic block #" + blockCounter);
			}
		}

		List<CatchClause> catchClauses = method.findAll(CatchClause.class);
		for (CatchClause catchClause : catchClauses) {
			int statementsInCatch = catchClause.getBody().getStatements().size();
			if (statementsInCatch > 2) {
				blockCounter++;
				complexBlocks.add("Complex exception handling block #" + blockCounter);
			}
		}

		return complexBlocks;
	}

	private int calculateNestingDepth(Node node) {
		int depth = 0;
		Node current = node;

		while (current != null) {
			if (current instanceof IfStmt || current instanceof ForStmt ||
				current instanceof ForEachStmt || current instanceof WhileStmt ||
				current instanceof DoStmt) {
				depth++;
			}
			current = current.getParentNode().orElse(null);
		}

		return depth;
	}

	private String getExtractMethodSuggestionAsString(String methodName, Integer complexity,
			   List<String> complexBlocks) {
		String blockDescription = String.join("; ", complexBlocks);
		return "Method [" + methodName + "] has high complexity (" + complexity +
		"). Consider extracting: " + blockDescription + ".";
	}

	private String getMoveMethodSuggestions(SourceFileDTO file) throws FileNotFoundException {
		File sourceFile = new File(file.getAbsolutePath());
		CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile);

		List<String> suggestions = new ArrayList<>();
		Map<String, Double> laaMap = file.getLocalityOfAttributeAccess();

		for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
			String methodName = method.getNameAsString();

			if (laaMap.containsKey(methodName) && laaMap.get(methodName) < MetricsThresholdDTO.getLaaThreshold()) {
				int foreignDataProviders = file.getForeignDataProviders().get(methodName);

				if (foreignDataProviders == MetricsThresholdDTO.getMoveMethodThreshold()) {
					String targetClass = findTargetClass(method);
					String suggestion = getMoveMethodSuggestionAsString(methodName, targetClass);
					suggestions.add(suggestion);
				}
			}
		}

		return String.join(" | ", suggestions);
	}

	private String getMoveMethodSuggestionAsString(String methodName, String targetClass) {
		String suggestion = "Method [" + methodName + "] is tightly coupled with " + targetClass
				+ ". Move this method to " + targetClass + ".";
		return suggestion;
	}

	private String findTargetClass(MethodDeclaration method) {
		Map<String, Integer> providerCounts = new HashMap<>();

		method.findAll(FieldAccessExpr.class).forEach(fa -> {
			String scope = fa.getScope().toString();
			if (!scope.equals("this")) {
				providerCounts.put(scope, providerCounts.getOrDefault(scope, 0) + 1);
			}
		});

		method.findAll(MethodCallExpr.class).forEach(mc -> {
			if (mc.getNameAsString().startsWith("get") && mc.getScope().isPresent()) {
				String scope = mc.getScope().get().toString();
				if (!scope.equals("this")) {
					providerCounts.put(scope, providerCounts.getOrDefault(scope, 0) + 1);
				}
			}
		});

		return providerCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
				.orElse("UnknownClass");
	}

	private String getExtractClassSuggestions(SourceFileDTO file) throws Exception {
		String suggestions = "";

		double tccThreshold = MetricsThresholdDTO.getTccThreshold();
		double tccValue = file.getTightClassCohesion();
		boolean isTccLow = tccValue < tccThreshold;

		if (!isTccLow) {
			return suggestions;
		}

		String className = file.getClassName();

		try {
			File sourceFile = new File(file.getAbsolutePath());
			CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile);
			Map<MethodDeclaration, Set<String>> methodToFields = analysisService.getMethodToFields(compilationUnit);
			List<List<String>> methodGroups = groupMethodsBySharedFields(methodToFields);
			List<List<String>> methodGroupsCleaned = this.filterMethodsForRefactoring(methodGroups);

			boolean hasMultipleGroups = methodGroupsCleaned.size() > 1;

			if (!hasMultipleGroups) {
				return suggestions;
			}

			List<String> groupSuggestions = new ArrayList<>();
			for (List<String> group : methodGroupsCleaned) {
				groupSuggestions.add(getExtractClassSuggestionAsString(group, className));
			}

			suggestions = String.join(" | ", groupSuggestions);

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
				MethodDeclaration methodA = methods.get(i);
				MethodDeclaration methodB = methods.get(j);

				Set<String> fieldsA = methodToFields.get(methodA);
				Set<String> fieldsB = methodToFields.get(methodB);

				boolean aCallsB = isMethodCalling(methodA, methodB);
		        boolean bCallsA = isMethodCalling(methodB, methodA);

		        double overlap = calculateJaccardSimilarity(fieldsA, fieldsB);

				if (overlap > OVERLAP_FIELDS_METHODS || aCallsB || bCallsA) {
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

	private boolean isMethodCalling(MethodDeclaration source, MethodDeclaration target) {
	    String targetName = target.getNameAsString();

	    List<MethodCallExpr> calls = source.findAll(MethodCallExpr.class);

	    for (MethodCallExpr call : calls) {
	        if (call.getNameAsString().equals(targetName)) {
	            if (!call.getScope().isPresent() || call.getScope().get().toString().equals("this")) {
	                return true;
	            }
	        }
	    }

	    return false;
	}

	private double calculateJaccardSimilarity(Set<String> fieldsA, Set<String> fieldsB) {
		double jaccardSimilarity = 0;

		if (fieldsA.isEmpty() && fieldsB.isEmpty()) {
			return jaccardSimilarity;
		}

		Set<String> intersection = new HashSet<>(fieldsA);
		intersection.retainAll(fieldsB);

		Set<String> union = new HashSet<>(fieldsA);
		union.addAll(fieldsB);

		jaccardSimilarity = (double) intersection.size() / union.size();

		return jaccardSimilarity;
	}

	private List<List<String>> filterMethodsForRefactoring(List<List<String>> methodGroups) {
		List<List<String>> methodGroupsCleaned = new ArrayList<>();

		for (List<String> group : methodGroups) {
			List<String> groupCleaned = new ArrayList<>();
			for (String method : group) {
				if (!isExcludedMethod(method)) {
					groupCleaned.add(method);
				}
			}

			if (!groupCleaned.isEmpty() && groupCleaned.size() > 1) {
				methodGroupsCleaned.add(groupCleaned);
			}
		}

		return methodGroupsCleaned;
	}

	private boolean isExcludedMethod(String method) {
		return method.startsWith("get") || method.startsWith("set") || method.startsWith("add")
				|| method.startsWith("equals") || method.equals("toString") || method.equals("hashCode");
	}

	private String getExtractClassSuggestionAsString(List<String> methodGroup, String className) {
		String methodList = String.join(", ", methodGroup);
		return "Methods [" + methodList + "] can be extracted to a new class.";
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