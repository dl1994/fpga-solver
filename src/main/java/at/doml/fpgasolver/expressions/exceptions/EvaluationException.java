package at.doml.fpgasolver.expressions.exceptions;

public class EvaluationException extends RuntimeException {
    
    private static final long serialVersionUID = 9142287366049781088L;
    
    public EvaluationException() {}
    
    public EvaluationException(String message) {
        super(message);
    }
}
