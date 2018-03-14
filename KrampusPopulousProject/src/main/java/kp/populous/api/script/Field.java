/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.Objects;
import kp.populous.api.data.SInt32;
import kp.populous.api.data.UInt32;
import kp.populous.api.script.ScriptConstant.FieldType;

/**
 *
 * @author Asus
 */
public final class Field
{
    private static final SInt32 INVALID_VALUE = SInt32.valueOf(3);
    public static final Field INVALID = new Field(FieldType.INVALID, INVALID_VALUE);
    
    private final FieldType type;
    private final SInt32 value;
    
    private Field(FieldType type, SInt32 value)
    {
        this.type = Objects.requireNonNull(type);
        this.value = Objects.requireNonNull(value);
    }
    
    public static final Field constant(SInt32 value) { return new Field(FieldType.CONSTANT, value); }
    public static final Field user(SInt32 index) { return new Field(FieldType.USER, index); }
    public static final Field internal(SInt32 index) { return new Field(FieldType.INTERNAL, index); }
    
    public final FieldType getFieldType() { return type; }
    public final boolean isConstant() { return type == FieldType.CONSTANT; }
    public final boolean isUser() { return type == FieldType.USER; }
    public final boolean isInternal() { return type == FieldType.INTERNAL; }
    
    public final SInt32 getIndex() { return this.value; }
    public final SInt32 getValue() { return this.value; }
    
    public final boolean isInvalid() { return type == FieldType.INVALID; }
    
    public static final Field read(byte[] buffer, int offset)
    {
        FieldType type = FieldType.decode(UInt32.read(buffer, offset));
        if(type == FieldType.INVALID)
            return INVALID;
        SInt32 value = SInt32.read(buffer, offset + 4);
        return new Field(type, value);
    }
    
    public static final void write(byte[] buffer, int offset, Field field)
    {
        if(field == null)
            field = INVALID;
        UInt32.write(buffer, offset, field.type.getCode());
        SInt32.write(buffer, offset + 4, field.value);
    }
}
