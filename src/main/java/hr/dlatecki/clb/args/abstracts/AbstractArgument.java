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
package hr.dlatecki.clb.args.abstracts;

import java.util.function.BiConsumer;
import hr.dlatecki.clb.args.ArgumentParser;
import hr.dlatecki.clb.args.exceptions.ParameterException;

public abstract class AbstractArgument {
    
    protected final int numOfParams;
    protected final BiConsumer<String[], ArgumentParser.PropertySetter> parameterParser;
    
    public AbstractArgument(int numOfParams, BiConsumer<String[], ArgumentParser.PropertySetter> parameterParser) {
        this.numOfParams = numOfParams;
        this.parameterParser = parameterParser;
    }
    
    public void parseParameters(String[] parameters, ArgumentParser.PropertySetter propertySetter) {
        if (parameters.length != numOfParams) {
            throw new ParameterException("Unexpected number of parameters. Expected: "
                    + numOfParams + ", actual: " + parameters.length);
        }
        
        parameterParser.accept(parameters, propertySetter);
    }
    
    public int getNumOfParams() {
        return numOfParams;
    }
    
    public abstract String getArgKey();
}
