package hr.dlatecki.clb.expressions.abstracts;

import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;
import hr.dlatecki.clb.parsing.lexical.LexicalToken;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AbstractNamedExpression implements IBooleanExpression {
    
    protected final String name;
    
    public AbstractNamedExpression(String name) {
        if (!LexicalToken.IDENTIFIER.matches(name)) {
            throw new IllegalArgumentException("Provided name is not a valid identifier. "
                    + "See hr.dlatecki.clb.parsing.LexicalToken.IDENTIFIER for regular expression "
                    + "which defines a valid identifier.");
        }
        
        this.name = name;
    }
    
    @Override
    public SortedSet<String> getVariables() {
        SortedSet<String> value = new TreeSet<>();
        
        value.add(name);
        
        return value;
    }
    
    @Override
    public String getName() {
        return name;
    }
}
