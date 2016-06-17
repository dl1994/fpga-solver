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
