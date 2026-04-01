package godclassinspector.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.25f, editorArea);

		bottom.addView("godclassinspector.ui.foundFiles");
		bottom.addView("godclassinspector.ui.umlDiagram");
	}
}