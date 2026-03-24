package godclassinspector.ui;

import java.io.File;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import godclassinspector.model.SourceFileDTO;

public class FoundFilesUI extends ViewPart {

	private TableViewer foundFilesViewer;

	public FoundFilesUI() {}

	@Override
	public void createPartControl(Composite parent) {
		this.foundFilesViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		Table resultsTable = this.foundFilesViewer.getTable();
		resultsTable.setHeaderVisible(true);
		resultsTable.setLinesVisible(true);

		createColumn("File Name", 100, 0);
		createColumn("Package", 100, 1);
		createColumn("Path", 200, 2);
		createColumn("WMC", 50, 3);
		createColumn("ATFD", 50, 4);
		createColumn("TCC", 50, 5);

		this.foundFilesViewer.addDoubleClickListener(event -> this.addDoubleClickListenerToFile(event));
		this.foundFilesViewer.setContentProvider(ArrayContentProvider.getInstance());
	}

	public void setInput(List<SourceFileDTO> projectDiscoveredFiles) {
		this.foundFilesViewer.setInput(projectDiscoveredFiles);
	}

	@Override
	public void setFocus() {
		this.foundFilesViewer.getControl().setFocus();
	}

	private void createColumn(String title, int width, int columnNumber) {
		TableViewerColumn resultsTableColumnViewer = new TableViewerColumn(this.foundFilesViewer, SWT.NONE);
		TableColumn tableColumn = resultsTableColumnViewer.getColumn();

		tableColumn.setText(title);
		tableColumn.setWidth(width);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);

		resultsTableColumnViewer.setLabelProvider(this.createColumnLabelProvider(columnNumber));
	}

	private ColumnLabelProvider createColumnLabelProvider(int columnNumber) {
	    return new ColumnLabelProvider() {
	        @Override
	        public String getText(Object element) {
	            SourceFileDTO file = (SourceFileDTO) element;

	            switch (columnNumber) {
	                case 0: return file.getFileName();
	                case 1: return file.getParentPackageName();
	                case 2: return file.getAbsolutePath();
	                case 3: return Integer.toString(file.getWeightedMethodCount());
	                case 4: return Integer.toString(file.getAccessToForeignData());
	                case 5: return String.format("%.3f", file.getTightClassCohesion());
	                default: return "";
	            }
	        }

	        @Override
	        public org.eclipse.swt.graphics.Color getBackground(Object element) {
	            SourceFileDTO file = (SourceFileDTO) element;

	            if (file.isGodClass()) {
	                return org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	            }
	            return null;
	        }

	        @Override
	        public org.eclipse.swt.graphics.Color getForeground(Object element) {
	            SourceFileDTO file = (SourceFileDTO) element;

	            if (file.isGodClass()) {
	                return org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	            }
	            return null;
	        }
	    };
	}

	private void addDoubleClickListenerToFile(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object firstElement = selection.getFirstElement();

		if (firstElement instanceof SourceFileDTO) {
			SourceFileDTO selectedFile = (SourceFileDTO) firstElement;
			openFileInEclipseEditor(selectedFile.getAbsolutePath());
		}
	}

	private void openFileInEclipseEditor(String absolutPath) {
		File fileToOpen = new File(absolutPath);

		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = getSite().getPage();

			try {
				IDE.openEditorOnFileStore(page, fileStore);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
