package godclassinspector.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import godclassinspector.model.ClassDTO;

public interface DetectionService {
	void checkGodClass(List<ClassDTO> files) throws Exception;
	Map<MethodDeclaration, Set<String>> getMethodToFields(CompilationUnit analyzeCompilationUnit);
}
