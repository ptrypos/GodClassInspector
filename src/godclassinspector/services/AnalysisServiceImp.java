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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import godclassinspector.model.MetricsThresholdDTO;
import godclassinspector.model.SourceFileDTO;

public class AnalysisServiceImp implements AnalysisService {

	@Override
	public void checkGodClass(List<SourceFileDTO> files) throws Exception {
		for (SourceFileDTO projectFile : files) {
			this.calculateMetrics(projectFile);
			boolean isGodClass = this.isGodClass(projectFile);
			projectFile.setGodClass(isGodClass);
		}
	}

	private boolean isGodClass(SourceFileDTO file) {
		MetricsThresholdDTO.setWmcThreshold(0);
		MetricsThresholdDTO.setAtfdThreshold(0);
		MetricsThresholdDTO.setTccThreshold(1);
		
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

	private void calculateMetrics(SourceFileDTO sourceFile) throws Exception {
		try {
			File fileToAnalyze = new File(sourceFile.getAbsolutePath());

			CompilationUnit analyzeCompilationUnit = StaticJavaParser.parse(fileToAnalyze);

			int weightedMethodCount = calculateWeightedMethodCount(analyzeCompilationUnit);
			int accessToForeignData = calculateAccessToForeignData(analyzeCompilationUnit);
			float tightClassCohesion = calculateTightClassCohesion(analyzeCompilationUnit);

			sourceFile.setWeightedMethodCount(weightedMethodCount);
			sourceFile.setAccessToForeignData(accessToForeignData);
			sourceFile.setTightClassCohesion(tightClassCohesion);

		} catch (NullPointerException nullPointerFile) {
			throw new Exception("No file was found to analyze. Please make sure you have scanned the project first.");
		} catch (ParseProblemException javaParserProblem) {
			throw new Exception("A Java file could not be parsed. It may contain syntax errors. Please fix any compilation errors in your project and try again.");
		}
	}

	private int calculateWeightedMethodCount(CompilationUnit analyzeCompilationUnit) {
		List<MethodDeclaration> classMethods = analyzeCompilationUnit.findAll(MethodDeclaration.class);
		int weightedMethodsCount = classMethods.size();
		return weightedMethodsCount;
	}

	private int calculateAccessToForeignData(CompilationUnit analyzeCompilationUnit) {
		List<MethodCallExpr> methodsCalled = analyzeCompilationUnit.findAll(MethodCallExpr.class);
		List<FieldAccessExpr> accessedFields = analyzeCompilationUnit.findAll(FieldAccessExpr.class);

		Set<String> foreignClasses = new HashSet<>();

		int accessToForeignData = 0;

		for (MethodCallExpr call: methodsCalled) {
			if (call.getNameAsString().startsWith("get")) {
				if (call.getScope().isPresent()) {
					String classParent = call.getScope().get().toString();

					if(!classParent.equals("this")) {
						foreignClasses.add(classParent);
					}
				}
			}
		}

		for (FieldAccessExpr field: accessedFields) {
			String classParent = field.getScope().toString();

			if (!classParent.equals("this")) {
				foreignClasses.add(classParent);
			}
		}

		accessToForeignData = foreignClasses.size();
		return accessToForeignData;
	}

	private float calculateTightClassCohesion(CompilationUnit analyzeCompilationUnit) {
		Map<MethodDeclaration, Set<String>> methodToFields = getMethodToFields(analyzeCompilationUnit);
		List<MethodDeclaration> methods = new ArrayList<>(methodToFields.keySet());

		float tightClassCohesion = 0;
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
			tightClassCohesion = (float) connectedPairs / totalPossiblePairs;
			tightClassCohesion = Math.round(tightClassCohesion * 1000.0f) / 1000.0f;
		}

		return tightClassCohesion;
	}

	private Map<MethodDeclaration, Set<String>> getMethodToFields(CompilationUnit analyzeCompilationUnit) {
	    Map<MethodDeclaration, Set<String>> methodToFields = new HashMap<>();

	    for (MethodDeclaration method : analyzeCompilationUnit.findAll(MethodDeclaration.class)) {
	        Set<String> usedFields = new HashSet<>();
	        List<NameExpr> fields = method.findAll(NameExpr.class);

	        for (NameExpr field : fields) {
	            usedFields.add(field.getNameAsString());
	        }

	        methodToFields.put(method, usedFields);
	    }

	    return methodToFields;
	}
}