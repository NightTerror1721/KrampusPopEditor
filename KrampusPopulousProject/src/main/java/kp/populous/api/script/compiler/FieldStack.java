/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler;

import java.util.LinkedList;
import java.util.Objects;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.compiler.FieldPool.VariableAllocator;
import kp.populous.api.script.compiler.parser.Constant;
import kp.populous.api.script.compiler.parser.Internal;
import kp.populous.api.script.compiler.parser.SpecialToken;
import kp.populous.api.script.compiler.parser.Variable;

public final class FieldStack
{
    private final VariableAllocator vars;
    private final FieldPool fields;
    private final LinkedList<StackValue> stack;
    
    FieldStack(FieldPool fields, VariableAllocator vars)
    {
        this.vars = Objects.requireNonNull(vars);
        this.fields = Objects.requireNonNull(fields);
        this.stack = new LinkedList<>();
    }
    
    public final StackValue pushVolatile() throws CompilationError
    {
        UInt16 index = vars.allocate();
        StackValue e = new StackValue(index, VOLATILE);
        stack.addFirst(e);
        return e;
    }
    
    private StackValue pushField(UInt16 index, int type)
    {
        StackValue e = new StackValue(index, type);
        stack.addFirst(e);
        return e;
    }
    public final StackValue pushVariable(Variable variable) throws CompilationError { return pushField(fields.registerVariable(variable), VARIABLE); }
    public final StackValue pushInternal(Internal internal) throws CompilationError { return pushField(fields.registerInternal(internal), INTERNAL); }
    public final StackValue pushConstant(Constant constant) throws CompilationError { return pushField(fields.registerConstant(constant), CONSTANT); }
    
    public final StackValue pushSpecialToken(SpecialToken token) throws CompilationError
    {
        if(!SpecialToken.isValidSpecialToken(token.getToken()))
            throw new CompilationError("Invalid special token: " + token.getToken().getTokenName());
        UInt16 code = token.getToken().getCode();
        StackValue e = new StackValue(code, SPTOKEN);
        stack.addFirst(e);
        return e;
    }
    
    public final StackValue pushStackValue(StackValue value) throws CompilationError
    {
        value.checkStack(this);
        if(value.isVolatile())
            throw new CompilationError("Expected valid (Internal, SpecialToken or Variable) but found operator result");
        stack.addFirst(value);
        return value;
    }
    
    public final StackValue peek() throws CompilationError
    {
        if(stack.isEmpty())
            throw new CompilationError("Empty stack");
        return stack.peekFirst();
    }
    
    public final StackValue pop() throws CompilationError
    {
        if(stack.isEmpty())
            throw new CompilationError("Empty stack");
        StackValue e = stack.removeFirst();
        if(e.isVolatile())
            vars.deallocate(e.index);
        return e;
    }
    
    
    public final class StackValue
    {
        public final UInt16 index;
        private final int type;
        
        private StackValue(UInt16 index, int type)
        {
            this.index = Objects.requireNonNull(index);
            this.type = type;
        }
        
        private void checkStack(FieldStack stack)
        {
            if(FieldStack.this != stack)
                throw new IllegalStateException();
        }
        
        public final boolean isVolatile() { return type == VOLATILE; }
        public final boolean isVariable() { return type == VARIABLE; }
        public final boolean isInternal() { return type == INTERNAL; }
        public final boolean isConstant() { return type == CONSTANT; }
        public final boolean isSpecialToken() { return type == SPTOKEN; }
    }
    
    private static final int VOLATILE = 0;
    private static final int VARIABLE = 1;
    private static final int INTERNAL = 2;
    private static final int CONSTANT = 3;
    private static final int SPTOKEN = 4;
}
