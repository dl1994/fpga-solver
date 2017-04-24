package at.dom_l.fpga_solver.expressions.interfaces;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public interface IBooleanExpression {
    
    public boolean evaluate(SortedMap<String, Boolean> values);
    
    public SortedSet<String> getVariables();
    
    public IBooleanExpression replaceVariable(String identifier, IBooleanExpression expression);
    
    public String getName();
    
    public static SortedSet<String> joinVariableSets(Stream<? extends IBooleanExpression> stream) {
        return stream
                .map(IBooleanExpression::getVariables)
                .reduce(new TreeSet<String>(),
                        (variables, newSet) -> {
                            variables.addAll(newSet);
                            
                            return variables;
                        });
    }
}
