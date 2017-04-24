package hr.dlatecki.clb.logic.exceptions;

public class TableRowComparisonException extends RuntimeException {
    
    private static final long serialVersionUID = 4758946167967243614L;
    
    public TableRowComparisonException(String message) {
        super(message);
    }
}
