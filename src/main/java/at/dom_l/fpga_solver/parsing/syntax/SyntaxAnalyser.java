package at.dom_l.fpga_solver.parsing.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import at.dom_l.fpga_solver.expressions.AndExpression;
import at.dom_l.fpga_solver.expressions.FunctionExpression;
import at.dom_l.fpga_solver.expressions.NandExpression;
import at.dom_l.fpga_solver.expressions.NorExpression;
import at.dom_l.fpga_solver.expressions.NotExpression;
import at.dom_l.fpga_solver.expressions.OrExpression;
import at.dom_l.fpga_solver.expressions.abstracts.AbstractCompoundExpression;
import at.dom_l.fpga_solver.expressions.interfaces.IBooleanExpression;
import at.dom_l.fpga_solver.parsing.exceptions.SyntaxException;
import at.dom_l.fpga_solver.expressions.VariableExpression;
import at.dom_l.fpga_solver.expressions.XnorExpression;
import at.dom_l.fpga_solver.expressions.XorExpression;
import at.dom_l.fpga_solver.parsing.lexical.LexicalToken;
import at.dom_l.fpga_solver.parsing.lexical.LexicalUnit;

public class SyntaxAnalyser {
    
    private int currentIndex;
    private LexicalUnit currentUnit;
    private LexicalToken currentType;
    private final List<LexicalUnit> lexicalUnits;
    
    public SyntaxAnalyser(List<LexicalUnit> lexicalUnits) {
        this.lexicalUnits = lexicalUnits;
    }
    
    private void previousLexicalUnit() {
        try {
            currentUnit = lexicalUnits.get(--currentIndex);
            currentType = currentUnit.getType();
        } catch (IndexOutOfBoundsException e) {
            throw new SyntaxException("Syntax error has occured.");
        }
    }
    
    private void nextLexicalUnit() {
        try {
            currentUnit = lexicalUnits.get(currentIndex++);
            currentType = currentUnit.getType();
        } catch (IndexOutOfBoundsException e) {
            throw new SyntaxException("Syntax error has occured.");
        }
    }
    
    public List<FunctionExpression> parseTokens() {
        List<FunctionExpression> functions = new ArrayList<>();
        
        while (currentIndex < lexicalUnits.size()) {
            nextLexicalUnit();
            
            if (currentType == LexicalToken.IDENTIFIER) {
                functions.add(function(currentUnit.getValue()));
            } else if (currentType == LexicalToken.SEMICOLON) {
                continue;
            } else {
                throw new SyntaxException(currentUnit, LexicalToken.SEMICOLON, LexicalToken.IDENTIFIER);
            }
        }
        
        return functions;
    }
    
    private FunctionExpression function(String name) {
        nextLexicalUnit();
        
        if (currentType == LexicalToken.ASSOCIATION) {
            IBooleanExpression expression = orXorFunction();
            
            nextLexicalUnit();
            
            if (currentType == LexicalToken.SEMICOLON) {
                return new FunctionExpression(name, expression);
            } else {
                throw new SyntaxException(currentUnit, LexicalToken.SEMICOLON);
            }
        } else {
            throw new SyntaxException(currentUnit, LexicalToken.ASSOCIATION);
        }
    }
    
    private static IBooleanExpression getCorrectSide(Supplier<IBooleanExpression> leftSideSupplier,
            Function<IBooleanExpression, IBooleanExpression> rightSideSupplier) {
        IBooleanExpression leftSide = leftSideSupplier.get();
        IBooleanExpression rightSide = rightSideSupplier.apply(leftSide);
        
        return rightSide == null ? leftSide : rightSide;
    }
    
    private static IBooleanExpression chainExpressions(IBooleanExpression leftSide,
            Class<? extends AbstractCompoundExpression> requiredType,
            Supplier<AbstractCompoundExpression> instanceGetter,
            Supplier<IBooleanExpression> rightSideSupplier,
            Function<IBooleanExpression, IBooleanExpression> leftSideSupplier) {
        IBooleanExpression rightSide = rightSideSupplier.get();
        IBooleanExpression currentExpression;
        
        if (leftSide.getClass() == requiredType) {
            currentExpression = ((AbstractCompoundExpression) leftSide).addExpression(rightSide);
        } else {
            currentExpression = instanceGetter.get().addExpression(leftSide).addExpression(rightSide);
        }
        
        IBooleanExpression nextExpression = leftSideSupplier.apply(currentExpression);
        
        return nextExpression == null ? currentExpression : nextExpression;
    }
    
    private IBooleanExpression epsilonCheck(Supplier<Optional<IBooleanExpression>> action) {
        try {
            nextLexicalUnit();
        } catch (SyntaxException e) {
            return null;
        }
        
        Optional<IBooleanExpression> result = action.get();
        
        if (result.isPresent()) {
            return result.get();
        } else {
            previousLexicalUnit();
            
            return null;
        }
    }
    
    private IBooleanExpression orXorFunction() {
        return getCorrectSide(this::norXonrFunction, this::orXor);
    }
    
    private IBooleanExpression orXor(IBooleanExpression leftSide) {
        return epsilonCheck(() -> {
            if (currentType == LexicalToken.KEYWORD_OR) {
                return Optional.ofNullable(chainExpressions(leftSide, OrExpression.class, OrExpression::new,
                        this::norXonrFunction, this::orXor));
            } else if (currentType == LexicalToken.KEYWORD_XOR) {
                return Optional.ofNullable(chainExpressions(leftSide, XorExpression.class, XorExpression::new,
                        this::norXonrFunction, this::orXor));
            } else {
                return Optional.empty();
            }
        });
    }
    
    private IBooleanExpression norXonrFunction() {
        return getCorrectSide(this::andFunction, this::norXnor);
    }
    
    private IBooleanExpression norXnor(IBooleanExpression leftSide) {
        return epsilonCheck(() -> {
            if (currentType == LexicalToken.KEYWORD_NOR) {
                return Optional.ofNullable(chainExpressions(leftSide, NorExpression.class, NorExpression::new,
                        this::andFunction, this::norXnor));
            } else if (currentType == LexicalToken.KEYWORD_XNOR) {
                return Optional.ofNullable(chainExpressions(leftSide, XnorExpression.class, XnorExpression::new,
                        this::andFunction, this::norXnor));
            } else {
                return Optional.empty();
            }
        });
    }
    
    private IBooleanExpression andFunction() {
        return getCorrectSide(this::nandFunction, this::and);
    }
    
    private IBooleanExpression and(IBooleanExpression leftSide) {
        return epsilonCheck(() -> {
            if (currentType == LexicalToken.KEYWORD_AND) {
                return Optional.ofNullable(chainExpressions(leftSide, AndExpression.class, AndExpression::new,
                        this::nandFunction, this::and));
            } else {
                return Optional.empty();
            }
        });
    }
    
    private IBooleanExpression nandFunction() {
        return getCorrectSide(this::not, this::nand);
    }
    
    private IBooleanExpression nand(IBooleanExpression leftSide) {
        return epsilonCheck(() -> {
            if (currentType == LexicalToken.KEYWORD_NAND) {
                return Optional.ofNullable(chainExpressions(leftSide, NandExpression.class, NandExpression::new,
                        this::not, this::nand));
            } else {
                return Optional.empty();
            }
        });
    }
    
    private IBooleanExpression not() {
        nextLexicalUnit();
        
        if (currentType == LexicalToken.KEYWORD_NOT) {
            return new NotExpression(not());
        } else if (currentType == LexicalToken.IDENTIFIER) {
            return new VariableExpression(currentUnit.getValue());
        } else if (currentType == LexicalToken.OPEN_PARENTHESIS) {
            IBooleanExpression groupedExpression = orXorFunction();
            
            nextLexicalUnit();
            
            if (currentType == LexicalToken.CLOSED_PARENTHESIS) {
                return groupedExpression;
            } else {
                throw new SyntaxException(currentUnit, LexicalToken.CLOSED_PARENTHESIS);
            }
        } else {
            throw new SyntaxException(currentUnit, LexicalToken.KEYWORD_NOT, LexicalToken.IDENTIFIER,
                    LexicalToken.OPEN_PARENTHESIS);
        }
    }
}
