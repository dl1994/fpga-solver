package at.doml.fpgasolver.expressions.abstracts;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import at.doml.fpgasolver.expressions.exceptions.EvaluationException;
import at.doml.fpgasolver.expressions.interfaces.IBooleanExpression;

public abstract class AbstractCompoundExpression implements IBooleanExpression {
    
    private final List<IBooleanExpression> expressions;
    
    public AbstractCompoundExpression() {
        expressions = new ArrayList<>();
    }
    
    public AbstractCompoundExpression addExpression(IBooleanExpression expression) {
        expressions.add(expression);
        
        return this;
    }
    
    @Override
    public boolean evaluate(SortedMap<String, Boolean> values) {
        if (expressions.size() == 0) {
            throw new EvaluationException("Unable to evaluate an empty expression.");
        }
        
        boolean result = expressions.get(0).evaluate(values);
        
        for (int i = 1, size = expressions.size(); i < size; i++) {
            if (!canChange(result)) {
                break;
            }
            
            result = binaryAction(result, expressions.get(i).evaluate(values));
        }
        
        return result;
    }
    
    @Override
    public SortedSet<String> getVariables() {
        return IBooleanExpression.joinVariableSets(expressions.stream());
    }
    
    @Override
    public IBooleanExpression replaceVariable(String identifier, IBooleanExpression expression) {
        List<IBooleanExpression> newExpressions = new ArrayList<>();
        
        for (IBooleanExpression e : expressions) {
            newExpressions.add(e.replaceVariable(identifier, expression));
        }
        
        expressions.clear();
        expressions.addAll(newExpressions);
        
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String operator = operator();
        
        expressions.forEach(expression -> {
            if (expression instanceof AbstractCompoundExpression) {
                builder.append("(").append(expression).append(")");
            } else {
                builder.append(expression);
            }
            
            builder.append(" ").append(operator).append(" ");
        });
        
        return builder.substring(0, builder.length() - operator.length() - 2);
    }
    
    @Override
    public String getName() {
        return operator();
    }
    
    protected abstract boolean binaryAction(boolean left, boolean right);
    
    protected abstract boolean canChange(boolean currentValue);
    
    protected abstract String operator();
}
