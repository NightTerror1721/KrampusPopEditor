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
    private final int line;
    private final String errorMsg;
    
    public CompilationException(CodeReader code, String message)
    {
        super(msg(code, message));
        this.line = code.getCurrentLine();
        this.errorMsg = message;
    }
    
    public final int getLine() { return line; }
    public final String getErrorMessage() { return errorMsg; }
    
    
    private static String msg(CodeReader code, String message)
    {
        return "Error in line " + code.getCurrentLine() + ": " + message;
    }
}
