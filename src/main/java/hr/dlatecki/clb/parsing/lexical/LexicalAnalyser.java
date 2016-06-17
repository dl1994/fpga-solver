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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import hr.dlatecki.clb.parsing.exceptions.LexicalException;

public class LexicalAnalyser {
    
    private boolean error;
    private boolean lexicalUnitsGenerated;
    private final List<String> lines;
    private final List<LexicalUnit> lexicalUnits;
    
    public LexicalAnalyser(String... lines) {
        this(Arrays.asList(lines), false);
    }
    
    public LexicalAnalyser(List<String> lines) {
        this(lines, true);
    }
    
    private LexicalAnalyser(List<String> lines, boolean copyList) {
        if (copyList) {
            this.lines = new ArrayList<>(lines);
        } else {
            this.lines = lines;
        }
        
        lexicalUnits = new ArrayList<>();
    }
    
    public List<LexicalUnit> getLexicalUnits() {
        if (generateLexicalUnits()) {
            throw new LexicalException(lexicalUnits);
        }
        
        return Collections.unmodifiableList(lexicalUnits);
    }
    
    private boolean generateLexicalUnits() {
        if (!lexicalUnitsGenerated) {
            int lineNumber = 1;
            
            for (String line : lines) {
                groupLineIntoLexicalUnits(line, lineNumber++);
            }
            
            lexicalUnitsGenerated = true;
        }
        
        return error;
    }
    
    private void groupLineIntoLexicalUnits(String line, int lineNumber) {
        if (line.isEmpty()) {
            return;
        }
        
        Matcher matcher = LexicalToken.matcher(line);
        
        while (matcher.find()) {
            String value = matcher.group();
            LexicalToken type = LexicalToken.firstMatching(value);
            
            if (type == LexicalToken.WHITESPACE) {
                continue;
            }
            
            int startIndex = matcher.start() + 1;
            int endIndex = matcher.end();
            
            LexicalUnit token = new LexicalUnit(lineNumber, startIndex, endIndex, value, line, type);
            
            if (type != LexicalToken.ERROR && !error) {
                lexicalUnits.add(token);
            } else {
                if (!error) {
                    error = true;
                    lexicalUnits.clear();
                }
                
                if (type == LexicalToken.ERROR) {
                    lexicalUnits.add(token);
                }
            }
        }
    }
}
