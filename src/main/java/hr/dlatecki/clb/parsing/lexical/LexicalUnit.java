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
