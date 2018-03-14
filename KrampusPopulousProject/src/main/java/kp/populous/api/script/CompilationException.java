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
public final class CompilationException extends Exception
{
    public CompilationException(CodeReader code, String message)
    {
        super(msg(code, message));
    }
    
    
    private static String msg(CodeReader code, String message)
    {
        return "Error in line " + code.getCurrentLine() + ": " + message;
    }
}
