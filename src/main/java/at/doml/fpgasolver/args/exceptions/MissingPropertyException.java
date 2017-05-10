package at.doml.fpgasolver.args.exceptions;

public class MissingPropertyException extends RuntimeException {
    
    private static final long serialVersionUID = -3597486670886080303L;
    
    private final String property;
    
    public MissingPropertyException(String property) {
        super("Missing property: " + property);
        this.property = property;
    }
    
    public String getProperty() {
        return property;
    }
}
