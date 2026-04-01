package godclassinspector.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import godclassinspector.model.ClassDTO;
import godclassinspector.model.SourceFileDTO;

public class UmlDiagramServiceImp implements UmlDiagramService {

	private final String PUBLIC_SYMBOL = "+";
	private final String PRIVATE_SYMBOL = "-";
	private final String PROTECTED_SYMBOL = "#";

	@Override
	public List<ClassDTO> extractClassesFeatures(List<SourceFileDTO> projectFiles) throws FileNotFoundException {
		List<ClassDTO> projectClassesList = new ArrayList<>();

		for (SourceFileDTO fileData : projectFiles) {
			ClassDTO projectClass = this.createClassDto(fileData);

			File projectFile = new File(fileData.getAbsolutePath());

			CompilationUnit compilationUnit = StaticJavaParser.parse(projectFile);

			compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(decl -> {
				projectClass.setInterface(decl.isInterface());
				projectClass.setAbstract(decl.isAbstract());

				decl.getExtendedTypes().forEach(st -> projectClass.setSuperClassName(st.getNameAsString()));
				decl.getFields().forEach(f -> {
					projectClass.getDependencies().add(f.getElementType().asString());
				});
			});

			List<String> extractedClassFields = this.extractClassFields(compilationUnit);
			List<String> extractedClassMethods = this.extractClassMethod(compilationUnit);

			projectClass.setGodClass(fileData.isGodClass());
			projectClass.setFields(extractedClassFields);
			projectClass.setMethods(extractedClassMethods);

			projectClassesList.add(projectClass);
		}

		return projectClassesList;
	}

	private ClassDTO createClassDto(SourceFileDTO file) {
		String className = file.getFileName();
		String packageName = file.getParentPackageName();

		ClassDTO projectClass = new ClassDTO(className, packageName);

		return projectClass;
	}

	private List<String> extractClassFields(CompilationUnit compilationUnit) {
		List<String> extractedClassFields = new ArrayList<>();
		List<FieldDeclaration> classFields = compilationUnit.findAll(FieldDeclaration.class);

		for (FieldDeclaration field : classFields) {
			String visibility = getFieldVisibility(field);
			String staticMod = field.isStatic() ? "{static} " : "";

			for (VariableDeclarator variable : field.getVariables()) {
				String name = variable.getNameAsString();
				String type = variable.getTypeAsString();

				String umlFieldAsString = visibility + " " + staticMod + name + " : " + type;

				extractedClassFields.add(umlFieldAsString);
			}
		}

		return extractedClassFields;
	}

	private List<String> extractClassMethod(CompilationUnit compilationUnit) {
		List<String> extractedClassMethods = new ArrayList<>();
		List<MethodDeclaration> classMethods = compilationUnit.findAll(MethodDeclaration.class);

		for (MethodDeclaration method : classMethods) {
			String visibility = getMethodVisibility(method);
			String staticMod = method.isStatic() ? "{static} " : "";
			String name = method.getNameAsString();
			String type = method.getTypeAsString();

			String umlMethodAsString = visibility + " " + staticMod + name + " : " + type;

			extractedClassMethods.add(umlMethodAsString);
		}

		return extractedClassMethods;
	}

	private String getFieldVisibility (FieldDeclaration field) {
		if (field.isPublic()) {
			return PUBLIC_SYMBOL;
		} else if (field.isPrivate()) {
			return PRIVATE_SYMBOL;
		} else if (field.isProtected()) {
			return PROTECTED_SYMBOL;
		} else {
			return "";
		}
	}

	private String getMethodVisibility (MethodDeclaration method) {
		if (method.isPublic()) {
			return PUBLIC_SYMBOL;
		} else if (method.isPrivate()) {
			return PRIVATE_SYMBOL;
		} else if (method.isProtected()) {
			return PROTECTED_SYMBOL;
		} else {
			return "";
		}
	}
}