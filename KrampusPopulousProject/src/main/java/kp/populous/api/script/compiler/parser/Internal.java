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
public final class Internal implements UnparsedToken, ParsedToken
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
    
    public static final boolean isValidInternal(String token) { return ScriptConstant.Internal.decode(token) != null; }

    @Override
    public final void compile(CodePool code, FieldPool fields) throws CompilationError
    {
        code.addCode(fields.registerInternal(this));
    }
    
    @Override
    public final DataType getReturnType() { return DataType.INTEGER; }
}
