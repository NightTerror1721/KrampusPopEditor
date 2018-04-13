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
public final class StopChar implements UnparsedOperand
{
    private final char symbol;
    
    private StopChar(char symbol) { this.symbol = symbol; }
    
    public final char getSymbol() { return symbol; }

    @Override
    public final TokenType getTokenType() { return TokenType.STOP_CHAR; }
    
    @Override
    public final String toString() { return Character.toString(symbol); }
    
    @Override
    public final boolean equals(Object o)
    {
        if(o instanceof StopChar)
        {
            StopChar sc = (StopChar) o;
            return symbol == sc.symbol;
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 3;
        hash = 31 * hash + this.symbol;
        return hash;
    }
    
    public static final StopChar
            COMMA = new StopChar(','),
            OPERATION_END = new StopChar(';'),
            LEFT_PARENTHESIS = new StopChar('('),
            RIGHT_PARENTHESIS = new StopChar(')'),
            SCOPE_END = new StopChar('}');
}
