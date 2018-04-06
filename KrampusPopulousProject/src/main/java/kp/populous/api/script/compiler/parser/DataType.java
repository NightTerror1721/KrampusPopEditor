/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.HashMap;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.compiler.CompilationError;

/**
 *
 * @author Asus
 */
public final class DataType
{
    private final ScriptConstant.Token specialToken;
    
    private DataType(ScriptConstant.Token specialToken) { this.specialToken = specialToken; }
    
    private static final HashMap<ScriptConstant.Token, DataType> SP = new HashMap<>();
    public static final DataType specialToken(ScriptConstant.Token specialToken) throws CompilationError
    {
        switch(specialToken)
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
            case GREEN: {
                DataType type = SP.get(specialToken);
                if(type == null)
                {
                    type = new DataType(specialToken);
                    SP.put(specialToken, type);
                }
                return type;
            }
            default: throw new CompilationError("Invalid Token: " + specialToken.getTokenName());
        }
    }
    
    public final boolean isVoid() { return this == VOID; }
    public final boolean isInteger() { return this == INTEGER; }
    public final boolean isSpecialToken() { return specialToken != null; }
    public final boolean isAnySpecialToken(ScriptConstant.Token... tokens)
    {
        for(ScriptConstant.Token token : tokens)
            if(token == specialToken)
                return true;
        return false;
    }
    
    public final ScriptConstant.Token getSpecialToken() { return specialToken; }
    
    
    public static final DataType VOID = new DataType(null);
    public static final DataType INTEGER = new DataType(null);
}
