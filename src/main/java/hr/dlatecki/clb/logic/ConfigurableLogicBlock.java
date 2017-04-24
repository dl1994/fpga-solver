package hr.dlatecki.clb.logic;

import hr.dlatecki.clb.expressions.interfaces.IBooleanExpression;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;

public class ConfigurableLogicBlock implements IBooleanExpression {
    
    private String name;
    private final boolean[] outputs;
    private final IBooleanExpression[] inputs;
    
    public ConfigurableLogicBlock(int numOfInputs) {
        if (numOfInputs <= 0) {
            throw new IllegalArgumentException("Number of inputs must be a natrual number.");
        }
        
        inputs = new IBooleanExpression[numOfInputs];
        outputs = new boolean[(int) Math.pow(2.0, numOfInputs)];
    }
    
    public ConfigurableLogicBlock(ConfigurableLogicBlock original) {
        this.outputs = Arrays.copyOf(original.outputs, original.outputs.length);
        this.inputs = Arrays.copyOf(original.inputs, original.inputs.length);
    }
    
    public void setInput(int index, IBooleanExpression value) {
        inputs[index] = value;
    }
    
    public void setOutput(int index, boolean value) {
        outputs[index] = value;
    }
    
    public void flipOutput(int index) {
        outputs[index] ^= true;
    }
    
    public int getTableSize() {
        return outputs.length;
    }
    
    public int getNumOfInputs() {
        return inputs.length;
    }
    
    private static void checkArraySize(int expectedSize, int actualSize) {
        if (expectedSize != actualSize) {
            throw new IllegalArgumentException("Provided array is of invalid size. Expected size was: " + expectedSize
                    + ", but provided size was: " + actualSize);
        }
    }
    
    public void setOutputs(boolean... outputs) {
        checkArraySize(this.outputs.length, outputs.length);
        
        System.arraycopy(outputs, 0, this.outputs, 0, this.outputs.length);
    }
    
    @Override
    public boolean evaluate(SortedMap<String, Boolean> values) {
        int mask = 1 << (inputs.length - 1);
        int index = 0;
        for (IBooleanExpression input : inputs) {
            if (input.evaluate(values)) {
                index |= mask;
            }
            
            mask >>>= 1;
        }
        
        return outputs[index];
    }
    
    @Override
    public SortedSet<String> getVariables() {
        return IBooleanExpression.joinVariableSets(Stream.of(inputs));
    }
    
    @Override
    public IBooleanExpression replaceVariable(String identifier, IBooleanExpression expression) {
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < inputs.length; i++) {
            builder.append(i).append(" <= ").append(inputs[inputs.length - i - 1].getName()).append("\n");
        }
        
        builder.append("\n");
        
        for (int i = 0; i < outputs.length; i++) {
            builder.append(i).append(": ").append(outputs[i]).append("\n");
        }
        
        return builder.toString();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
