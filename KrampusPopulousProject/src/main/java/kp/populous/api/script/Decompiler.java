/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import kp.populous.api.data.Data;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant.Internal;
import kp.populous.api.script.ScriptConstant.Token;
import kp.populous.api.script.ScriptFunctions.Function;
import kp.populous.api.script.ScriptFunctions.Parameter;

/**
 *
 * @author Asus
 */
final class Decompiler
{
    private final ScriptHandle it;
    private final StringBuilder source;
    private final LinkedList<String> errors = new LinkedList<>();
    private final Indent indent = new Indent();
    
    Decompiler(Script script)
    {
        this.it = new ScriptHandle(script);
        this.source = new StringBuilder(512);
    }
    
    public final String getSource() { return source.toString(); }
    public final List<String> getErrors() { return new ArrayList<>(errors); }
    public final boolean hasErrors() { return !errors.isEmpty(); }
    
    public final boolean decompile() throws ScriptIOException
    {
        if(!it.checkVersion())
        {
            error("Wrong version");
            writeln("/* WRONG VERSION: " + it.getCurrentCode() + " */");
        }
        it.increase();
        
        if(!decompileBody(true))
            return false;
        
        if(!it.checkCurrentCode(Token.SCRIPT_END))
        {
            error("Expected END OF SCRIPT but not found");
            return false;
        }
        return errors.isEmpty();
    }
    
    private boolean decompileBody(boolean first) throws ScriptIOException
    {
        checkExpected(Token.BEGIN);
        it.increase();
        
        if(!first)
            indent.increase();
        
        for(;;)
        {
            if(it.checkCurrentCode(Token.END))
            {
                indent.decrease();
                indent.write();
                it.increase();
                return true;
            }
            
            indent.write();
            
            Token code = Token.decode(it.getCurrentCode());
            if(code == null)
            {
                error("Unknown command");
                skipError();
                continue;
            }
            
            switch(code)
            {
                case DO: {
                    if(!decompileDo())
                        skipError();
                } break;
                case EVERY: {
                    if(!decompileEvery())
                        skipError();
                } break;
                case INCREMENT:
                case DECREMENT:
                case SET: {
                    if(!decompileIncDecSet(code))
                        skipError();
                } break;
                case MULTIPLY:
                case DIVIDE: {
                    if(!decompileMulDiv(code))
                        skipError();
                } break;
                case IF: {
                    if(!decompileIf())
                        skipError();
                } break;
                default: {
                    error("Unknown command");
                    skipError();
                } break;
            }
            
            writeln();
        }
    }
    
    
    private boolean decompileDo() throws ScriptIOException
    {
        if(!it.checkCurrentCode(Token.DO))
        {
            errorExpected(Token.DO);
            return false;
        }
        it.increase();
        
        Token token = Token.decode(it.getCurrentCode());
        if(token == null)
        {
            error("Unknown DO command (functon)");
            return false;
        }
        Function func = ScriptFunctions.get(token);
        if(func == null)
        {
            error("Unknown DO command (functon): " + token);
            return false;
        }
        it.increase();
        
        write(func.getCommandToken().getFunctionName() + "(");
        int len = func.getParameterCount();
        boolean error = false;
        for(int i=0;i<len;i++)
        {
            if(!error)
            {
                if(!decompileParameter(func, i))
                    error = true;
            }
            else write("?");
            if(i + 1 < len)
                write(", ");
            it.increase();
        }
        write(")");
        
        return true;
    }
    
    private boolean decompileParameter(Function func, int index) throws ScriptIOException
    {
        if(index >= func.getParameterCount())
            throw new IllegalStateException();
        Parameter par = func.getParameter(index);
        UInt16 code = it.getCurrentCode();
        if(par.isValidToken(code))
        {
            write(Token.decode(code));
            return true;
        }
        if(par.allowField())
            return decompileCurrentField();
        return false;
    }
    
    private boolean decompileCurrentField() throws ScriptIOException
    {
        Field field = it.getCurrentField();
        switch(field.getFieldType())
        {
            
            case USER:
                if(field.getIndex().toInt() >= ScriptConstant.MAX_VARS)
                {
                    error("User Variable overflow");
                    return false;
                }
            case INTERNAL:
            case CONSTANT:
                write(field);
                return true;
            default:
                error("Unknown variable type");
                return false;
        }
    }
    
    
    private boolean decompileEvery() throws ScriptIOException
    {
        if(!it.checkCurrentCode(Token.EVERY))
        {
            errorExpected(Token.EVERY);
            return false;
        }
        it.increase();
        
        write("every(");
        
        if(!decompileEveryConstant())
        {
            write("?) {}");
            return false;
        }
        it.increase();
        
        if(!it.checkCurrentCode(Token.BEGIN))
        {
            write(", ");
            if(!decompileEveryConstant())
            {
                write("?) {}");
                return false;
            }
            it.increase();
        }
        writeln(") {");
        
        boolean res = decompileBody(false);
        write("}");
        return res;
    }
    
    private boolean decompileEveryConstant() throws ScriptIOException
    {
        Field field = it.getCurrentField();
        if(!field.isConstant())
        {
            error("Expected Constant");
            return false;
        }
        write(Integer.toString(field.getValue().toInt() + 1));
        return true;
    }
    
    
    private boolean decompileIncDecSet(Token token) throws ScriptIOException
    {
        if(!it.checkCurrentCode(token))
        {
            error("Expected " + token.name() + " token");
            return false;
        }
        it.increase();
        
        write(token.getTokenName() + "(");
        if(!decompileCurrentField())
        {
            it.increase(2);
            error("Malformed " + token.name() + " command");
            return true;
        }
        it.increase();
        
        write(", ");
        if(!decompileCurrentField())
        {
            it.increase();
            error("Malformed " + token.name() + " command");
            return true;
        }
        it.increase();
        
        write(")");
        
        return true;
    }
    
    private boolean decompileMulDiv(Token token) throws ScriptIOException
    {
        if(!it.checkCurrentCode(token))
        {
            error("Expected " + token.name() + " token");
            return false;
        }
        it.increase();
        
        write(token.getTokenName() + "(");
        if(!decompileCurrentField())
        {
            it.increase(3);
            error("Malformed " + token.name() + " command");
            return true;
        }
        it.increase();
        
        write(", ");
        if(!decompileCurrentField())
        {
            it.increase(2);
            error("Malformed " + token.name() + " command");
            return true;
        }
        it.increase();
        
        write(", ");
        if(!decompileCurrentField())
        {
            it.increase();
            error("Malformed " + token.name() + " command");
            return true;
        }
        it.increase();
        
        write(")");
        
        return true;
    }
    
    
    private boolean decompileIf() throws ScriptIOException
    {
        if(!it.checkCurrentCode(Token.IF))
        {
            errorExpected(Token.IF);
            return false;
        }
        it.increase();
        
        write("if(");
        if(!decompileCondition())
            return false;
        
        writeln(") {");
        
        if(!decompileBody(false))
            fatal("Invalid IF command");
        write("}");
        
        if(it.checkCurrentCode(Token.ELSE))
        {
            it.increase();
            writeln(" else {");
            if(!decompileBody(false))
                fatal("Invalid ELSE command");
            write("}");
        }
        
        if(!it.checkCurrentCode(Token.ENDIF))
        {
            errorExpected(Token.ENDIF);
            return false;
        }
        it.increase();
        
        return true;
    }
    
    private boolean decompileCondition() throws ScriptIOException
    {
        Token token = Token.decode(it.getCurrentCode());
        if(token == null)
        {
            error("Invalid command");
            return false;
        }
        switch(token)
        {
            case AND:
            case OR:
                if(!decompileLogic(token))
                    return false;
                break;
            case GREATER_THAN:
            case LESS_THAN:
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case GREATER_THAN_EQUAL_TO:
            case LESS_THAN_EQUAL_TO:
                if(!decompileTest(token))
                    return false;
                break;
            default:
                error("Invalid command");
                return false;
        }
        return true;
    }
    
    private boolean decompileLogic(Token token) throws ScriptIOException
    {
        it.increase();
        
        if(!decompileCondition())
            return false;
        
        write(" " + token.getTokenName() + " ");
        
        return decompileCondition();
    }
    
    private boolean decompileTest(Token token) throws ScriptIOException
    {
        it.increase();
        
        if(!decompileCurrentField())
        {
            error("Expected valid field");
            return false;
        }
        it.increase();
        
        write(" " + token.getTokenName() + " ");
        
        if(!decompileCurrentField())
        {
            error("Expected valid field");
            return false;
        }
        it.increase();
        
        return true;
    }
    
    
    
    
    
    
    
    
    
    private void writeln() { source.append('\n'); }
    private void write(String string) { source.append(string); }
    private void writeln(String string) { source.append(string).append('\n'); }
    private void write(Data data) { source.append(data.toString()); }
    private void writeln(Data data) { source.append(data.toString()).append('\n'); }
    private void write(Token token) { source.append(token.getTokenName()); }
    private void writeln(Token token) { source.append(token.getTokenName()).append('\n'); }
    private void write(Internal internal) { source.append(internal.getInternalName()); }
    private void writeln(Internal internal) { source.append(internal.getInternalName()).append('\n'); }
    private void write(Field field)
    {
        switch(field.getFieldType())
        {
            case USER: write("$" + field.getIndex()); break;
            case INTERNAL: {
                Internal in = Internal.decode(UInt16.valueOf(field.getIndex()));
                write(in == null ? "?" : in.getInternalName());
            } break;
            case CONSTANT: write(field.getValue().toString()); break;
            default: write("?"); break;
        }
    }
    private void writeln(Field field) { write(field); writeln(); }
    private void writeIndent()
    {
        if(!indent.inBase())
            write(indent.indent);
    }
    
    private void error(String message) { errors.add(message); }
    private void fatal(String message) throws ScriptIOException { throw new ScriptIOException(message); }
    private void fatal(Throwable cause) throws ScriptIOException { throw new ScriptIOException(cause); }
    private void fatal(String message, Throwable cause) throws ScriptIOException { throw new ScriptIOException(message, cause); }
    
    private void skipError() throws ScriptIOException
    {
        do {
            UInt16 code = it.nextCode();
            if(code.toInt() >= ScriptConstant.TOKEN_OFFSET || code.toInt() == 0)
                return;
        } while(it.hasMoreCodes());
    }
    
    private void skip(int amount) throws ScriptIOException
    {
        while(it.hasMoreCodes())
        {
            it.increase();
            if(--amount >= 0)
                return;
        }
        fatal("Unexpected end of script");
    }
    
    private void errorExpected(Token expected) throws ScriptIOException
    {
        error("Expected " + expected + " but found " + Token.decode(it.getCurrentCode()));
    }
    private void checkExpected(Token token) throws ScriptIOException
    {
        if(!it.checkCurrentCode(token))
            errorExpected(token);
    }
    
    private final class Indent
    {
        private String indent = "";
        
        public final void increase() { indent += "    "; }
        public final void decrease()
        {
            if(!inBase())
                indent = indent.substring(4);
        }
        public final boolean inBase() { return indent.length() == 0; }
        public final void write() { writeIndent(); }
    }
}
