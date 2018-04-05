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
public final class Constant implements UnparsedToken, ParsedToken
{
    private final Integer value;
    
    private Constant(Integer value) { this.value = Objects.requireNonNull(value); }
    
    public final Integer getValue() { return value; }

    @Override
    public final TokenType getTokenType() { return TokenType.CONSTANT; }
    
    public static final Constant parse(String token) throws CompilationError
    {
        try { return new Constant(Integer.decode(token)); }
        catch(NumberFormatException ex) { throw new CompilationError("Invalid constant: " + token); }
    }
    
    private static final Pattern PAT = Pattern.compile("[+-]?(0[xX])?[0-9]+");
    public static final boolean isValidConstant(String token) { return PAT.matcher(token).matches(); }

    @Override
    public final void compile(CodePool code, FieldPool fields) throws CompilationError
    {
        code.addCode(fields.registerConstant(this));
    }
}
