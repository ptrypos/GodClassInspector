package godclassinspector.ui;

import godclassinspector.models.ExtractMethodInfo;

public class ExtractMethodParser {

    public ExtractMethodInfo parse(String suggestion) {
        try {
            ExtractMethodInfo info = new ExtractMethodInfo();
            extractMethodName(suggestion, info);
            extractComplexityLevel(suggestion, info);
            extractComplexBlocks(suggestion, info);
            
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    private void extractMethodName(String suggestion, ExtractMethodInfo info) {
        int startIdx = suggestion.indexOf("[");
        int endIdx = suggestion.indexOf("]");
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            String methodName = suggestion.substring(startIdx + 1, endIdx).trim();
            info.setMethodName(methodName);
        }
    }

    private void extractComplexityLevel(String suggestion, ExtractMethodInfo info) {
        int complexityStart = suggestion.indexOf("(");
        int complexityEnd = suggestion.indexOf(")");
        if (complexityStart != -1 && complexityEnd != -1 && complexityEnd > complexityStart) {
            String complexityStr = suggestion.substring(complexityStart + 1, complexityEnd).trim();
            try {
                int complexity = Integer.parseInt(complexityStr);
                info.setComplexityLevel(complexity);
            } catch (NumberFormatException e) {
                info.setComplexityLevel(0);
            }
        }
    }

    private void extractComplexBlocks(String suggestion, ExtractMethodInfo info) {
        int extractStart = suggestion.indexOf("Consider extracting:");
        if (extractStart != -1) {
            String blocksText = suggestion.substring(extractStart + "Consider extracting:".length());
            blocksText = blocksText.replace(".", "").trim();

            String[] blocks = blocksText.split(";");
            for (String block : blocks) {
                String cleanBlock = block.trim();
                if (!cleanBlock.isEmpty()) {
                    info.addComplexBlock(cleanBlock);
                }
            }
        }
    }
}