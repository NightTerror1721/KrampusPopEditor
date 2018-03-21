/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.Collection;
import java.util.HashMap;
import kp.populous.api.utils.InternalMacro;

/**
 *
 * @author Asus
 */
public final class MacroPool
{
    private final HashMap<String, ScriptMacro> macros = new HashMap<>();
    
    public final void registerMacro(ScriptMacro macro, int line) throws CompilationException
    {
        if(macro == null)
            throw new NullPointerException();
        if(InternalMacro.existsMacro(macro.getName()))
            throw new CompilationException(line, "Cannot override internal macro " + macro.getName());
        macros.put(macro.getName(), macro);
    }
    
    public final void unregisterMacro(String name)
    {
        macros.remove(name);
    }
    
    public final ScriptMacro getMacro(String name)
    {
        return InternalMacro.existsMacro(name) ? InternalMacro.getMacro(name) : macros.get(name);
    }
    
    public final boolean hasMacro(String name) { return InternalMacro.existsMacro(name) || macros.containsKey(name); }
    
    public final void registerMacros(Collection<ScriptMacro> macros)
    {
        macros.stream().filter((macro) -> (!InternalMacro.existsMacro(macro.getName())))
                .forEachOrdered((macro) -> {
            this.macros.put(macro.getName(), macro);
        });
    }
    
    public final void registerMacros(MacroPool macros)
    {
        macros.macros.values().stream().filter((macro) -> (!InternalMacro.existsMacro(macro.getName())))
                .forEachOrdered((macro) -> {
            this.macros.put(macro.getName(), macro);
        });
    }
}
