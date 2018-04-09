/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.ScriptFunctions;
import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public final class Function implements UnparsedOperand, Operand
{
    private final ScriptFunctions.Function ref;
    private final Operand[] parameters;
    
    private Function(ScriptFunctions.Function ref, Operand[] parameters)
    {
        this.ref = Objects.requireNonNull(ref);
        this.parameters = Objects.requireNonNull(parameters);
    }
    
    public final ScriptConstant.Token getToken() { return ref.getCommandToken(); }
    
    public final int getParameterCount() { return parameters.length; }
    public final Operand getParameter(int index) { return parameters[index]; }
    
    @Override
    public final TokenType getTokenType() { return TokenType.FUNCTION; }
    
    @Override
    public final void resolve(CodePool code, FieldPool fields, Environment env) throws CompilationError
    {
        if(env.isConditional())
            throw new CompilationError("Cannot call function in conditional environment");
        if(!env.isSuperficial())
            throw new CompilationError("Function " + ref.getCommandToken().getFunctionName() + " cannot return any value");
        for(Operand par : parameters)
            par.resolve(code, fields, Environment.DEEP);
        UInt16[] indices = new UInt16[parameters.length];
        for(int i=indices.length-1;i>=0;i--)
            indices[i] = fields.pop();
        code.addCode(ScriptConstant.Token.DO);
        code.addCode(ref.getCommandToken());
        for(UInt16 index : indices)
            code.addCode(index);
    }
    
    public static final Function parse(ScriptConstant.Token id, Operand[] pars) throws CompilationError
    {
        ScriptFunctions.Function ref = ScriptFunctions.get(id);
        if(ref == null)
            throw new CompilationError(id + " is not a valid function");
        if(ref.getParameterCount() != pars.length)
            throw new CompilationError("Function " + id + " required " + ref.getParameterCount() + " parameters. But found " + pars.length);
        for(int i=0;i<pars.length;i++)
        {
            ScriptFunctions.Parameter refPar = ref.getParameter(i);
            Operand par = pars[i];
            switch(par.getTokenType())
            {
                case CONSTANT:
                case INTERNAL:
                case VARIABLE:
                    if(!refPar.allowField())
                        throw new CompilationError("Expected " + refPar.possibleValues() + " for parameter " + i + " in function " + id);
                    break;
                case SPECIAL_TOKEN:
                    if(!refPar.isValidToken(((SpecialToken) par).getToken()))
                        throw new CompilationError("Expected " + refPar.possibleValues() + " for parameter " + i + " in function " + id);
                default: throw new CompilationError("Expected " + refPar.possibleValues() + " for parameter " + i + " in function " + id);
            }
        }
        return new Function(ref, pars);
    }
    
}
