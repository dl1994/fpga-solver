package hr.dlatecki.clb.expressions;

import hr.dlatecki.clb.expressions.abstracts.AbstractCompoundExpression;

public class NandExpression extends AbstractCompoundExpression {
    
    @Override
    protected boolean binaryAction(boolean left, boolean right) {
        return !(left & right);
    }
    
    @Override
    protected boolean canChange(boolean currentValue) {
        return currentValue == true;
    }
    
    @Override
    protected String operator() {
        return "nand";
    }
}
