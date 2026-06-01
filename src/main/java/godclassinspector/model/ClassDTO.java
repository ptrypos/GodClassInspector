package godclassinspector.model;

import java.io.File;
import java.util.Map;

public class ClassDTO {

	private String className;
	private String absolutePath;
	private String parentPackageName;
	private long fileSize;
	private int weightedMethodCount;
	private int accessToForeignData;
	private double tightClassCohesion;
	private Map<String, Double> localityOfAttributeAccess;
	private Map<String, Integer> foreignDataProviders;
	private boolean isGodClass;
	private boolean isUnrefactableClass;

	public ClassDTO(File file, String parentPackageName) {
		this.className = file.getName();
		this.className = this.className.replace(".java", "");
		this.absolutePath = file.getAbsolutePath();
		this.parentPackageName = parentPackageName;
		this.fileSize = file.length();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String fileName) {
		this.className = fileName;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getParentPackageName() {
		return parentPackageName;
	}

	public void setParentPackageName(String parentPackageName) {
		this.parentPackageName = parentPackageName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getWeightedMethodCount() {
		return weightedMethodCount;
	}

	public void setWeightedMethodCount(int weightedMethodCount) {
		this.weightedMethodCount = weightedMethodCount;
	}

	public int getAccessToForeignData() {
		return accessToForeignData;
	}

	public void setAccessToForeignData(int accessToForeignData) {
		this.accessToForeignData = accessToForeignData;
	}

	public double getTightClassCohesion() {
		return tightClassCohesion;
	}

	public void setTightClassCohesion(double tightClassCohesion) {
		this.tightClassCohesion = tightClassCohesion;
	}
	
	public Map<String, Double> getLocalityOfAttributeAccess() {
		return localityOfAttributeAccess;
	}

	public void setLocalityOfAttributeAccess(Map<String, Double> methodLocalityOfAttributeAccess) {
		this.localityOfAttributeAccess = methodLocalityOfAttributeAccess;
	}
	
	public Map<String, Integer> getForeignDataProviders() {
		return foreignDataProviders;
		
	}
	
	public void setForeignDataProviders(Map<String, Integer> foreignDataProviders) {
		this.foreignDataProviders = foreignDataProviders;		
	}

	public boolean isGodClass() {
		return isGodClass;
	}

	public void setGodClass(boolean isGodClass) {
		this.isGodClass = isGodClass;
	}
	
	public boolean isUnrefactableClass() {
		return isUnrefactableClass;
	}
	
	public void setUnrefactableClass(boolean isUnrefactableClass) {
		this.isUnrefactableClass = isUnrefactableClass;
	}

	@Override
	public String toString() {
		return "SourceFileDTO [fileName=" + className + ", absolutePath=" + absolutePath + ", parentPackageName="
				+ parentPackageName + ", fileSize=" + fileSize + ", weightedMethodCount=" + weightedMethodCount
				+ ", accessToForeignData=" + accessToForeignData + ", tightClassCohesion=" + tightClassCohesion
				+ ", isGodClass=" + isGodClass + "]";
	}
}