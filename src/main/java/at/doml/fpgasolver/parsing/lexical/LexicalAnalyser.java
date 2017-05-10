package at.doml.fpgasolver.parsing.lexical;

import at.doml.fpgasolver.parsing.exceptions.LexicalException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

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
