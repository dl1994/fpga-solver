package at.doml.fpgasolver.args.abstracts;

import at.doml.fpgasolver.args.exceptions.ParameterException;
import at.doml.fpgasolver.args.ArgumentParser;
import java.util.function.BiConsumer;

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
