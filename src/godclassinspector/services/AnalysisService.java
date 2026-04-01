package godclassinspector.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import godclassinspector.model.SourceFileDTO;

public interface AnalysisService {
	void checkGodClass(List<SourceFileDTO> files) throws Exception;
	Map<MethodDeclaration, Set<String>> getMethodToFields(CompilationUnit analyzeCompilationUnit);
}
