package godclassinspector.model;

import java.util.List;

public class ScanResultsDTO {
	private static List<SourceFileDTO> foundFiles;
	
	public static void setFiles(List<SourceFileDTO> files) {
		foundFiles = files;
	}
	
	public static List<SourceFileDTO> getFiles() {
		return foundFiles;
	}
	
	public static void clearFiles() {
		foundFiles.clear();
	}
}
