/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import java.util.regex.Pattern;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public final class Variable implements UnparsedOperand, Operand
{
    private final String name;
    
    private Variable(String name) { this.name = Objects.requireNonNull(name); }
    
    public final String getName() { return name; }

    @Override
    public final TokenType getTokenType() { return TokenType.VARIABLE; }
    
    public static final Variable parse(String token) throws CompilationError
    {
        if(!isValidVariable(token))
            throw new CompilationError("Invalid variable:" + token);
        return new Variable(token);
    }
    
    public static final Variable parseOrNull(String token)
    {
        if(!isValidVariable(token))
            return null;
        return new Variable(token);
    }
    
    private static final Pattern PAT = Pattern.compile("\\$[_a-zA-Z0-9]+");
    public static final boolean isValidVariable(String token) { return PAT.matcher(token).matches(); }
    
    @Override
    public final void resolve(CodePool code, FieldPool fields, Environment env) throws CompilationError
    {
        switch(env)
        {
            case SUPERFICIAL: throw new CompilationError("Cannot put Variable here");
            case DEEP: fields.pushVariable(this); break;
            case COND_SUPERFICIAL:
                code.addCode(ScriptConstant.Token.NOT_EQUAL_TO);
                code.addCode(fields.registerVariable(this));
                code.addCode(fields.registerConstant(Constant.ZERO));
                break;
            case COND_DEEP: code.addCode(fields.registerVariable(this)); break;
            default: throw new IllegalStateException();
        }
    }
    
    @Override
    public boolean isCompatibleWithConditionals() { return true; }
}
