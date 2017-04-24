package at.dom_l.fpga_solver.args.exceptions;

public class UnknownArgumentException extends RuntimeException {
    
    private static final long serialVersionUID = 8473016731867842766L;
    
    private final String argument;
    
    public UnknownArgumentException(String argument) {
        super("Unknown argument: " + (argument.length() == 1 ? "-" : "--") + argument);
        this.argument = argument;
    }
    
    public String getArgument() {
        return argument;
    }
}
