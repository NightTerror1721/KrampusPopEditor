/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.Objects;
import java.util.regex.Pattern;
import kp.populous.api.script.ScriptConstant.Internal;
import kp.populous.api.script.ScriptConstant.Token;

/**
 *
 * @author Asus
 */
public final class SourceToken implements Comparable<SourceToken>
{
    private final String token;
    
    public SourceToken(String token)
    {
        this.token = Objects.requireNonNull(token);
    }
    
    public final int length() { return token.length(); }
    public final boolean isEmpty() { return token.isEmpty(); }
    
    public final Token toToken() { return Token.decode(token); }
    public final Internal toInternal() { return Internal.decode(token); }
    public final Integer toInteger() { return Integer.decode(token); }
    
    public final boolean checkPattern(Pattern pattern) { return pattern.matcher(token).matches(); }
    public final boolean isValidVariable() { return checkPattern(PATTERN_VAR); }
    public final boolean isValidConstant() { return checkPattern(PATTERN_CONST); }
    public final boolean isValidInternal() { return toInternal() != null; }
    public final boolean isValidToken() { return toToken() != null; }
    
    public final boolean isValidValue()
    {
        if(checkPattern(PATTERN_VAR) || checkPattern(PATTERN_CONST) || isValidInternal())
            return true;
        Token t = toToken();
        return t != null && !t.isFunction();
    }
    
    public final boolean isEndLine() { return token.equals("\n"); }
    
    public final boolean equals(SourceToken stoken) { return token.equals(stoken.token); }
    
    @Override
    public final boolean equals(Object o)
    {
        return o instanceof SourceToken &&
                token.equals(((SourceToken)o).token);
    }

    @Override
    public final int hashCode()
    {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.token);
        return hash;
    }
    
    @Override
    public final String toString() { return token; }

    @Override
    public final int compareTo(SourceToken o) { return token.compareTo(o.token); }
    
    
    private static final Pattern PATTERN_VAR = Pattern.compile("\\$[A-Za-z0-9_]+");
    public static final boolean isValidVariable(String token) { return PATTERN_VAR.matcher(token).matches(); }
    
    private static final Pattern PATTERN_CONST = Pattern.compile("-?[0-9]+");
    public static final boolean isValidConstant(String token) { return PATTERN_CONST.matcher(token).matches(); }
    
    public static final boolean isValidInternal(String token) { return Internal.decode(token) != null; }
    
    
    
    public static final SourceToken ENDLINE = new SourceToken("\n");
    public static final SourceToken PARENTHESIS_BEGIN = new SourceToken("(");
    public static final SourceToken PARENTHESIS_END = new SourceToken(")");
    public static final SourceToken BODY_BEGIN = new SourceToken("{");
    public static final SourceToken BODY_END = new SourceToken("}");
    public static final SourceToken COMMA = new SourceToken(",");
    public static final SourceToken EQUAL_TO = new SourceToken("==");
    public static final SourceToken NOT_EQUAL_TO = new SourceToken("!=");
    public static final SourceToken GREATER_THAN = new SourceToken(">");
    public static final SourceToken GREATER_THAN_EQUAL_TO = new SourceToken(">=");
    public static final SourceToken LESS_THAN = new SourceToken("<");
    public static final SourceToken LESS_THAN_EQUAL_TO = new SourceToken("<=");
    public static final SourceToken NEGATE = new SourceToken("!");
    public static final SourceToken ZERO = new SourceToken("0");
}
