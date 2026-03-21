package godclassinspector.services;

import com.github.javaparser.ast.CompilationUnit;

import godclassinspector.model.SourceFileDTO;

public interface AnalysisService {
	void calculateMetrics(SourceFileDTO sourceFile);
	int calculateWeightedMethodCount(CompilationUnit analyzeCompilationUnit);
	int calculateAccessToForeignData(CompilationUnit analyzeCompilationUnit);
	float calculateTightClassCohesion(CompilationUnit analyzeCompilationUnit);
}
