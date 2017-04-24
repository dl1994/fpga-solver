package hr.dlatecki.clb.parsing.lexical;

public class LexicalUnit {
    
    private final int lineNumber;
    private final int startIndex;
    private final int endIndex;
    private final String value;
    private final String line;
    private final LexicalToken type;
    
    public LexicalUnit(int lineNumber, int startIndex, int endIndex, String value, String line, LexicalToken type) {
        checkValue(lineNumber, "Line number");
        checkValue(startIndex, "Start index");
        checkValue(endIndex, "End index");
        
        if (startIndex > endIndex) {
            throw new IllegalArgumentException("Start index cannot be bigger than end index.");
        }
        
        this.lineNumber = lineNumber;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.value = value;
        this.line = line;
        this.type = type;
    }
    
    public LexicalUnit(String value, LexicalToken type) {
        this(1, 1, value.length() + 1, value, value, type);
    }
    
    private static void checkValue(int value, String valueName) {
        if (value <= 0) {
            throw new IllegalArgumentException(valueName + " cannot be zero or negative.");
        }
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getStartIndex() {
        return startIndex;
    }
    
    public int getEndIndex() {
        return endIndex;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getLine() {
        return line;
    }
    
    public LexicalToken getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return type.toString() + " @" + lineNumber + " [" + startIndex + "-" + endIndex + "]: " + value;
    }
    
    public String highlighted() {
        StringBuilder builder = new StringBuilder();
        
        builder.append(line);
        builder.append("\n");
        
        for (int i = 1; i < startIndex; i++) {
            builder.append(" ");
        }
        
        builder.append("^");
        
        for (int i = startIndex + 1; i < endIndex; i++) {
            builder.append(" ");
        }
        
        if (startIndex != endIndex) {
            builder.append("^");
        }
        
        return builder.toString();
    }
}
