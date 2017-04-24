package hr.dlatecki.clb.parsing.exceptions;

import hr.dlatecki.clb.parsing.lexical.LexicalUnit;
import java.util.List;

public class LexicalException extends RuntimeException {
    
    private static final long serialVersionUID = 8638160199591178954L;
    
    private final List<LexicalUnit> lexicalUnits;
    
    public LexicalException(List<LexicalUnit> lexicalUnits) {
        this.lexicalUnits = lexicalUnits;
    }
    
    public List<LexicalUnit> getLexicalUnits() {
        return lexicalUnits;
    }
}
