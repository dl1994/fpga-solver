/* 
 * The MIT License (MIT)
 * 
 * Copyright © 2016 Domagoj Latečki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package hr.dlatecki.clb.parsing.lexical;

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
