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
package hr.dlatecki.clb.expressions;

import java.util.SortedMap;
import java.util.SortedSet;
import hr.dlatecki.clb.expressions.abstracts.AbstractCompoundExpression;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;

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
