package hr.dlatecki.clb.parsing.exceptions;

import hr.dlatecki.clb.parsing.lexical.LexicalToken;
import hr.dlatecki.clb.parsing.lexical.LexicalUnit;

public class SyntaxException extends RuntimeException {
    
    private static final long serialVersionUID = -8219803943519321129L;
    
    private final LexicalUnit actualUnit;
    private final LexicalToken[] expectedTypes;
    
    public SyntaxException(LexicalUnit actualUnit, LexicalToken... expectedTypes) {
        this.actualUnit = actualUnit;
        this.expectedTypes = expectedTypes;
    }
    
    public SyntaxException(String message) {
        super(message);
        
        actualUnit = null;
        expectedTypes = null;
    }
    
    public LexicalUnit getActualUnit() {
        return actualUnit;
    }
    
    public LexicalToken[] getExpectedTypes() {
        return expectedTypes;
    }
}
