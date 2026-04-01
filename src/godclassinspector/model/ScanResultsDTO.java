package godclassinspector.model;

import java.util.List;

import godclassinspector.ui.FoundFilesUI;

public class ScanResultsDTO {
	private static List<SourceFileDTO> foundFiles;
	private static FoundFilesUI foundFilesUiObject;

	public static void setFiles(List<SourceFileDTO> files) {
		foundFiles = files;
	}

	public static List<SourceFileDTO> getFiles() {
		return foundFiles;
	}

	public static void clearFiles() {
		foundFiles.clear();
	}

	public static FoundFilesUI getView() {
		return foundFilesUiObject;
	}

	public static void setView(FoundFilesUI foundFilesUi) {
		foundFilesUiObject = foundFilesUi;
	}
}
