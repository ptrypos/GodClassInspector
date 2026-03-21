package godclassinspector.model;

import java.io.File;

public class SourceFileDTO {
    private File file;
	private String fileName;
    private String absolutePath;
    private String parentPackageName;
    private long fileSize;

    public SourceFileDTO(File file, String parentPackageName) {
    	this.file = file;
        this.fileName = file.getName();
        this.absolutePath = file.getAbsolutePath();
		this.parentPackageName = parentPackageName;
        this.fileSize = file.length();
    }

    public File getFile() {
    	return this.file;
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
    
    public String toString() {
    	return ("Filename: " + this.fileName + " | AbsolutPath: " + this.absolutePath + " | ParentPackageName: " + this.parentPackageName + " | FileSize: " + this.fileSize);
    }
}