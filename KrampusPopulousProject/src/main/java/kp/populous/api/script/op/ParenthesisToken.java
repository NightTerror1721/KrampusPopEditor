/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.op;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import kp.populous.api.script.CompilationException;
import kp.populous.api.script.op.TokenStream.TokenIterator;

/**
 *
 * @author Marc
 */
public class ParenthesisToken extends Token
{
    private final List<Token> tokens;
    private final int lineIdx;
    
    private ParenthesisToken(List<Token> tokens, int lineIdx)
    {
        this.tokens = Objects.requireNonNull(tokens);
        this.lineIdx = lineIdx;
    }
    
    public static final ParenthesisToken wrap(String text, int lineIdx) throws CompilationException
    {
        TokenStream ts = new TokenStream(text, lineIdx);
        LinkedList<Token> tokens = new LinkedList<>();
        Token token;
        while((token = ts.nextToken()) != null)
            tokens.add(token);
        return new ParenthesisToken(Collections.unmodifiableList(tokens), lineIdx);
    }
    
    public final TokenIterator getTokenIterator() { return new TokenIterator(tokens.toArray(new Token[tokens.size()]), lineIdx); }
    
    @Override
    public final String toString() { return tokens.toString(); }

    @Override
    public final boolean isOperator() { return false; }
    
    @Override
    public final boolean isParenthesis() { return true; }
    
    @Override
    public final ParenthesisToken toParenthesis() { return this; }
    
}
