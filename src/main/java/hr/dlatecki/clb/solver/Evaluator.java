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
package hr.dlatecki.clb.solver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import hr.dlatecki.clb.expressions.FunctionExpression;
import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;
import hr.dlatecki.clb.logic.TruthTable;

public class Evaluator {
    
    private final SortedSet<String> variables;
    private final List<FunctionExpression> targetFunctions;
    private final Map<String, FunctionExpression> functionsMap;
    
    public Evaluator(FunctionExpression... targetFunctions) {
        this.targetFunctions = Arrays.asList(targetFunctions);
        
        variables = IBooleanExpression.joinVariableSets(this.targetFunctions.stream());
        functionsMap = this.targetFunctions.stream()
                .collect(Collectors.toMap(
                        FunctionExpression::getName,
                        function -> function));
        variables.removeAll(functionsMap.keySet());
        functionsMap.forEach((identifier, expression) -> {
            this.targetFunctions.forEach(targetFunction -> {
                targetFunction.replaceVariable(identifier, expression);
            });
        });
    }
    
    public void evaluate(CLBChromosome chromosome) {
        int error = 0;
        
        for (Map.Entry<String, FunctionExpression> entry : functionsMap.entrySet()) {
            TruthTable fpgaTable = new TruthTable(chromosome.getFunctionOutput(entry.getKey()), variables);
            TruthTable targetTable = new TruthTable(entry.getValue(), variables);
            
            chromosome.tagClbs(entry.getKey());
            error += fpgaTable.countDifferentRows(targetTable);
        }
        
        chromosome.setError(error);
        chromosome.setNumOfTaggedClbs();
    }
    
    public SortedSet<String> getVariables() {
        return variables;
    }
    
    public Set<String> getFunctionNames() {
        return functionsMap.keySet();
    }
}
