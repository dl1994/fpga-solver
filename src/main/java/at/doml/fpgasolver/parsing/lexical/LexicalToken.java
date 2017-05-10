package at.doml.fpgasolver.parsing.lexical;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LexicalToken {
    
    KEYWORD_AND("and", "and"),
    KEYWORD_OR("or", "or"),
    KEYWORD_NOT("not", "not"),
    KEYWORD_NAND("nand", "nand"),
    KEYWORD_NOR("nor", "nor"),
    KEYWORD_XOR("xor", "xor"),
    KEYWORD_XNOR("xnor", "xnor"),
    ASSOCIATION("<=", "<="),
    OPEN_PARENTHESIS("\\(", "("),
    CLOSED_PARENTHESIS("\\)", ")"),
    SEMICOLON(";", ";"),
    IDENTIFIER("[A-Za-z\\$_][A-Za-z0-9\\$_]*", "identifier"),
    WHITESPACE("\\s+", " "),
    ERROR("[^\\s]+", "error");
    
    private static final Pattern ALL_PATTERNS;
    
    static {
        StringBuilder builder = new StringBuilder();
        
        for (LexicalToken tokenType : values()) {
            builder.append("(").append(tokenType.pattern.pattern()).append(")|");
        }
        
        ALL_PATTERNS = Pattern.compile(builder.substring(0, builder.length() - 1));
    }
    
    private final Pattern pattern;
    private final String value;
    
    private LexicalToken(String pattern, String value) {
        this.pattern = Pattern.compile(pattern);
        this.value = value;
    }
    
    public boolean matches(String string) {
        return pattern.matcher(string).matches();
    }
    
    public static boolean matchesAny(String input) {
        LexicalToken tokenType = firstMatching(input);
        
        if (tokenType == ERROR || tokenType == WHITESPACE) {
            return false;
        } else {
            return true;
        }
    }
    
    public static LexicalToken firstMatching(String input) {
        for (LexicalToken tokenType : values()) {
            if (tokenType.matches(input)) {
                return tokenType;
            }
        }
        
        return ERROR;
    }
    
    public static Matcher matcher(String input) {
        return ALL_PATTERNS.matcher(input);
    }
    
    public String getValue() {
        return value;
    }
}
