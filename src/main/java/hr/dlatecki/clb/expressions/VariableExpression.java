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
import hr.dlatecki.clb.expressions.abstracts.AbstractNamedExpression;
import hr.dlatecki.clb.expressions.exceptions.EvaluationException;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;

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
