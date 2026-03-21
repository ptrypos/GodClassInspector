package godclassinspector.ui;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import godclassinspector.model.SourceFileDTO;

public class FilesFoundUI extends ViewPart {

	private TableViewer viewer;
	
	public FilesFoundUI() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		Table resultsTable = viewer.getTable();
		resultsTable.setHeaderVisible(true);
		resultsTable.setLinesVisible(true);
		
		createColumn(viewer, "File Name", 200, 0);
		createColumn(viewer, "Package", 250, 1);
		createColumn(viewer, "Path", 400, 2);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());;
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
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
	
	public void setInput(List<SourceFileDTO> files) {
		viewer.setInput(files);
	}
}
