package at.dom_l.fpga_solver.solver;

import java.util.Arrays;

public class CLBInputs {
    
    private final int[] inputs;
    
    public CLBInputs(int size) {
        inputs = new int[size];
        
        for (int i = 0; i < size; i++) {
            inputs[i] = -1;
        }
    }
    
    public CLBInputs(CLBInputs original) {
        this.inputs = Arrays.copyOf(original.inputs, original.inputs.length);
    }
    
    public boolean contains(int index) {
        for (int input : inputs) {
            if (index == input) {
                return true;
            }
        }
        
        return false;
    }
    
    public int[] getInputs() {
        return inputs;
    }
    
    public void setInput(int index, int value) {
        inputs[index] = value;
    }
    
    public int getInput(int index) {
        return inputs[index];
    }
    
    @Override
    public String toString() {
        return Arrays.toString(inputs);
    }
}
