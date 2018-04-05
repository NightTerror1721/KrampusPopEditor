/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.ScriptFunctions;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public final class Function implements UnparsedToken, ParsedToken
{
    private final ScriptFunctions.Function ref;
    private final ParsedToken[] parameters;
    
    private Function(ScriptFunctions.Function ref, ParsedToken[] parameters)
    {
        this.ref = Objects.requireNonNull(ref);
        this.parameters = Objects.requireNonNull(parameters);
    }
    
    public final ScriptConstant.Token getToken() { return ref.getCommandToken(); }
    
    public final int getParameterCount() { return parameters.length; }
    public final ParsedToken getParameter(int index) { return parameters[index]; }
    
    @Override
    public final TokenType getTokenType() { return TokenType.FUNCTION; }

    @Override
    public final void compile(CodePool code, FieldPool fields) throws CompilationError
    {
        
    }
    
    public static final Function parse(ScriptConstant.Token id, ParsedToken[] pars) throws CompilationError
    {
        ScriptFunctions.Function ref = ScriptFunctions.get(id);
        if(ref == null)
            throw new CompilationError(id + " is not a valid function");
        if(ref.getParameterCount() != pars.length)
            throw new CompilationError("Function " + id + " required " + ref.getParameterCount() + " parameters. But found " + pars.length);
        for(int i=0;i<pars.length;i++)
        {
            ScriptFunctions.Parameter refPar = ref.getParameter(i);
            ParsedToken par = pars[i];
            switch(par.getTokenType())
            {
                case CONSTANT:
                case VARIABLE:
                case INTERNAL:
                    if(!refPar.allowField())
                        throw new CompilationError("Expected " + refPar.possibleValues() + " for parameter " + i + " in function " + id);
                    break;
                //case 
            }
        }
        return new Function(ref, pars);
    }
}
