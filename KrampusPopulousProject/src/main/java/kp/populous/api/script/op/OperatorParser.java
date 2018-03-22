/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.op;

import kp.populous.api.script.CompilationException;
import kp.populous.api.script.op.TokenStream.TokenIterator;

/**
 *
 * @author Asus
 */
public final class OperatorParser
{
    private OperatorParser() {}
    
    
    public static final Operator parse(String line, int lineIdx) throws CompilationException
    {
        if(line.isEmpty())
            return Operator.getter("1");
        TokenIterator it = new TokenStream(line, lineIdx).parseAllTokens();
        return generateOperator(it);
    }
    
    private static Operator parseOperand(Token token, TokenIterator it) throws CompilationException
    {
        if(token == Token.OPEN_PARENTHESIS)
            return generateOperator(it.extractUntil(Token.CLOSE_PARENTHESIS));
        if(token.isOperator())
        {
            if(it.hasNext())
                throw new CompilationException(it.getLine(), "Expected valid value or macro after unary operator.");
            if(token == OperatorSymbol.NEGATE)
            {
                token = it.next();
                return Operator.unary(OperatorSymbol.NEGATE, Operator.getter(token.toString()));
            }
            if(token == OperatorSymbol.BIT_NEGATE)
            {
                token = it.next();
                return Operator.unary(OperatorSymbol.BIT_NEGATE, Operator.getter(token.toString()));
            }
        }
        return Operator.getter(token.toString());
    }
    
    private static Operator generateOperator(TokenIterator it) throws CompilationException
    {
        if(!it.hasNext())
            throw new IllegalStateException();
        Operator op1 = parseOperand(it.next(), it);
        if(!it.hasNext())
            return op1;
        return subGenerateOperator(op1, it);
    }
    
    private static Operator generatePart(TokenIterator it) throws CompilationException
    {Tu ubicación
        if(!it.hasNext())
            throw new IllegalStateException();
        Token op1 = it.next();
        return parseOperand(op1, it);
    }
    
    private static Operator subGenerateOperator(Operator op1, TokenIterator it) throws CompilationException
    {
        Token op = it.next();
        if(!op.isOperator())
            throw new CompilationException(it.getLine(), "Expected valid binary operator between values or macros. But found: " + op);
        OperatorSymbol opToken = checkValidOperatorToken(op, it);
        
        Operator op2 = generateOperatorPart(opToken, it);
        Operator operator = Operator.binary(opToken, op1, op2);
        
        if(!it.hasNext())
            return operator;
        return subGenerateOperator(operator, it);
    }
    
    private static Operator generateOperatorPart(OperatorSymbol op, TokenIterator it) throws CompilationException
    {
        OperatorSymbol nextOperator = findNextOperatorToken(it);
        if(nextOperator != null && op.comparePriority(nextOperator) >= 0)
            nextOperator = null;
        
        Operator op2;
        if(nextOperator != null)
            op2 = generateSuperOperatorScope(op, it);
        else op2 = generatePart(it);
        
        return op2;
    }
    
    private static OperatorSymbol findNextOperatorToken(TokenIterator it) throws CompilationException
    {
        for(int i=it.it;i<it.length();i++)
            if(isValidOperatorToken(it.tokens[i]) != null)
                return isValidOperatorToken(it.tokens[i]);
        return null;
    }
    
    private static Operator generateSuperOperatorScope(OperatorSymbol opBase, TokenIterator it) throws CompilationException
    {
        int start = it.it;
        for(; it.hasNext(); it.it++)
        {
            if(isValidOperatorToken(it.tokens[it.it]) == null)
                continue;
            OperatorSymbol op = isValidOperatorToken(it.tokens[it.it]);
            if(opBase.comparePriority(op) > 0)
                return generateOperator(it.sub(start, it.it));
        }
        it.it = start;
        return generateOperator(it);
    }
    
    private static OperatorSymbol checkValidOperatorToken(Token op, TokenIterator it) throws CompilationException
    {
        OperatorSymbol token = isValidOperatorToken(op);
        if(token == null)
            throw new CompilationException(it.getLine(), "Expected a valid operator.");
        return token;
    }
    
    private static OperatorSymbol isValidOperatorToken(Token op) throws CompilationException
    {
        return op.isOperator() ? (OperatorSymbol) op : null;
    }
}
