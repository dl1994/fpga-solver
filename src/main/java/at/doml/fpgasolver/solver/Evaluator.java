package at.doml.fpgasolver.solver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import at.doml.fpgasolver.expressions.FunctionExpression;
import at.doml.fpgasolver.expressions.interfaces.IBooleanExpression;
import at.doml.fpgasolver.logic.TruthTable;

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
