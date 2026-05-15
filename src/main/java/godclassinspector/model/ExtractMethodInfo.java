package godclassinspector.model;

import java.util.ArrayList;
import java.util.List;

public class ExtractMethodInfo {
    
    private String methodName;
    private int complexityLevel;
    private List<String> complexBlocks;

    public ExtractMethodInfo() {
        this.complexBlocks = new ArrayList<>();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(int complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public List<String> getComplexBlocks() {
        return complexBlocks;
    }

    public void addComplexBlock(String block) {
        this.complexBlocks.add(block);
    }

    public String getFormattedDescription() {
        return "Method with complexity level " + complexityLevel + 
               " contains " + complexBlocks.size() + " complex code block(s)";
    }

    @Override
    public String toString() {
        return "ExtractMethodInfo{" +
                "methodName='" + methodName + '\'' +
                ", complexityLevel=" + complexityLevel +
                ", complexBlocks=" + complexBlocks +
                '}';
    }
}