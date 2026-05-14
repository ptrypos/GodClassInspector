package godclassinspector.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import godclassinspector.model.NodeType;
import godclassinspector.model.SuggestionNode;

public class SuggestionLabelProvider extends LabelProvider {
    
    private Color methodColor;
    private Color complexityColor;
    private Color blockColor;
    private Color detailColor;
    private Display display;

    public SuggestionLabelProvider(Display display) {
        super();
        this.display = display;
        initializeColors();
    }

    private void initializeColors() {
        if (display != null && !display.isDisposed()) {
            methodColor = new Color(display, 0, 100, 150);
            complexityColor = new Color(display, 200, 100, 0);
            blockColor = new Color(display, 100, 100, 100);
            detailColor = new Color(display, 70, 70, 70);
        }
    }

    @Override
    public String getText(Object element) {
        if (element instanceof SuggestionNode) {
            SuggestionNode node = (SuggestionNode) element;
            return node.getDisplayText();
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof SuggestionNode) {
            SuggestionNode node = (SuggestionNode) element;
            return getIconForNodeType(node.getType());
        }
        return super.getImage(element);
    }

    private Image getIconForNodeType(NodeType type) {
        
        switch (type) {
            case ROOT:
                return null;
            case CLASS:
                return null;
            case REFACTORING_TYPE:
                return null;
            case DETAIL:
                return null;
            default:
                return null;
        }
    }

    public Color getForeground(Object element) {
        if (element instanceof SuggestionNode) {
            SuggestionNode node = (SuggestionNode) element;
            NodeType type = node.getType();
            
            switch (type) {
                case CLASS:
                    return methodColor;
                case REFACTORING_TYPE:
                    return complexityColor;
                case DETAIL:
                    String text = node.getDisplayText();
                    if (text.startsWith("Complexity Level:")) {
                        return complexityColor;
                    } else if (text.startsWith("•")) {
                        return blockColor;
                    } else {
                        return detailColor;
                    }
                case ROOT:
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        disposeColor(methodColor);
        disposeColor(complexityColor);
        disposeColor(blockColor);
        disposeColor(detailColor);
        super.dispose();
    }

    private void disposeColor(Color color) {
        if (color != null && !color.isDisposed()) {
            color.dispose();
        }
    }
}