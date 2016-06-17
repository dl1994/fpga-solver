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
package hr.dlatecki.clb.expressions.abstracts;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import hr.dlatecki.clb.expressions.exceptions.EvaluationException;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;

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
