/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;
import kp.populous.api.script.compiler.parser.Operand.Environment;

/**
 *
 * @author Asus
 */
public final class OperatorSymbol implements UnparsedOperand
{
    private final String symbol;
    private final int priority;
    private final int operandCount;
    private final boolean conditionMode;
    private final OperatorAction action;
    
    private OperatorSymbol(String symbol, int priority, int operandCount, boolean conditionMode, OperatorAction action)
    {
        this.symbol = Objects.requireNonNull(symbol);
        this.priority = priority;
        this.operandCount = operandCount;
        this.conditionMode = conditionMode;
        this.action = Objects.requireNonNull(action);
    }
    
    public final int getOperandCount() { return operandCount; }
    public final int getPriority() { return priority; }
    public final String getSymbol() { return symbol; }
    
    public final void resolve(CodePool code, FieldPool fields, Environment env, Operand... operands) throws CompilationError
    {
        if(this.conditionMode && !env.isConditional())
            throw new CompilationError("Cannot use non conditional operators in conditional environment");
        if(!this.conditionMode && env.isConditional())
            throw new CompilationError("Cannot use conditional operators in non conditional environment");
        if(operands == null)
            throw new NullPointerException();
        if(operands.length != operandCount)
            throw new CompilationError("Expected " + operandCount + " operands in operator " + symbol + ". But found " + operands.length);
        action.apply(code, fields, env, operands);
    }
    
    @Override
    public final TokenType getTokenType() { return TokenType.OPERATOR_SYMBOL; }
    
    
    public static final OperatorSymbol
            OR = new OperatorSymbol("||", 0, 2, true, (code, fields, env, ops) -> {
                code.addCode(ScriptConstant.Token.OR);
                ops[0].resolve(code, fields, Environment.COND_DEEP);
                ops[1].resolve(code, fields, Environment.COND_DEEP);
            }),
            
            AND = new OperatorSymbol("&&", 1, 2, true, (code, fields, env, ops) -> {
                code.addCode(ScriptConstant.Token.AND);
                ops[0].resolve(code, fields, Environment.COND_DEEP);
                ops[1].resolve(code, fields, Environment.COND_DEEP);
            });
    
    @FunctionalInterface
    private interface OperatorAction
    {
        void apply(CodePool code, FieldPool fields, Environment env, Operand[] operands) throws CompilationError;
    }
}
