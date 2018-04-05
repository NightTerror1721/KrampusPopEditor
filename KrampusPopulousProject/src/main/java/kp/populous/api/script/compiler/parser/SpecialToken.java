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
public final class SpecialToken implements UnparsedToken, ParsedToken
{
    private final ScriptConstant.Token token;
    
    private SpecialToken(ScriptConstant.Token token) { this.token = Objects.requireNonNull(token); }
    
    public final ScriptConstant.Token getToken() { return token; }
    
    @Override
    public final TokenType getTokenType() { return TokenType.SPECIAL_TOKEN; }
    
    public static final SpecialToken parse(String token) throws CompilationError
    {
        if(!isValidSpecialToken(token))
            throw new CompilationError("Invalid token :" + token);
        return new SpecialToken(ScriptConstant.Token.decode(token));
    }
    
    public static final boolean isValidSpecialToken(String token)
    {
        ScriptConstant.Token value = ScriptConstant.Token.decode(token);
        if(value == null || value.isFunction())
            return false;
        switch(value)
        {
            case ON:
            case OFF:
            case COUNT_WILD:
            case ATTACK_MARKER:
            case ATTACK_BUILDING:
            case ATTACK_PERSON:
            case ATTACK_NORMAL:
            case ATTACK_BY_BOAT:
            case ATTACK_BY_BALLON:
            case GUARD_NORMAL:
            case GUARD_WITH_GHOSTS:
            case BLUE:
            case RED:
            case YELLOW:
            case GREEN:
                return true;
            default: return false;
        }
    }

    @Override
    public void compile(CodePool code, FieldPool fields) throws CompilationError
    {
        
    }
    
}
