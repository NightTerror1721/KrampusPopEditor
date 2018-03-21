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
public final class OperatorSymbol
{
    /* Binaries */
    public static final OperatorSymbol
            OR = new OperatorSymbol("||", 0, (op0, op1) -> (op0 != 0 ? op0 : op1 != 0 ? op1 : 0)),
            
            AND = new OperatorSymbol("&&", 1, (op0, op1) -> (op0 != 0 && op1 != 0) ? op1 : 0),
            
            EQUALS = new OperatorSymbol("==", 2, (op0, op1) -> (op0 == op1) ? op1 : 0),
            NOT_EQUALS = new OperatorSymbol("!=", 2, (op0, op1) -> (op0 != op1) ? op1 : 0),
            GRATER_THAN = new OperatorSymbol(">", 2, (op0, op1) -> (op0 > op1) ? op1 : 0),
            LESS_THAN = new OperatorSymbol("<", 2, (op0, op1) -> (op0 < op1) ? op1 : 0),
            GREATER_EQUALS_THAN = new OperatorSymbol(">=", 2, (op0, op1) -> (op0 >= op1) ? op1 : 0),
            LESS_EQUALS_THAN = new OperatorSymbol("<=", 2, (op0, op1) -> (op0 <= op1) ? op1 : 0),
            BIT_COMPARE = new OperatorSymbol("<=>", 5, (op0, op1) -> (op0 < op1 ? -1 : op0 == op1 ? 0 : 1)),
            
            BIT_OR = new OperatorSymbol("|", 3, (op0, op1) -> (op0 | op1)),
            BIT_AND = new OperatorSymbol("&", 3, (op0, op1) -> (op0 & op1)),
            BIT_XOR = new OperatorSymbol("^", 3, (op0, op1) -> (op0 ^ op1)),
            
            BIT_LSH = new OperatorSymbol("<<", 4, (op0, op1) -> (op0 << op1)),
            BIT_RSH = new OperatorSymbol(">>", 4, (op0, op1) -> (op0 >>> op1)),
            
            PLUS = new OperatorSymbol("+", 5, (op0, op1) -> (op0 + op1)),
            MINUS = new OperatorSymbol("-", 5, (op0, op1) -> (op0 - op1)),
            
            MULTIPLY = new OperatorSymbol("*", 6, (op0, op1) -> (op0 * op1)),
            DIVIDE = new OperatorSymbol("/", 6, (op0, op1) -> (op0 / op1)),
            MODULE = new OperatorSymbol("%", 6, (op0, op1) -> (op0 % op1));
    
    /* Unaries */
    public static final OperatorSymbol
            NEGATE = new OperatorSymbol("!", 7, (op0, op1) -> (op0 != 0 ? op0 : 0)),
            BIT_NEGATE = new OperatorSymbol("~", 7, (op0, op1) -> (~op0));
    
    
    
    
    private final String symbol;
    private final int priority;
    private final OperatorAction action;
    
    private OperatorSymbol(String symbol, int priority, OperatorAction action)
    {
        this.symbol = Objects.requireNonNull(symbol);
        this.priority = priority;
        this.action = Objects.requireNonNull(action);
    }
    
    public final String getSymbol() { return symbol; }
    public final int getPriority() { return priority; }
    
    public final int comparePriority(OperatorSymbol os) { return Integer.compare(priority, os.priority); }
    
    public final int apply(String op0, String op1)
    {
        return action.apply(decodeOperand(op0), decodeOperand(op1));
    }
    
    public final int apply(String op0)
    {
        return action.apply(decodeOperand(op0), 0);
    }
    
    private int decodeOperand(String op)
    {
        try { return Integer.decode(op); }
        catch(NumberFormatException ex) { return 0; }
    }
    
    @FunctionalInterface
    private interface OperatorAction
    {
        int apply(int op0, int op1);
    }
}
