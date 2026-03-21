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

public class FilesFoundUI extends ViewPart {

	private TableViewer foundFilesViewer;
	
	public FilesFoundUI() {}

	@Override
	public void createPartControl(Composite parent) {
		this.foundFilesViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		Table resultsTable = this.foundFilesViewer.getTable();
		resultsTable.setHeaderVisible(true);
		resultsTable.setLinesVisible(true);
		
		createColumn(foundFilesViewer, "File Name", 200, 0);
		createColumn(foundFilesViewer, "Package", 250, 1);
		createColumn(foundFilesViewer, "Path", 400, 2);
		
		this.foundFilesViewer.addDoubleClickListener(event -> this.addDoubleClickListenerToFile(event));
		this.foundFilesViewer.setContentProvider(ArrayContentProvider.getInstance());;
	}
	
	public void setInput(List<SourceFileDTO> files) {
		this.foundFilesViewer.setInput(files);
	}
	
	@Override
	public void setFocus() {
		this.foundFilesViewer.getControl().setFocus();
	}
	
	private void createColumn(TableViewer viewer, String title, int width, int columnNumber) {
		TableViewerColumn resultsTableColumnViewer = new TableViewerColumn(viewer, SWT.NONE);
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
					default: return "";
				}
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
