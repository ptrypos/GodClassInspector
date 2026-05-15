package godclassinspector.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Text;

public class SuggestionContentProvider implements ITreeContentProvider {
    
    private final Text searchText;

    public SuggestionContentProvider(Text searchText) {
        this.searchText = searchText;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof SuggestionNode) {
            SuggestionNode node = (SuggestionNode) inputElement;
            return node.getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof SuggestionNode) {
            SuggestionNode node = (SuggestionNode) parentElement;
            List<SuggestionNode> filteredChildren = new ArrayList<>();
            
            String searchTerm = searchText.getText().toLowerCase();
            
            for (SuggestionNode child : node.getChildren()) {
                if (searchTerm.isEmpty() || child.getDisplayText().toLowerCase().contains(searchTerm)) {
                    filteredChildren.add(child);
                }
            }
            
            return filteredChildren.toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof SuggestionNode) {
            return ((SuggestionNode) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof SuggestionNode) {
            return ((SuggestionNode) element).hasChildren();
        }
        return false;
    }
}