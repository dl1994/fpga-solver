package at.doml.fpgasolver.expressions;

import at.doml.fpgasolver.expressions.exceptions.EvaluationException;
import at.doml.fpgasolver.expressions.interfaces.IBooleanExpression;
import at.doml.fpgasolver.expressions.abstracts.AbstractNamedExpression;
import java.util.SortedMap;

public class VariableExpression extends AbstractNamedExpression {
    
    public VariableExpression(String name) {
        super(name);
    }
    
    @Override
    public boolean evaluate(SortedMap<String, Boolean> values) {
        Boolean value = values.get(name);
        
        if (value == null) {
            throw new EvaluationException("Unable to evaluate a variable with name \"" + name
                    + "\" - no such variable exists in the map of provided values.");
        }
        
        return value;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public IBooleanExpression replaceVariable(String identifier, IBooleanExpression expression) {
        if (name.equals(identifier)) {
            return expression;
        } else {
            return this;
        }
    }
}
