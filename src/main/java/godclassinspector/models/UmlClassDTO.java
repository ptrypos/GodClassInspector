package godclassinspector.models;

import java.util.ArrayList;
import java.util.List;

public class UmlClassDTO {

	private String className;
	private String packageName;
	private boolean isInterface;
	private boolean isAbstract;
	private boolean isGodClass;
	private boolean isUnrefactableClass;
	private String superClassName;
	private List<String> implementedInterfaces = new ArrayList<>();
	private List<String> associations = new ArrayList<>();
	private List<String> dependencies = new ArrayList<>();
	private List<String> fields;
	private List<String> methods;

	public UmlClassDTO(String className, String packageName) {
		this.className = className;
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean isGodClass() {
		return isGodClass;
	}

	public boolean isUnrefactableClass() {
		return isUnrefactableClass;
	}

	public void setUnrefactableClass(boolean isUnrefactableClass) {
		this.isUnrefactableClass = isUnrefactableClass;
	}

	public void setGodClass(boolean isGodClass) {
		this.isGodClass = isGodClass;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public List<String> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void setImplementedInterfaces(List<String> implementedInterfaces) {
		this.implementedInterfaces = implementedInterfaces;
	}

	public List<String> getAssociations() {
		return associations;
	}

	public void setAssociations(List<String> associations) {
		this.associations = associations;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	@Override
	public String toString() {
		return "ClassDTO [className=" + className + ", packageName=" + packageName + ", isInterface=" + isInterface
				+ ", isAbstract=" + isAbstract + ", isGodClass=" + isGodClass + ", superClassName=" + superClassName
				+ ", implementedInterfaces=" + implementedInterfaces + ", associations=" + associations
				+ ", dependencies=" + dependencies + ", fields=" + fields + ", methods=" + methods + "]";
	}
}