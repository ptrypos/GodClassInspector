package godclassinspector.model;

import java.io.File;

public class SourceFileDTO {

	private String fileName;
	private String absolutePath;
	private String parentPackageName;
	private long fileSize;
	private int weightedMethodCount;
	private int accessToForeignData;
	private double tightClassCohesion;
	private boolean isGodClass;

	public SourceFileDTO(File file, String parentPackageName) {
		this.fileName = file.getName();
		this.absolutePath = file.getAbsolutePath();
		this.parentPackageName = parentPackageName;
		this.fileSize = file.length();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public boolean isGodClass() {
		return isGodClass;
	}

	public void setGodClass(boolean isGodClass) {
		this.isGodClass = isGodClass;
	}

	@Override
	public String toString() {
		return "SourceFileDTO [fileName=" + fileName + ", absolutePath=" + absolutePath + ", parentPackageName="
				+ parentPackageName + ", fileSize=" + fileSize + ", weightedMethodCount=" + weightedMethodCount
				+ ", accessToForeignData=" + accessToForeignData + ", tightClassCohesion=" + tightClassCohesion
				+ ", isGodClass=" + isGodClass + "]";
	}
}