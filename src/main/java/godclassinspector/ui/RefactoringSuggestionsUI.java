package godclassinspector.ui;

import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import godclassinspector.models.ExtractMethodInfo;
import godclassinspector.models.NodeType;
import godclassinspector.models.SuggestionContentProvider;
import godclassinspector.models.SuggestionNode;

public class RefactoringSuggestionsUI extends ViewPart {

    public static final String ID = "godclassinspector.ui.refactoringSuggestions";

    private TreeViewer viewer;
    private Text searchText;
    private Display display;
    private ExtractMethodParser extractMethodParser;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        display = parent.getDisplay();
        extractMethodParser = new ExtractMethodParser();

        createSearchBox(parent);

        viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(new SuggestionContentProvider(searchText));
        viewer.setLabelProvider(new SuggestionLabelProvider(display));
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

                if ("Extract Method".equals(refactoringType)) {
                    processExtractMethodSuggestion(classNode, refactoringType, rawDescription);
                } else {

                    processGenericSuggestion(classNode, refactoringType, rawDescription);
                }
            }

            rootNode.addChild(classNode);
        }

        viewer.setInput(rootNode);
        viewer.expandAll();
    }

    private void processExtractMethodSuggestion(SuggestionNode classNode, String refactoringType, String rawDescription) {
        String[] individualSuggestions = rawDescription.split("\\s*\\|\\s*");

        for (String description : individualSuggestions) {
            if (description.trim().isEmpty()) {
                continue;
            }

            ExtractMethodInfo methodInfo = extractMethodParser.parse(description.trim());

            if (methodInfo != null) {

                SuggestionNode typeNode = new SuggestionNode(
                    refactoringType + " - " + methodInfo.getMethodName(),
                    methodInfo.getFormattedDescription(),
                    NodeType.REFACTORING_TYPE
                );

                SuggestionNode complexityNode = new SuggestionNode(
                    "Complexity Level: " + methodInfo.getComplexityLevel(),
                    null,
                    NodeType.DETAIL
                );
                typeNode.addChild(complexityNode);

                for (String block : methodInfo.getComplexBlocks()) {
                    SuggestionNode blockNode = new SuggestionNode(
                        "• " + block,
                        null,
                        NodeType.DETAIL
                    );
                    typeNode.addChild(blockNode);
                }

                SuggestionNode summaryNode = new SuggestionNode(
                    "Action: Break this method into smaller, focused methods",
                    null,
                    NodeType.DETAIL
                );
                typeNode.addChild(summaryNode);

                classNode.addChild(typeNode);
            }
        }
    }

    private void processGenericSuggestion(SuggestionNode classNode, String refactoringType, String rawDescription) {
        String[] individualSuggestions = rawDescription.split("\\s*\\|\\s*");

        for (String description : individualSuggestions) {
            if (description.trim().isEmpty()) {
				continue;
			}

            SuggestionNode typeNode = new SuggestionNode(
                refactoringType,
                null,
                NodeType.REFACTORING_TYPE
            );

            SuggestionNode detailNode = new SuggestionNode(
                description.trim(),
                null,
                NodeType.DETAIL
            );

            typeNode.addChild(detailNode);
            classNode.addChild(typeNode);
        }
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}