package godclassinspector.model;

import java.io.File;

public class SourceFileDTO {
	private String fileName;
    private String absolutePath;
    private String parentPackageName;
    private long fileSize;
    private int weightedMethodCount;
    private int accessToForeignData;
    private float tightClassCohesion;

    public SourceFileDTO(File file, String parentPackageName) {
        this.fileName = file.getName();
        this.absolutePath = file.getAbsolutePath();
		this.parentPackageName = parentPackageName;
        this.fileSize = file.length();
    }
    
    public String getFileName() {
    	return this.fileName;
    }
    
    public String getAbsolutePath() {
    	return this.absolutePath;
    }
    
    public String getParentPackageName() {
    	return this.parentPackageName;
    }
    
    public long getFileSize() {
    	return this.fileSize;
    }
    
    public int getWeightedMethodCount() {
    	return this.weightedMethodCount;
    }
    
    public int getAccessToForeignData() {
    	return this.accessToForeignData;
    }
    
    public float getTightClassCohesion() {
    	return this.tightClassCohesion;
    }
    
    public void setWeightedMethodCount(int weightedMethodCound) {
    	this.weightedMethodCount = weightedMethodCound;
    }
    
    public void setAccessToForeignData(int accessToForeignData) {
    	this.accessToForeignData = accessToForeignData;
    }
    
    public void setTightClassCohesion(float tightClassCohesion) {
    	this.tightClassCohesion = tightClassCohesion;
    }
    
    public String toString() {
    	return ("Filename: " + this.fileName + " | AbsolutPath: " + this.absolutePath + " | ParentPackageName: " + this.parentPackageName + " | FileSize: " + this.fileSize + 
    			" | Weighted Method Count: " + this.weightedMethodCount + " | Access to Foreign Data: " + this.accessToForeignData + " | Tight Class Cohesion: " + this.tightClassCohesion);
    }
}