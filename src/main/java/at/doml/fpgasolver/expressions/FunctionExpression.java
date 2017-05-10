package at.doml.fpgasolver.expressions;

import at.doml.fpgasolver.expressions.abstracts.AbstractNamedExpression;
import at.doml.fpgasolver.expressions.interfaces.IBooleanExpression;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class FunctionExpression extends AbstractNamedExpression {
    
    private IBooleanExpression expression;
    
    public FunctionExpression(String name, IBooleanExpression expression) {
        super(name);
        this.expression = expression;
    }
    
    @Override
    public boolean evaluate(SortedMap<String, Boolean> values) {
        return expression.evaluate(values);
    }
    
    @Override
    public String toString() {
        return name + " <= " + expression.toString() + ";";
    }
    
    @Override
    public SortedSet<String> getVariables() {
        SortedSet<String> output = new TreeSet<>(super.getVariables());
        
        output.addAll(getExpressionVariables());
        
        return output;
    }
    
    public SortedSet<String> getExpressionVariables() {
        return expression.getVariables();
    }
    
    @Override
    public IBooleanExpression replaceVariable(String identifier, IBooleanExpression expression) {
        this.expression = this.expression.replaceVariable(identifier, expression);
        
        return this;
    }
}
