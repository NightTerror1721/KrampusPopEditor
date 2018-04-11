/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public final class Operator implements Operand
{
    private final OperatorSymbol symbol;
    private final Operand[] operands;
    
    private Operator(OperatorSymbol symbol, Operand... operands)
    {
        if(symbol.getOperandCount() != operands.length)
            throw new IllegalStateException();
        this.symbol = symbol;
        this.operands = operands;
    }
    
    public static final Operator unary(OperatorSymbol symbol, Operand op0) { return new Operator(symbol, Objects.requireNonNull(op0)); }
    public static final Operator binary(OperatorSymbol symbol, Operand op0, Operand op1) { return new Operator(symbol, Objects.requireNonNull(op0), Objects.requireNonNull(op1)); }
    
    public final OperatorSymbol getSymbol() { return symbol; }
    
    public final int getOperandCount() { return operands.length; }
    public final Operand getOperand(int index) { return operands[index]; }
    
    @Override
    public final TokenType getTokenType() { return TokenType.OPERATOR; }
    
    @Override
    public final void resolve(CodePool code, FieldPool fields, Environment env) throws CompilationError
    {
        symbol.resolve(code, fields, env, operands);
    }

    @Override
    public boolean isCompatibleWithConditionals() { return symbol.isConditionalSymbol(); }
}
