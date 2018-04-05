/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import java.util.regex.Pattern;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public final class Variable implements UnparsedToken, ParsedToken
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
    
    private static final Pattern PAT = Pattern.compile("\\$[_a-zA-Z0-9]+");
    public static final boolean isValidVariable(String token) { return PAT.matcher(token).matches(); }

    @Override
    public final void compile(CodePool code, FieldPool fields) throws CompilationError
    {
        code.addCode(fields.registerVariable(this));
    }
}
