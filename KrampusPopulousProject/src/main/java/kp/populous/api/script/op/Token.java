/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.op;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public abstract class Token
{
    @Override
    public abstract String toString();
    
    public abstract boolean isOperator(); 
    
    public boolean isParenthesis() { return false; }
    public ParenthesisToken toParenthesis() { throw new UnsupportedOperationException(); }
    
    public static final Token identifier(String token) { return new Identifier(token); }
    
    private static class Identifier extends Token
    {
        private final String name;
        
        private Identifier(String name)
        {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public final String toString() { return name; }

        @Override
        public final boolean isOperator() { return false; }
    }
    
    private static final class Keyword extends Identifier
    {
        private Keyword(String name)
        {
            super(name);
        }
    }
    
    public static final Token OPEN_PARENTHESIS = new Keyword("(");
    public static final Token CLOSE_PARENTHESIS = new Keyword(")");
}
