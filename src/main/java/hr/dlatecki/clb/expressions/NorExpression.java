package hr.dlatecki.clb.expressions;

import hr.dlatecki.clb.expressions.abstracts.AbstractCompoundExpression;

public class NorExpression extends AbstractCompoundExpression {
    
    @Override
    protected boolean binaryAction(boolean left, boolean right) {
        return !(left | right);
    }
    
    @Override
    protected boolean canChange(boolean currentValue) {
        return true;
    }
    
    @Override
    protected String operator() {
        return "nor";
    }
}
