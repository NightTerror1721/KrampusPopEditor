/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.Objects;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant.Token;

/**
 *
 * @author Asus
 */
public final class ScriptHandle
{
    private final Script script;
    private int current;
    
    public ScriptHandle(Script script)
    {
        this.script = Objects.requireNonNull(script);
    }
    
    public final UInt16 getCurrentCode() throws ScriptIOException
    {
        if(current < 0 || current >= script.codes.length)
            throw new ScriptIOException("Exceded script maximum code length");
        return script.codes.getCode(current);
    }
    public final boolean checkCurrentCode(UInt16 code) throws ScriptIOException { return getCurrentCode().equals(code); }
    public final boolean checkCurrentCode(Token token) throws ScriptIOException { return checkCurrentCode(token.getCode()); }
    
    public final boolean checkVersion() { return script.codes.getVersion() == ScriptConstant.SCRIPT_VERSION; }
    
    public final Field getCurrentField() throws ScriptIOException
    {
        int code = getCurrentCode().toInt();
        if(code < 0 || code >= script.fields.length)
            throw new ScriptIOException("Exceded script maximum fields length: " + code);
        return script.fields.getField(code);
    }
    
    public final ScriptHandle increase(int amount) throws ScriptIOException
    {
        current += amount;
        if(current < 0 || current >= script.codes.length)
            throw new ScriptIOException("Exceded script maximum code length: " + current);
        return this;
    }
    public final ScriptHandle increase() throws ScriptIOException { return ScriptHandle.this.increase(1); }
    
    public final ScriptHandle decrease(int amount) throws ScriptIOException
    {
        current -= amount;
        if(current < 0 || current >= script.codes.length)
            throw new ScriptIOException("Exceded script maximum code length");
        return this;
    }
    public final ScriptHandle decrease() throws ScriptIOException { return ScriptHandle.this.decrease(1); }
    
    public final UInt16 nextCode() throws ScriptIOException { return increase().getCurrentCode(); }
    public final Field nextField() throws ScriptIOException { return increase().getCurrentField(); }
    
    public final UInt16 previousCode() throws ScriptIOException { return decrease().getCurrentCode(); }
    public final Field previousField() throws ScriptIOException { return decrease().getCurrentField(); }
    
    public final boolean hasMoreCodes() { return current + 1 < script.codes.length; }
}
