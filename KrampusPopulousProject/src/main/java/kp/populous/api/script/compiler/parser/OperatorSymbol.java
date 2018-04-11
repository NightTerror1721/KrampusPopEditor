/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.ScriptConstant.Token;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;
import kp.populous.api.script.compiler.FieldStack.StackValue;
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
    private final boolean conditional;
    private final OperatorAction action;
    
    private OperatorSymbol(String symbol, int priority, int operandCount, boolean conditional, OperatorAction action)
    {
        this.symbol = Objects.requireNonNull(symbol);
        this.priority = priority;
        this.operandCount = operandCount;
        this.conditional = conditional;
        this.action = Objects.requireNonNull(action);
    }
    
    public final String getSymbol() { return symbol; }
    public final int getOperandCount() { return operandCount; }
    public final int getPriority() { return priority; }
    public final boolean isConditionalSymbol() { return conditional; }
    
    public final void resolve(CodePool code, FieldPool fields, Environment env, Operand... operands) throws CompilationError
    {
        if(operands == null)
            throw new NullPointerException();
        if(operands.length != operandCount)
            throw new CompilationError("Expected " + operandCount + " operands in operator " + symbol + ". But found " + operands.length);
        action.apply(code, fields, env, operands);
    }
    
    @Override
    public final TokenType getTokenType() { return TokenType.OPERATOR_SYMBOL; }
    
    
    public static final OperatorSymbol
            ASSIGN = new OperatorSymbol("=", 0, 2, false, (code, fields, env, ops) -> {
                StackValue index = fields.pop();
                checkIsSetteable(index);
                StackValue valueIndex = fields.pop();
                code.addCode(Token.SET);
                code.addCode(index);
                code.addCode(valueIndex);
                fields.pushStackValue(index);
            }),
            ASSIGN_PLUS = new OperatorSymbol("+=", 0, 2, false, cominatedPlusMinusAssignation(true)),
            ASSIGN_MINUS = new OperatorSymbol("-=", 0, 2, false, cominatedPlusMinusAssignation(false)),
            ASSIGN_MULTIPLY = new OperatorSymbol("*=", 0, 2, false, cominatedMulDivAssignation(true)),
            ASSIGN_DIVIDE = new OperatorSymbol("/=", 0, 2, false, cominatedMulDivAssignation(false)),
            
            OR = new OperatorSymbol("||", 1, 2, true, conditionalAction(Token.OR)),
            
            AND = new OperatorSymbol("&&", 2, 2, true, conditionalAction(Token.AND)),
            
            EQUALS = new OperatorSymbol("==", 3, 2, true, conditionalAction(Token.EQUAL_TO)),
            NOT_EQUALS = new OperatorSymbol("!=", 3, 2, true, conditionalAction(Token.NOT_EQUAL_TO)),
            GREATER_THAN = new OperatorSymbol(">", 3, 2, true, conditionalAction(Token.GREATER_THAN)),
            LESS_THAN = new OperatorSymbol("<", 3, 2, true, conditionalAction(Token.LESS_THAN)),
            GREATER_EQUALS_THAN = new OperatorSymbol(">=", 3, 2, true, conditionalAction(Token.GREATER_THAN_EQUAL_TO)),
            EQULESS_EQUALS_THANALS = new OperatorSymbol("<=", 3, 2, true, conditionalAction(Token.LESS_THAN_EQUAL_TO)),
            
            PLUS = new OperatorSymbol("+", 4, 2, false, plusMinusOperation(true)),
            MINUS = new OperatorSymbol("-", 4, 2, false, plusMinusOperation(false)),
            
            MULTIPLY = new OperatorSymbol("*", 5, 2, false, mulDivOperation(true)),
            DIVIDE = new OperatorSymbol("/", 5, 2, false, mulDivOperation(false)),
            
            NEGATE = new OperatorSymbol("!", 6, 1, true, (code, fields, env, ops) -> {
                if(ops[0].isCompatibleWithConditionals())
                {
                    code.addCode(Token.NOT_EQUAL_TO);
                    ops[0].resolve(code, fields, Environment.COND_DEEP);
                    code.addCode(fields.registerConstant(Constant.ZERO));
                }
                else
                {
                    ops[0].resolve(code, fields, Environment.DEEP);

                    CondBlock cond = CondBlock.create(code);
                    cond.add(Token.EQUAL_TO);
                    code.addCode(fields.pop());
                    code.addCode(fields.registerConstant(Constant.ZERO));

                    StackValue index = fields.pushVolatile();

                    cond.If()
                            .add(Token.SET)
                            .add(index)
                            .add(fields.registerConstant(Constant.ONE))
                        .Else()
                            .add(Token.SET)
                            .add(index)
                            .add(fields.registerConstant(Constant.ZERO))
                        .end();
                }
            });
    
    @FunctionalInterface
    private interface OperatorAction
    {
        void apply(CodePool code, FieldPool fields, Environment env, Operand[] operands) throws CompilationError;
    }
    
    private static OperatorAction conditionalAction(Token token)
    {
        return (code, fields, env, ops) -> {
            if(ops[0].isCompatibleWithConditionals() && ops[1].isCompatibleWithConditionals())
            {
                code.addCode(token);
                ops[0].resolve(code, fields, Environment.COND_DEEP);
                ops[1].resolve(code, fields, Environment.COND_DEEP);
            }
            else
            {
                ops[0].resolve(code, fields, Environment.DEEP);
                ops[1].resolve(code, fields, Environment.DEEP);
                
                CondBlock cond = CondBlock.create(code);
                cond.add(token);
                code.addParametersFromStack(fields, 2);
                
                StackValue index = fields.pushVolatile();
                
                cond.If()
                        .add(Token.SET)
                        .add(index)
                        .add(fields.registerConstant(Constant.ONE))
                    .Else()
                        .add(Token.SET)
                        .add(index)
                        .add(fields.registerConstant(Constant.ZERO))
                    .end();
            }
        };
    }
    
    private static OperatorAction cominatedPlusMinusAssignation(boolean isPlus)
    {
        return (code, fields, env, ops) -> {
            StackValue index = fields.pop();
            checkIsSetteable(index);
            StackValue valueIndex = fields.pop();
            
            code.addCode(isPlus ? Token.INCREMENT : Token.DECREMENT);
            code.addCode(index);
            code.addCode(valueIndex);
            fields.pushStackValue(index);
        };
    }
    
    private static OperatorAction plusMinusOperation(boolean isPlus)
    {
        return (code, fields, env, ops) -> {
            StackValue index = fields.pop();
            StackValue valueIndex = fields.pop();
            StackValue result = fields.pushVolatile();
            
            code.addCode(Token.SET);
            code.addCode(index);
            code.addCode(result);
            
            code.addCode(isPlus ? Token.INCREMENT : Token.DECREMENT);
            code.addCode(result);
            code.addCode(valueIndex);
        };
    }
    
    private static OperatorAction cominatedMulDivAssignation(boolean isMul)
    {
        return (code, fields, env, ops) -> {
            StackValue index = fields.pop();
            checkIsSetteable(index);
            StackValue valueIndex = fields.pop();
            
            code.addCode(isMul ? Token.MULTIPLY : Token.DIVIDE);
            code.addCode(index);
            code.addCode(index);
            code.addCode(valueIndex);
            fields.pushStackValue(index);
        };
    }
    
    private static OperatorAction mulDivOperation(boolean isMul)
    {
        return (code, fields, env, ops) -> {
            StackValue index = fields.pop();
            StackValue valueIndex = fields.pop();
            StackValue result = fields.pushVolatile();
            
            code.addCode(isMul ? Token.MULTIPLY : Token.DIVIDE);
            code.addCode(result);
            code.addCode(index);
            code.addCode(valueIndex);
            fields.pushStackValue(index);
        };
    }
    
    private static boolean isSetteable(StackValue value)
    {
        if(value.isVariable())
            return true;
        if(value.isInternal())
        {
            ScriptConstant.Internal i = ScriptConstant.Internal.decode(value.index);
            return i != null && Internal.isSetteableInternal(i);
        }
        return false;
    }
    private static void checkIsSetteable(StackValue value) throws CompilationError
    {
        if(!isSetteable(value))
            throw new CompilationError("Expected valid Variable or setteable internal (ATTR_*).");
    }
    
    
    private static final class CondBlock
    {
        private final CodePool code;
        
        private CondBlock(CodePool code) { this.code = code; }
        
        public static final CondBlock create(CodePool code) throws CompilationError
        {
            CondBlock cb = new CondBlock(code);
            code.addCode(ScriptConstant.Token.IF);
            return cb;
        }
        
        public final CondBlock add(UInt16 code) throws CompilationError { this.code.addCode(code); return this; }
        public final CondBlock add(ScriptConstant.Internal internal) throws CompilationError { code.addCode(internal); return this; }
        public final CondBlock add(Token token) throws CompilationError { code.addCode(token); return this; }
        public final CondBlock add(StackValue value) throws CompilationError { code.addCode(value); return this; }
        
        public final CondBlock If() throws CompilationError { return add(Token.BEGIN); }
        public final CondBlock Else() throws CompilationError { return add(Token.END).add(Token.ELSE).add(Token.BEGIN); }
        public final CondBlock end() throws CompilationError { return add(Token.END).add(Token.ENDIF); }
    }
}
