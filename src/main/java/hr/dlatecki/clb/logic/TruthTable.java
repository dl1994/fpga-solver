package hr.dlatecki.clb.logic;

import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;
import hr.dlatecki.clb.logic.exceptions.TableRowComparisonException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public class TruthTable {
    
    private final SortedSet<String> variables;
    private final List<List<Boolean>> rows;
    
    public TruthTable(IBooleanExpression expression, SortedSet<String> variables) {
        this.variables = variables;
        
        rows = new ArrayList<>();
        evaluateTable(expression);
    }
    
    private void evaluateTable(IBooleanExpression expression) {
        int conbinations = (int) Math.pow(2.0, variables.size());
        
        SortedMap<String, Boolean> values = new TreeMap<>();
        
        variables.forEach(variable -> {
            values.put(variable, Boolean.FALSE);
        });
        
        evaluateRow(expression, values);
        
        for (int i = 1; i < conbinations; i++) {
            setValues(i, values);
            evaluateRow(expression, values);
        }
    }
    
    private void evaluateRow(IBooleanExpression expression, SortedMap<String, Boolean> values) {
        List<Boolean> row = new ArrayList<>(values.values());
        
        row.add(expression.evaluate(values));
        rows.add(row);
    }
    
    private static void setValues(int index, SortedMap<String, Boolean> values) {
        int mask = 1 << (values.size() - 1);
        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            if ((index & mask) == 0) {
                entry.setValue(Boolean.FALSE);
            } else {
                entry.setValue(Boolean.TRUE);
            }
            
            mask >>>= 1;
        }
    }
    
    @Override
    public String toString() {
        return toString("1", "0");
    }
    
    public String toString(String trueSymbol, String falseSymbol) {
        final String lastRowName = "RESULT";
        StringBuilder builder = new StringBuilder();
        
        int longerSymbolLength = Math.max(trueSymbol.length(), falseSymbol.length());
        for (String variable : variables) {
            builder.append(variable).append(" ");
            
            int missingLength = longerSymbolLength - variable.length();
            
            while (missingLength > 0) {
                builder.append(" ");
                missingLength--;
            }
            
            builder.append("| ");
        }
        
        builder.append(lastRowName).append("\n");
        
        int length = builder.length();
        
        while (length > 0) {
            builder.append("-");
            length--;
        }
        
        builder.append("\n");
        
        for (List<Boolean> row : rows) {
            int index = 0;
            for (String variable : variables) {
                boolean value = row.get(index);
                
                if (value) {
                    appendSymbol(trueSymbol, Math.max(variable.length(), longerSymbolLength), builder);
                } else {
                    appendSymbol(falseSymbol, Math.max(variable.length(), longerSymbolLength), builder);
                }
                
                builder.append("| ");
                
                index++;
            }
            
            if (row.get(index)) {
                appendSymbol(trueSymbol, Math.max(lastRowName.length(), longerSymbolLength), builder);
            } else {
                appendSymbol(falseSymbol, Math.max(lastRowName.length(), longerSymbolLength), builder);
            }
            
            builder.append("\n");
        }
        
        return builder.toString();
    }
    
    private static void appendSymbol(String symbol, int expectedLength, StringBuilder builder) {
        builder.append(symbol).append(" ");
        
        int missingLength = expectedLength - symbol.length();
        while (missingLength > 0) {
            builder.append(" ");
            missingLength--;
        }
    }
    
    public int countDifferentRows(TruthTable other) {
        if (this.rows.size() != other.rows.size()) {
            throw new TableRowComparisonException("Unable to count different rows in truth tables of different size.");
        }
        
        int difference = 0;
        
        for (int i = 0, size = rows.size(); i < size; i++) {
            List<Boolean> thisRow = this.rows.get(i);
            List<Boolean> otherRow = other.rows.get(i);
            
            boolean thisValue = thisRow.get(thisRow.size() - 1);
            boolean otherValue = otherRow.get(otherRow.size() - 1);
            
            if (thisValue != otherValue) {
                difference++;
            }
        }
        
        return difference;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        TruthTable other = (TruthTable) obj;
        
        if (rows == null) {
            if (other.rows != null) {
                return false;
            }
        } else if (!rows.equals(other.rows)) {
            return false;
        }
        
        if (variables == null) {
            if (other.variables != null) {
                return false;
            }
        } else if (!variables.equals(other.variables)) {
            return false;
        }
        
        return true;
    }
}
