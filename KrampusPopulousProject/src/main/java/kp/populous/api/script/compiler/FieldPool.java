/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import kp.populous.api.data.SInt32;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.Field;
import kp.populous.api.script.Script;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.compiler.FieldStack.StackValue;
import kp.populous.api.script.compiler.parser.Constant;
import kp.populous.api.script.compiler.parser.Internal;
import kp.populous.api.script.compiler.parser.SpecialToken;
import kp.populous.api.script.compiler.parser.Variable;
import kp.populous.api.utils.Pair;

/**
 *
 * @author Asus
 */
public final class FieldPool
{
    private final FieldAllocator fields = new FieldAllocator();
    private final VariableAllocator varAllocator = new VariableAllocator();
    private final FieldStack stack = new FieldStack(this, varAllocator);
    private final HashMap<Integer, UInt16> constants = new HashMap<>();
    private final HashMap<ScriptConstant.Internal, UInt16> internals = new HashMap<>();
    private final HashMap<String, UInt16> variables = new HashMap<>();

    public final UInt16 registerVariable(Variable token) throws CompilationError
    {
        String name = token.getName();
        if(variables.containsKey(name))
            return variables.get(name);
        UInt16 index = varAllocator.allocate();
        variables.put(name, index);
        return index;
    }

    public final UInt16 registerConstant(Constant token) throws CompilationError
    {
        Integer value = token.getValue();
        if(constants.containsKey(value))
            return constants.get(value);
        Pair<Field, UInt16> field = fields.allocateConstant(SInt32.valueOf(value));
        constants.put(value, field.right);
        return field.right;
    }

    public final UInt16 registerInternal(Internal token) throws CompilationError
    {
        ScriptConstant.Internal in = token.getValue();
        if(internals.containsKey(in))
            return internals.get(in);
        Pair<Field, UInt16> field = fields.allocateInternal(SInt32.valueOf(in.getCode()));
        internals.put(in, field.right);
        return field.right;
    }
    
    /* Stack */
    //public final FieldStack getStack() { return stack; }
    
    public final StackValue pushVariable(Variable variable) throws CompilationError { return stack.pushVariable(variable); }
    public final StackValue pushConstant(Constant constant) throws CompilationError { return stack.pushConstant(constant); }
    public final StackValue pushInternal(Internal internal) throws CompilationError { return stack.pushInternal(internal); }
    public final StackValue pushSpecialToken(SpecialToken token) throws CompilationError { return stack.pushSpecialToken(token); }
    public final StackValue pushVolatile() throws CompilationError { return stack.pushVolatile(); }
    public final StackValue pushStackValue(StackValue value) throws CompilationError { return stack.pushStackValue(value); }
    
    public final StackValue peek() throws CompilationError { return stack.peek(); }
    
    public final StackValue pop() throws CompilationError { return stack.pop(); }
    
    /* End stack */

    public final void fillScriptFields(Script script) { fields.fillScriptFields(script); }
    
    private static final class FieldAllocator
    {
        private final Pair<Field, UInt16>[] fields = new Pair[ScriptConstant.MAX_FIELDS];
        private int size = 0;
        
        public final Pair<Field, UInt16> allocateConstant(SInt32 value) throws CompilationError
        {
            if(size >= fields.length)
                throw new CompilationError("Field count overflow");
            Field field = Field.constant(value);
            UInt16 index = UInt16.valueOf(size);
            return fields[size++] = new Pair<>(field, index);
        }
        
        public final Pair<Field, UInt16> allocateInternal(SInt32 value) throws CompilationError
        {
            if(size >= fields.length)
                throw new CompilationError("Field count overflow");
            Field field = Field.internal(value);
            UInt16 index = UInt16.valueOf(size);
            return fields[size++] = new Pair<>(field, index);
        }
        
        public final Pair<Field, UInt16> allocateVariable(SInt32 value) throws CompilationError
        {
            if(size >= fields.length)
                throw new CompilationError("Field count overflow");
            Field field = Field.user(value);
            UInt16 index = UInt16.valueOf(size);
            return fields[size++] = new Pair<>(field, index);
        }
        
        public final void fillScriptFields(Script script)
        {
            int count = 0;
            for(Pair<Field, UInt16> pair : fields)
            {
                if(pair != null && pair.right.toInt() != count)
                    throw new IllegalStateException();
                script.fields.setField(count++, pair == null ? Field.INVALID : pair.left);
            }
        }
    }
    
    final class VariableAllocator
    {
        private final HashSet<UInt16> allocated = new HashSet<>();
        
        private final LinkedList<UInt16> deleted = new LinkedList<>();
        private final HashSet<UInt16> deletedSet = new HashSet<>();
        
        private final LinkedList<UInt16> stack = new LinkedList<>();
        
        public final boolean hasAllocated(UInt16 index)
        {
            return allocated.contains(index);
        }
        
        public final UInt16 allocate() throws CompilationError
        {
            if(deleted.isEmpty())
            {
                if(allocated.size() >= ScriptConstant.MAX_VARS)
                    throw new CompilationError("Variable count overflow");
                Pair<Field, UInt16> field = fields.allocateVariable(SInt32.valueOf(allocated.size()));
                allocated.add(field.right);
                return field.right;
            }
            UInt16 index = deleted.removeFirst();
            deletedSet.remove(index);
            return index;
        }
        
        public final void deallocate(UInt16 index)
        {
            if(!allocated.contains(index))
                throw new IllegalStateException();
            if(deletedSet.contains(index))
                throw new IllegalStateException();
            deleted.add(index);
            deletedSet.add(index);
        }
    }
}
