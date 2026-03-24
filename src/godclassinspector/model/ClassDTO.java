package godclassinspector.model;

import java.util.List;

public class ClassDTO {

	private String className;
	private String packageName;
	private boolean isInterface;
	private boolean isAbstract;
	private boolean isGodClass;
	private List<String> fields;
	private List<String> methods;

	public ClassDTO(String className, String packageName) {
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

	public void setGodClass(boolean isGodClass) {
		this.isGodClass = isGodClass;
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
				+ ", isAbstract=" + isAbstract + ", isGodClass=" + isGodClass + ", fields=" + fields + ", methods="
				+ methods + "]";
	}
}
