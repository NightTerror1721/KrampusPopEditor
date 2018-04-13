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

/**
 *
 * @author Asus
 */
public final class Internal implements UnparsedOperand, Operand
{
    private final ScriptConstant.Internal value;
    
    private Internal(ScriptConstant.Internal value) { this.value = Objects.requireNonNull(value); }
    
    public final ScriptConstant.Internal getValue() { return value; }

    @Override
    public final TokenType getTokenType() { return TokenType.INTERNAL; }
    
    public static final Internal parse(String token) throws CompilationError
    {
        ScriptConstant.Internal value = ScriptConstant.Internal.decode(token);
        if(value == null)
            throw new CompilationError("Invalid interla: " + token);
        return new Internal(value);
    }
    
    public static final Internal parseOrNull(String token)
    {
        ScriptConstant.Internal value = ScriptConstant.Internal.decode(token);
        if(value == null)
            return null;
        return new Internal(value);
    }
    
    public static final boolean isValidInternal(String token) { return ScriptConstant.Internal.decode(token) != null; }
    
    @Override
    public final void resolve(CodePool code, FieldPool fields, Environment env) throws CompilationError
    {
        switch(env)
        {
            case SUPERFICIAL: throw new CompilationError("Cannot put Internal here");
            case DEEP: fields.pushInternal(this); break;
            case COND_SUPERFICIAL:
                code.addCode(ScriptConstant.Token.NOT_EQUAL_TO);
                code.addCode(fields.registerInternal(this));
                code.addCode(fields.registerConstant(Constant.ZERO));
                break;
            case COND_DEEP: code.addCode(fields.registerInternal(this)); break;
            default: throw new IllegalStateException();
        }
    }
    
    @Override
    public boolean isCompatibleWithConditionals() { return true; }
    
    @Override
    public final String toString() { return value.getInternalName(); }
    
    @Override
    public final boolean equals(Object o)
    {
        if(o instanceof Internal)
        {
            Internal i = (Internal) o;
            return value.equals(i.value);
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.value);
        return hash;
    }
    
    public static final boolean isSetteableInternal(ScriptConstant.Internal internal)
    {
        return internal.name().startsWith("ATTR_");
    }
}
