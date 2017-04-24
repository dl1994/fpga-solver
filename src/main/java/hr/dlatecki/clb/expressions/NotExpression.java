package hr.dlatecki.clb.expressions;

import hr.dlatecki.clb.expressions.abstracts.AbstractCompoundExpression;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;
import java.util.SortedMap;
import java.util.SortedSet;

public class NotExpression implements IBooleanExpression {
    
    private IBooleanExpression expression;
    
    public NotExpression(IBooleanExpression expression) {
        this.expression = expression;
    }
    
    @Override
    public boolean evaluate(SortedMap<String, Boolean> values) {
        return !expression.evaluate(values);
    }
    
    @Override
    public SortedSet<String> getVariables() {
        return expression.getVariables();
    }
    
    @Override
    public String toString() {
        if (expression instanceof AbstractCompoundExpression) {
            return "not (" + expression.toString() + ")";
        } else {
            return "not " + expression.toString();
        }
    }
    
    @Override
    public IBooleanExpression replaceVariable(String identifier, IBooleanExpression expression) {
        this.expression = this.expression.replaceVariable(identifier, expression);
        
        return this;
    }
    
    @Override
    public String getName() {
        return "not";
    }
}
