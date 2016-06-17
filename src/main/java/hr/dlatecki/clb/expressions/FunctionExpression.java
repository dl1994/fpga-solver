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
import java.util.TreeSet;
import hr.dlatecki.clb.expressions.abstracts.AbstractNamedExpression;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;

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
