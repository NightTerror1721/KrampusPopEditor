/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.op;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public abstract class Operator
{
    final OperatorSymbol symbol;
    
    private Operator(OperatorSymbol symbol)
    {
        this.symbol = symbol;
    }
    
    public abstract int apply();
    
    public static final Operator getter(String value) { return new Getter(value); }
    
    public static final Operator unary(OperatorSymbol symbol, Operator op) { return new UnaryOperator(symbol, op); }
    
    public static final Operator binary(OperatorSymbol symbol, Operator op0, Operator op1) { return new BinaryOperator(symbol, op0, op1); }
    
    
    private static class BinaryOperator extends Operator
    {
        private final Operator op0;
        private final Operator op1;
        
        private BinaryOperator(OperatorSymbol symbol, Operator op0, Operator op1)
        {
            super(Objects.requireNonNull(symbol));
            this.op0 = Objects.requireNonNull(op0);
            this.op1 = Objects.requireNonNull(op1);
        }
        
        @Override
        public int apply() { return symbol.apply(op0, op1); }
    }
    
    private static class UnaryOperator extends Operator
    {
        private final Operator op;
        
        private UnaryOperator(OperatorSymbol symbol, Operator op)
        {
            super(Objects.requireNonNull(symbol));
            this.op = Objects.requireNonNull(op);
        }
        
        @Override
        public int apply() { return symbol.apply(op); }
    }
    
    private static class Getter extends Operator
    {
        private final String op;
        
        private Getter(String op)
        {
            super(null);
            this.op = Objects.requireNonNull(op);
        }
        
        @Override
        public int apply() { return OperatorSymbol.decodeOperand(op); }
    }
}
