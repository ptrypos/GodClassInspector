package godclassinspector.ui;

import java.util.Map;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import godclassinspector.model.NodeType;
import godclassinspector.model.SuggestionContentProvider;
import godclassinspector.model.SuggestionNode;

public class RefactoringSuggestionsUI extends ViewPart {
    
    public static final String ID = "godclassinspector.ui.refactoringSuggestions";
    
    private TreeViewer viewer;
    private Text searchText;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        
        createSearchBox(parent);
        
        viewer = new TreeViewer(parent);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(new SuggestionContentProvider(searchText));
        viewer.setLabelProvider(new SuggestionLabelProvider());
    }

    private void createSearchBox(Composite parent) {
        searchText = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        searchText.setMessage("Search suggestions...");
        searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        searchText.addModifyListener(event -> {
            viewer.refresh();
        });
    }

    public void setInput(Map<String, Map<String, String>> input) {
        if (input == null || input.isEmpty()) {
            viewer.setInput(null);
            return;
        }
        
        SuggestionNode rootNode = new SuggestionNode("Refactoring Suggestions", null, NodeType.ROOT);
        
        for (Map.Entry<String, Map<String, String>> classEntry : input.entrySet()) {
            String className = classEntry.getKey();
            Map<String, String> suggestionsMap = classEntry.getValue();
            
            SuggestionNode classNode = new SuggestionNode(className, null, NodeType.CLASS);
            
            for (Map.Entry<String, String> suggestionEntry : suggestionsMap.entrySet()) {
                String refactoringType = suggestionEntry.getKey();
                String rawDescription = suggestionEntry.getValue();
                
                String[] individualSuggestions = rawDescription.split("\\s*\\|\\s*");
                
                for (String description : individualSuggestions) {
                    if (!description.trim().isEmpty()) {
                        SuggestionNode typeNode = new SuggestionNode(refactoringType, description.trim(), NodeType.REFACTORING_TYPE);
                        classNode.addChild(typeNode);
                    }
                }
            }
            
            rootNode.addChild(classNode);
        }
        
        viewer.setInput(rootNode);
        viewer.expandAll();
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    class SuggestionLabelProvider extends LabelProvider {
        
        @Override
        public String getText(Object element) {
            if (element instanceof SuggestionNode) {
                SuggestionNode node = (SuggestionNode) element;
                return node.getDisplayText();
            }
            return super.getText(element);
        }
    }
}