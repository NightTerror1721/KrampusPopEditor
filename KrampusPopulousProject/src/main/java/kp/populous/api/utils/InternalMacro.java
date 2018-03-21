/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;
import kp.populous.api.script.MacroPool;
import kp.populous.api.script.ScriptMacro;

/**
 *
 * @author Asus
 */
public final class InternalMacro implements ScriptMacro
{
    private final String name;
    private final BiFunction<MacroPool, String[], String> expandAction;
    private final boolean[] expandParameters;
    
    private InternalMacro(String name, BiFunction<MacroPool, String[], String> expandAction, boolean[] expandParameters)
    {
        this.name = Objects.requireNonNull(name);
        this.expandAction = Objects.requireNonNull(expandAction);
        this.expandParameters = Objects.requireNonNull(expandParameters);
    }
    
    @Override
    public final String getName() { return name; }
    
    @Override
    public final boolean expandParameter(int index) { return index < 0 || index >= expandParameters.length ? true : expandParameters[index]; }

    @Override
    public final String expand(MacroPool macros, String[] args) { return expandAction.apply(macros, args); }
    
    
    private static final HashMap<String, ScriptMacro> MACROS = new HashMap<>();
    
    public static final boolean existsMacro(String name) { return MACROS.containsKey(name); }
    
    public static final ScriptMacro getMacro(String name) { return MACROS.get(name); }
    
    public static final void registerMacro(String name, BiFunction<MacroPool, String[], String> expandAction, boolean... expandParameters)
    {
        if(existsMacro(name))
            throw new IllegalArgumentException("Macro " + name + " already exists");
        MACROS.put(name, new InternalMacro(name, expandAction,expandParameters));
    }
    
    
    
    /* INTERNAL INTERNAL MACROS */
    static {
        registerMacro("defined", (macros, args) -> {
            return args.length > 0 && macros.hasMacro(args[0]) ? "1" : "0";
        }, false);
    }
}
