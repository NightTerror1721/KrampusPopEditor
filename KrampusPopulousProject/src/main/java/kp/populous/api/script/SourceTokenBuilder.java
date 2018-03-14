/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import kp.populous.api.data.Data;
import kp.populous.api.script.ScriptConstant.Internal;
import kp.populous.api.script.ScriptConstant.Token;

/**
 *
 * @author Asus
 */
final class SourceTokenBuilder
{
    private final StringBuilder sb = new StringBuilder(16);
    
    public final int length() { return sb.length(); }
    public final boolean isEmpty() { return sb.length() <= 0; }
    
    public final SourceTokenBuilder append(byte value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(short value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(int value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(long value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(float value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(double value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(char value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(String value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(Data value) { sb.append(value); return this; }
    public final SourceTokenBuilder append(Token value) { sb.append(value.getTokenName()); return this; }
    public final SourceTokenBuilder append(Internal value) { sb.append(value.getInternalName()); return this; }
    public final SourceTokenBuilder append(Object value) { sb.append(value); return this; }
    
    public final void clear() { sb.delete(0, sb.length()); }
    
    public final SourceToken extract()
    {
        SourceToken token = new SourceToken(sb.toString());
        clear();
        return token;
    }
}
