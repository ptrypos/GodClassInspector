package godclassinspector.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import godclassinspector.models.ClassDTO;
import godclassinspector.models.UmlClassDTO;

public class UmlDiagramServiceImp implements UmlDiagramService {

	private final String PUBLIC_SYMBOL = "+";
	private final String PRIVATE_SYMBOL = "-";
	private final String PROTECTED_SYMBOL = "#";

	@Override
	public List<UmlClassDTO> extractClassesFeatures(List<ClassDTO> projectFiles) throws FileNotFoundException {
		List<UmlClassDTO> projectClassesList = new ArrayList<>();

		for (ClassDTO fileData : projectFiles) {
			UmlClassDTO projectClass = this.createClassDto(fileData);

			File projectFile = new File(fileData.getAbsolutePath());

			CompilationUnit compilationUnit = StaticJavaParser.parse(projectFile);

			compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(decl -> {
				projectClass.setInterface(decl.isInterface());
				projectClass.setAbstract(decl.isAbstract());

				decl.getExtendedTypes().forEach(st -> projectClass.setSuperClassName(st.getNameAsString()));

				decl.getImplementedTypes()
						.forEach(it -> projectClass.getImplementedInterfaces().add(it.getNameAsString()));

				projectClass.getAssociations().addAll(extractFieldAssociations(decl.getFields()));
			});

			projectClass.getDependencies().addAll(extractMethodDependencies(compilationUnit));

			List<String> extractedClassFields = this.extractClassFields(compilationUnit);
			List<String> extractedClassMethods = this.extractClassMethod(compilationUnit);

			projectClass.setGodClass(fileData.isGodClass());
			projectClass.setUnrefactableClass(fileData.isUnrefactableClass());
			projectClass.setFields(extractedClassFields);
			projectClass.setMethods(extractedClassMethods);

			projectClassesList.add(projectClass);
		}

		return projectClassesList;
	}

	private UmlClassDTO createClassDto(ClassDTO file) {
		String className = file.getClassName();
		String packageName = file.getParentPackageName();

		UmlClassDTO projectClass = new UmlClassDTO(className, packageName);

		return projectClass;
	}

	private List<String> extractFieldAssociations(List<FieldDeclaration> fields) {
		Set<String> associations = new LinkedHashSet<>();
		for (FieldDeclaration field : fields) {
			collectTypeNames(field.getElementType(), associations);
		}
		return new ArrayList<>(associations);
	}

	private List<String> extractMethodDependencies(CompilationUnit compilationUnit) {
		Set<String> dependencies = new LinkedHashSet<>();

		for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
			collectTypeNames(method.getType(), dependencies);
			method.getParameters().forEach(p -> collectTypeNames(p.getType(), dependencies));
		}

		for (ConstructorDeclaration constructor : compilationUnit.findAll(ConstructorDeclaration.class)) {
			constructor.getParameters().forEach(p -> collectTypeNames(p.getType(), dependencies));
		}

		for (ObjectCreationExpr creation : compilationUnit.findAll(ObjectCreationExpr.class)) {
			collectTypeNames(creation.getType(), dependencies);
		}

		return new ArrayList<>(dependencies);
	}

	private void collectTypeNames(Type type, Set<String> names) {
		if (type == null) {
			return;
		}

		if (type.isClassOrInterfaceType()) {
			ClassOrInterfaceType cit = type.asClassOrInterfaceType();
			names.add(cit.getNameAsString());
			cit.getTypeArguments().ifPresent(args -> args.forEach(arg -> collectTypeNames(arg, names)));
		} else if (type.isArrayType()) {
			collectTypeNames(type.asArrayType().getComponentType(), names);
		} else if (type.isWildcardType()) {
			type.asWildcardType().getExtendedType().ifPresent(t -> collectTypeNames(t, names));
			type.asWildcardType().getSuperType().ifPresent(t -> collectTypeNames(t, names));
		}
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

	private String getFieldVisibility(FieldDeclaration field) {
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

	private String getMethodVisibility(MethodDeclaration method) {
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