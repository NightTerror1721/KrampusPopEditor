/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.io.EOFException;
import java.util.Objects;
import kp.populous.api.script.CodeReader;
import kp.populous.api.script.ScriptConstant.Token;
import kp.populous.api.script.ScriptFunctions;
import kp.populous.api.script.compiler.CompilationError;

/**
 *
 * @author Asus
 */
public final class SourceTokenizer
{
    private final CodeReader source;
    private final Accumulator sb = new Accumulator();
    
    public SourceTokenizer(CodeReader source)
    {
        this.source = Objects.requireNonNull(source);
    }
    
    public final UnparsedOperand nextToken() throws EOFException
    {
        if(!source.hasNext())
            return null;
        
        sb.tryClear();
        try
        {
            loop:
            for(;;)
            {
                char c = source.next();
                switch(c)
                {
                    case '\r':
                        break;
                    case ' ':
                    case '\n':
                    case '\t':
                        if(sb.isEmpty())
                            break;
                        break loop;
                        
                }
            }
        }
        catch(EOFException ex) {}
        
        return sb.isEmpty() ? null : parseToken(sb.getAndClear());
    }
    
    private UnparsedOperand parseToken(String stoken) throws CompilationError
    {
        switch(stoken)
        {
            case "set": return InstructionId.SET;
            case "inc": return InstructionId.INC;
            case "dec": return InstructionId.DEC;
            case "mul": return InstructionId.MUL;
            case "div": return InstructionId.DIV;
            case "if": return InstructionId.IF;
            case "else": return InstructionId.ELSE;
            case "every": return InstructionId.EVERY;
        }
        UnparsedOperand uo;
        
        if((uo = SpecialToken.parseOrNull(stoken)) != null)
            return uo;
        if((uo = Internal.parseOrNull(stoken)) != null)
            return uo;
        if((uo = Variable.parseOrNull(stoken)) != null)
            return uo;
        if((uo = Constant.parseOrNull(stoken)) != null)
            return uo;
        Token token = Token.decode(stoken);
        if(token != null)
        {
            ScriptFunctions.Function func = ScriptFunctions.get(token);
            if(func != null)
                return parseFunction(func);
        }
        throw new CompilationError("Invalid token: " + stoken);
    }
    
    private Function parseFunction(ScriptFunctions.Function ref)
    {
        
    }
    
    
    private static final class Accumulator
    {
        private final StringBuilder sb = new StringBuilder(32);
        
        public final Accumulator add(String value) { sb.append(value); return this; }
        public final Accumulator add(byte value) { sb.append(value); return this; }
        public final Accumulator add(short value) { sb.append(value); return this; }
        public final Accumulator add(int value) { sb.append(value); return this; }
        public final Accumulator add(long value) { sb.append(value); return this; }
        public final Accumulator add(float value) { sb.append(value); return this; }
        public final Accumulator add(double value) { sb.append(value); return this; }
        public final Accumulator add(boolean value) { sb.append(value); return this; }
        public final Accumulator add(char value) { sb.append(value); return this; }
        public final Accumulator add(Object value) { sb.append(value); return this; }
        
        public final boolean isEmpty() { return sb.length() <= 0; }
        public final int length() { return sb.length(); }
        
        public final void clear() { sb.delete(0, sb.length()); }
        
        public final String get() { return sb.toString(); }
        public final String getAndClear()
        {
            String value = get();
            clear();
            return value;
        }
        
        public final void tryClear()
        {
            if(!isEmpty())
                clear();
        }
        
        @Override
        public final String toString() { return sb.toString(); }
    }
}
