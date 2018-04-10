/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler;

import kp.populous.api.data.Raw;
import kp.populous.api.data.SByte;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.Script;
import kp.populous.api.script.ScriptConstant;

/**
 *
 * @author Asus
 */
public final class CodePool
{
    private final Raw data;
    private int index;
    
    public CodePool()
    {
        data = new Raw(ScriptConstant.MAX_CODES * 2);
        index = 1;
        
        data.signedByte(0, SByte.valueOf(12));
        data.signedByte(1, SByte.ZERO);
    }
    
    public final void addCode(UInt16 code) throws CompilationError
    {
        if(code == null)
            throw new NullPointerException();
        if(index >= ScriptConstant.MAX_CODES)
            throw new CompilationError("Number of Code overflow");
        data.unsignedInt16((index++) * 2, code);
    }
    public final void addCode(ScriptConstant.Token token) throws CompilationError { addCode(token.getCode()); }
    public final void addCode(ScriptConstant.Internal internal) throws CompilationError { addCode(internal.getCode()); }
    
    public final void addParametersFromStack(FieldPool fields, int amount) throws CompilationError
    {
        UInt16[] aux = new UInt16[amount];
        for(int i=aux.length-1;i>=0;i--)
            aux[i] = fields.pop();
        for(UInt16 idx : aux)
            addCode(idx);
    }
    
    public final void fillScriptCode(Script script)
    {
        script.codes.copyFrom(data);
    }
}
