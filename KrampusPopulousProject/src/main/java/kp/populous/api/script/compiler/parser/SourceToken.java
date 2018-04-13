/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

/**
 *
 * @author Asus
 */
public interface SourceToken
{
    TokenType getTokenType();
    
    default boolean is(TokenType type) { return getTokenType() == type; }
    default boolean is(TokenType type0, TokenType type1)
    {
        TokenType type = getTokenType();
        return type == type0 || type == type1;
    }
    default boolean is(TokenType type0, TokenType type1, TokenType type2)
    {
        TokenType type = getTokenType();
        return type == type0 || type == type1 || type == type2;
    }
    default boolean is(TokenType... types)
    {
        TokenType type = getTokenType();
        for(TokenType t : types)
            if(type == t)
                return true;
        return false;
    }
    
    default boolean isField()
    {
        switch(getTokenType())
        {
            case CONSTANT:
            case VARIABLE:
            case INTERNAL:
                return true;
            default: return false;
        }
    }
    
    @Override
    String toString();
    
    @Override
    boolean equals(Object o);
}
