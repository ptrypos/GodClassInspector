package godclassinspector.model;

import java.util.ArrayList;
import java.util.List;

public class SuggestionNode {
    private String name;
    private String description;
    private NodeType type;
    private SuggestionNode parent;
    private List<SuggestionNode> children;

    public SuggestionNode(String name, String description, NodeType type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.children = new ArrayList<>();
    }

    public void addChild(SuggestionNode child) {
        child.parent = this;
        this.children.add(child);
    }

    public String getDisplayText() {
        switch (this.type) {
            case ROOT:
                return "Refactoring Suggestions (" + children.size() + " classes)";

            case CLASS:
                return name + " (" + children.size() + " suggestions)";

            case REFACTORING_TYPE:
                if (description != null && !description.isEmpty()) {
                    String truncated = description;
                    if (truncated.length() > 120) {
                        truncated = truncated.substring(0, 117) + "...";
                    }
                    return name + ": " + truncated;
                } else {
                    return name;
                }

            case METHOD:
                return name;

            default:
                return name;
        }
    }

    public NodeType getType() {
        return type;
    }

    public SuggestionNode getParent() {
        return parent;
    }

    public List<SuggestionNode> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
}