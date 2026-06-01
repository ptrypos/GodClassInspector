package godclassinspector.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;

import godclassinspector.model.MetricsThresholdDTO;
import godclassinspector.model.ClassDTO;

public class DetectionServiceImp implements DetectionService {

	@Override
	public void checkGodClass(List<ClassDTO> files) throws Exception {
		for (ClassDTO projectFile : files) {
			this.calculateMetrics(projectFile);
			boolean isGodClass = this.isGodClass(projectFile);
			projectFile.setGodClass(isGodClass);
		}
	}

	@Override
	public Map<MethodDeclaration, Set<String>> getMethodToFields(CompilationUnit compilationUnit) {
		Map<MethodDeclaration, Set<String>> methodToFields = new HashMap<>();

		Set<String> actualClassFields = new HashSet<>();
		compilationUnit.findAll(FieldDeclaration.class).forEach(fd -> {
			fd.getVariables().forEach(var -> actualClassFields.add(var.getNameAsString()));
		});

		for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
			if (!isExcludedMethod(method)) {
				Set<String> usedFields = new HashSet<>();
				List<NameExpr> accessedNames = method.findAll(NameExpr.class);

				for (NameExpr name : accessedNames) {
					if (actualClassFields.contains(name.getNameAsString())) {
						usedFields.add(name.getNameAsString());
					}
				}

				methodToFields.put(method, usedFields);
			}
		}

		return methodToFields;
	}

	private boolean isGodClass(ClassDTO file) {
		int wmcThreshold = MetricsThresholdDTO.getWmcThreshold();
		int atfdThreshold = MetricsThresholdDTO.getAtfdThreshold();
		double tccThreshold = MetricsThresholdDTO.getTccThreshold();

		int wmcValue = file.getWeightedMethodCount();
		int atfdValue = file.getAccessToForeignData();
		double tccValue = file.getTightClassCohesion();

		boolean isWmcGreater = (wmcValue >= wmcThreshold);
		boolean isAtfdGreater = (atfdValue > atfdThreshold);
		boolean isTccLess = (tccValue < tccThreshold);

		if (isWmcGreater && isAtfdGreater && isTccLess) {
			return true;
		} else {
			return false;
		}
	}

	private void calculateMetrics(ClassDTO sourceFile) throws Exception {
		try {
			File fileToAnalyze = new File(sourceFile.getAbsolutePath());

			CompilationUnit compilationUnit = StaticJavaParser.parse(fileToAnalyze);

			int weightedMethodCount = calculateWeightedMethodCount(compilationUnit);
			int accessToForeignData = calculateAccessToForeignData(compilationUnit);
			double tightClassCohesion = calculateTightClassCohesion(compilationUnit);
			Map<String, Double> methodsLocalityOfAttributeAccess = new HashMap<>();
			Map<String, Integer> methodsForeignDataProviders = new HashMap<>();

			for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
				double methodLocalityOfAttributeAccess = calculateLocalityOfAttributeAccess(method);
				int methodForeignDataProviders = calculateForeignDataProviders(method);

				String methodKey = method.getNameAsString();
				methodsLocalityOfAttributeAccess.put(methodKey, methodLocalityOfAttributeAccess);
				methodsForeignDataProviders.put(methodKey, methodForeignDataProviders);
			}

			sourceFile.setWeightedMethodCount(weightedMethodCount);
			sourceFile.setAccessToForeignData(accessToForeignData);
			sourceFile.setTightClassCohesion(tightClassCohesion);
			sourceFile.setLocalityOfAttributeAccess(methodsLocalityOfAttributeAccess);
			sourceFile.setForeignDataProviders(methodsForeignDataProviders);

		} catch (NullPointerException nullPointerFile) {
			throw new Exception("No file was found to analyze. Please make sure you have scanned the project first.");
		} catch (ParseProblemException javaParserProblem) {
			throw new Exception(
					"A Java file could not be parsed. It may contain syntax errors. Please fix any compilation errors in your project and try again.");
		}
	}

	private int calculateWeightedMethodCount(CompilationUnit compilationUnit) {
		List<MethodDeclaration> classMethods = compilationUnit.findAll(MethodDeclaration.class);

		int totalComplexity = 0;

		for (MethodDeclaration method : classMethods) {
			int methodWeight = 1;

			methodWeight += method.findAll(Statement.class, n -> n instanceof IfStmt || n instanceof ForStmt
					|| n instanceof ForEachStmt || n instanceof WhileStmt || n instanceof DoStmt).size();

			methodWeight += method.findAll(SwitchEntry.class).size();
			methodWeight += method.findAll(CatchClause.class).size();
			methodWeight += method.findAll(BinaryExpr.class,
					binaryExpression -> binaryExpression.getOperator() == BinaryExpr.Operator.AND
							|| binaryExpression.getOperator() == BinaryExpr.Operator.OR)
					.size();

			totalComplexity = totalComplexity + methodWeight;
		}

		return totalComplexity;
	}

	private int calculateAccessToForeignData(CompilationUnit compilationUnit) {
		List<MethodCallExpr> methodsCalled = compilationUnit.findAll(MethodCallExpr.class);
		List<FieldAccessExpr> accessedFields = compilationUnit.findAll(FieldAccessExpr.class);

		int accessToForeignDataCounter = 0;

		for (MethodCallExpr call : methodsCalled) {
			if (call.getNameAsString().startsWith("get")) {
				if (call.getScope().isPresent()) {
					String classParent = call.getScope().get().toString();

					if (!classParent.equals("this") && !classParent.equals("System")) {
						accessToForeignDataCounter++;
					}
				}
			}
		}

		for (FieldAccessExpr field : accessedFields) {
			String classParent = field.getScope().toString();

			if (!classParent.equals("this") && !classParent.equals("System")) {
				accessToForeignDataCounter++;
			}
		}

		return accessToForeignDataCounter;
	}

	private double calculateTightClassCohesion(CompilationUnit compilationUnit) {
		Map<MethodDeclaration, Set<String>> methodToFields = getMethodToFields(compilationUnit);
		List<MethodDeclaration> methods = new ArrayList<>(methodToFields.keySet());

		double tightClassCohesion = 0;
		int totalPossiblePairs = 0;
		int connectedPairs = 0;
		int numberOfMethods = methods.size();

		for (int i = 0; i < numberOfMethods; i++) {
			for (int j = i + 1; j < numberOfMethods; j++) {
				Set<String> methodFieldsA = methodToFields.get(methods.get(i));
				Set<String> methodFieldsB = methodToFields.get(methods.get(j));

				if (methodFieldsA.stream().anyMatch(methodFieldsB::contains)) {
					connectedPairs++;
				}
			}
		}

		totalPossiblePairs = (numberOfMethods * (numberOfMethods - 1)) / 2;

		if (totalPossiblePairs > 0) {
			tightClassCohesion = (double) connectedPairs / totalPossiblePairs;
			tightClassCohesion = Math.round(tightClassCohesion * 1000.0f) / 1000.0f;
		}

		return tightClassCohesion;
	}

	private double calculateLocalityOfAttributeAccess(MethodDeclaration method) {
		Set<String> localAccesses = new HashSet<>();
		Set<String> foreignAccesses = new HashSet<>();

		method.findAll(FieldAccessExpr.class).forEach(fa -> {
			String scope = fa.getScope().toString();
			if (scope.equals("this")) {
				localAccesses.add(fa.getNameAsString());
			} else if (!scope.equals("System")) {
				foreignAccesses.add(fa.getNameAsString());
			}
		});

		method.findAll(MethodCallExpr.class).forEach(mc -> {
			if (mc.getNameAsString().startsWith("get") && mc.getScope().isPresent()) {
				String scope = mc.getScope().get().toString();
				if (scope.equals("this")) {
					localAccesses.add(mc.getNameAsString());
				} else if (!scope.equals("System")) {
					foreignAccesses.add(mc.getNameAsString());
				}
			}
		});

		int total = localAccesses.size() + foreignAccesses.size();
		return (total == 0) ? 1.0 : (double) localAccesses.size() / total;
	}

	private int calculateForeignDataProviders(MethodDeclaration method) {
		Set<String> providers = new HashSet<>();

		for (FieldAccessExpr fieldAccess : method.findAll(FieldAccessExpr.class)) {
			String fieldScope = fieldAccess.getScope().toString();

			if (!fieldScope.equals("this")) {
				providers.add(fieldScope);
			}
		}

		for (MethodCallExpr methodAccess : method.findAll(MethodCallExpr.class)) {
			String methodName = methodAccess.getNameAsString();

			if (methodName.startsWith("get") && methodAccess.getScope().isPresent()) {
				String methodScope = methodAccess.getScope().get().toString();

				if (!methodScope.equals("this")) {
					providers.add(methodScope);
				}
			}
		}

		return providers.size();
	}

	private boolean isExcludedMethod(MethodDeclaration method) {
		if (method.isStatic()) {
			return true;
		}

		String methodName = method.getNameAsString();

		return methodName.startsWith("get") || methodName.startsWith("set") || methodName.startsWith("add")
				|| methodName.startsWith("equals") || methodName.equals("toString") || methodName.equals("hashCode");
	}
}