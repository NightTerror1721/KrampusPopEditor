/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

/**
 *
 * @author Asus
 */
public interface ScriptMacro
{
    String getName();
    boolean expandParameter(int index);
    //List<String> getParameters();
    //boolean isVarargs();
    String expand(MacroPool macros, String[] args);
}
