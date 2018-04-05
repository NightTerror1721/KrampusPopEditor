/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import kp.populous.api.data.SInt32;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.Field;
import kp.populous.api.script.Script;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.compiler.parser.Constant;
import kp.populous.api.script.compiler.parser.Internal;
import kp.populous.api.script.compiler.parser.Variable;

/**
 *
 * @author Asus
 */
public final class FieldPool
{
    private final HashMap<Integer, UInt16> constants = new HashMap<>();
    private final HashMap<ScriptConstant.Internal, UInt16> internals = new HashMap<>();
    private final HashMap<String, UInt16> variables = new HashMap<>();
    private final LinkedList<Field> list = new LinkedList<>();
    private UInt16 returnIndex;

    private void checkSize() throws CompilationError
    {
        if(list.size() >= ScriptConstant.MAX_FIELDS)
            throw new CompilationError("Field count overflow");
    }

    public final UInt16 registerVariable(Variable token) throws CompilationError
    {
        String name = token.getName();
        if(variables.containsKey(name))
            return variables.get(name);
        checkSize();
        if(variables.size() >= ScriptConstant.MAX_VARS)
            throw new CompilationError("Variable count overflow");
        Field field = Field.user(SInt32.valueOf(variables.size()));
        UInt16 index = UInt16.valueOf(list.size());
        list.add(field);
        variables.put(name, index);
        return index;
    }

    public final UInt16 registerConstant(Constant token) throws CompilationError
    {
        Integer value = token.getValue();
        if(constants.containsKey(value))
            return constants.get(value);
        checkSize();
        Field field = Field.constant(SInt32.valueOf(value));
        UInt16 index = UInt16.valueOf(list.size());
        list.add(field);
        constants.put(value, index);
        return index;
    }

    public final UInt16 registerInternal(Internal token) throws CompilationError
    {
        ScriptConstant.Internal in = token.getValue();
        if(internals.containsKey(in))
            return internals.get(in);
        checkSize();
        Field field = Field.internal(SInt32.valueOf(in.getCode()));
        UInt16 index = UInt16.valueOf(list.size());
        list.add(field);
        internals.put(in, index);
        return index;
    }
    
    public final UInt16 getReturnIndex() throws CompilationError
    {
        if(returnIndex == null)
        {
            checkSize();
            if(variables.size() >= ScriptConstant.MAX_VARS)
                throw new CompilationError("Variable count overflow");
            Field field = Field.user(SInt32.valueOf(variables.size()));
            returnIndex = UInt16.valueOf(list.size());
            list.add(field);
        }
        return returnIndex;
    }

    public final void fillScriptFields(Script script)
    {
        int count = 0;
        for(Field field : list)
            script.fields.setField(count++, field);
        for(; count < ScriptConstant.MAX_FIELDS; count++)
            script.fields.setField(count, Field.INVALID);
    }
    
    
    private static final class MEM
    {
        //private 
    }
}
