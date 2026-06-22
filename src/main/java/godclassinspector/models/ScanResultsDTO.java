package godclassinspector.models;

import java.util.List;

import godclassinspector.ui.FoundFilesUI;

public class ScanResultsDTO {
	private static List<ClassDTO> foundFiles;
	private static FoundFilesUI foundFilesUiObject;

	public static void setFiles(List<ClassDTO> files) {
		foundFiles = files;
	}

	public static List<ClassDTO> getFiles() {
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
