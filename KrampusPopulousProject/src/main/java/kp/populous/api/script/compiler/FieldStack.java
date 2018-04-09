/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler;

import java.util.LinkedList;
import java.util.Objects;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant.Token;
import kp.populous.api.script.compiler.FieldPool.VariableAllocator;
import kp.populous.api.script.compiler.parser.SpecialToken;

public final class FieldStack
{
    private final VariableAllocator vars;
    private final LinkedList<Entry> stack;
    
    FieldStack(VariableAllocator vars)
    {
        this.vars = Objects.requireNonNull(vars);
        this.stack = new LinkedList<>();
    }
    
    public final UInt16 pushVolatile() throws CompilationError
    {
        UInt16 index = vars.allocate();
        stack.addFirst(new Entry(index, true));
        return index;
    }
    public final UInt16 pushField(UInt16 index) throws CompilationError
    {
        if(!vars.hasAllocated(index))
            throw new CompilationError("Unallocated index: " + index);
        stack.addFirst(new Entry(index, false));
        return index;
    }
    public final UInt16 pushSpecialToken(Token token) throws CompilationError
    {
        if(!SpecialToken.isValidSpecialToken(token))
            throw new CompilationError("Invalid special token: " + token.getTokenName());
        UInt16 code = token.getCode();
        stack.addFirst(new Entry(code, false));
        return code;
    }
    
    public final UInt16 peek() throws CompilationError
    {
        if(stack.isEmpty())
            throw new CompilationError("Empty stack");
        Entry e = stack.peekFirst();
        return e.index;
    }
    
    public final UInt16 pop() throws CompilationError
    {
        if(stack.isEmpty())
            throw new CompilationError("Empty stack");
        Entry e = stack.removeFirst();
        if(e.isVolatile)
            vars.deallocate(e.index);
        return e.index;
    }
    
    
    private static final class Entry
    {
        private final UInt16 index;
        private final boolean isVolatile;
        
        private Entry(UInt16 index, boolean isVolatile)
        {
            this.index = Objects.requireNonNull(index);
            this.isVolatile = isVolatile;
        }
    }
}
