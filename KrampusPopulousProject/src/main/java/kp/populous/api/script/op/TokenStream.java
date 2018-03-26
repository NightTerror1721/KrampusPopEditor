/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.op;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import kp.populous.api.script.CompilationException;

/**
 *
 * @author Asus
 */
public final class TokenStream
{
    private static final char EOF = '\uffff';
    
    private final char[] code;
    private final int line;
    private int it = -1;
    
    public TokenStream(String source, int line)
    {
        this.code = source.toCharArray();
        this.line = line;
    }
    
    private char moveChar(int index)
    {
        it += index;
        it = it < 0 ? -1 : it >= code.length ? code.length : it;
        return it >= code.length ? EOF : code[it];
    }
    private char nextChar() { return ++it >= code.length ? EOF : code[it]; }
    private char peekChar(int index)
    {
        index = it + index;
        return index < 0 || index >= code.length ? EOF : code[index];
    }
    private char peekChar() { return peekChar(0); }
    private boolean canPeekChar(int index) { return peekChar(index) != EOF; }
    private boolean canPeekChar() { return peekChar(0) != EOF; }
    
    private String getUntilChar(char end, boolean ignoreFirst) throws CompilationException
    {
        if(ignoreFirst)
            nextChar();
        int start = it;
        char c;
        while((c = nextChar()) != EOF)
            if(c == end)
            {
                return new String(code, start, it - start);
            }
        throw new CompilationException(line, "Expected \"" + end + "\" but not found.");
    }
    
    public final Token nextToken() throws CompilationException
    {
        StringBuilder sb = new StringBuilder(16);
        
        char c;
        loop:
        while((c = nextChar()) != EOF)
        {
            switch(c)
            {
                case '\r': break;
                case ' ':
                case '\t':
                case '\n':
                    if(sb.length() > 0)
                        break loop;
                    break;
                case '+':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.PLUS;
                case '-':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.MINUS;
                case '*':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.MULTIPLY;
                case '/':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.DIVIDE;
                case '%':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.MODULE;
                case '~':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.BIT_NEGATE;
                case '^':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return OperatorSymbol.BIT_XOR;
                case '=':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    if(nextChar() == '=')
                        return OperatorSymbol.EQUALS;
                    moveChar(-1);
                    throw new CompilationException(line, "Invalid operator '='");
                case '|':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    if(nextChar() == '|')
                        return OperatorSymbol.OR;
                    moveChar(-1);
                    return OperatorSymbol.BIT_OR;
                case '&':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    if(nextChar() == '&')
                        return OperatorSymbol.AND;
                    moveChar(-1);
                    return OperatorSymbol.BIT_AND;
                case '!':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    if(nextChar() == '=')
                        return OperatorSymbol.NOT_EQUALS;
                    moveChar(-1);
                    return OperatorSymbol.NEGATE;
                case '>':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    switch(nextChar())
                    {
                        case '>': return OperatorSymbol.BIT_RSH;
                        case '=': return OperatorSymbol.GREATER_EQUALS_THAN;
                        default:
                            moveChar(-1);
                            return OperatorSymbol.GREATER_THAN;
                    }
                case '<':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    switch(nextChar())
                    {
                        case '<': return OperatorSymbol.BIT_LSH;
                        case '=':
                            if(peekChar(1) == '>')
                            {
                                nextChar();
                                return OperatorSymbol.COMPARE;
                            }
                            return OperatorSymbol.LESS_EQUALS_THAN;
                        default:
                            moveChar(-1);
                            return OperatorSymbol.LESS_THAN;
                    }
                case '(':
                    if(sb.length() > 0)
                    {
                        moveChar(-1);
                        break loop;
                    }
                    return ParenthesisToken.wrap(getUntilChar(')', true), line);
                case ')':
                    throw new CompilationException(line, "Unexpected end of parenthesis block \")\"");
                default:
                    sb.append(c);
                    break;
            }
        }
        
        return sb.length() > 0 ? Token.identifier(sb.toString()) : null;
    }
    
    public final TokenIterator parseAllTokens() throws CompilationException
    {
        LinkedList<Token> tokens = new LinkedList<>();
        Token token;
        while((token = nextToken()) != null)
            tokens.add(token);
        return new TokenIterator(tokens.toArray(new Token[tokens.size()]), line);
    }
    
    
    public static final class TokenIterator implements Iterator<Token>
    {
        private final int line;
        final Token[] tokens;
        int it = 0;
        
        public TokenIterator(Token[] tokens, int line) { this.tokens = Objects.requireNonNull(tokens); this.line = line; }
        
        public final int getLine() { return line; }

        @Override
        public final boolean hasNext() { return it < tokens.length; }

        @Override
        public final Token next() { return tokens[it++]; }
        
        public final TokenIterator extractUntil(Token end) throws CompilationException
        {
            LinkedList<Token> list = new LinkedList<>();
            for(; hasNext(); it++)
            {
                Token token = tokens[it];
                if(token.equals(end))
                    return new TokenIterator(list.toArray(new Token[list.size()]), line);
                list.add(token);
            }
            throw new CompilationException(line, "Expected '" + end + "'");
        }
        
        public final TokenIterator sub(int start, int end)
        {
            Token[] array = new Token[end - start];
            System.arraycopy(tokens, start, array, 0, array.length);
            return new TokenIterator(array, line);
        }
        
        public final int length() { return tokens.length; }
    }
}
