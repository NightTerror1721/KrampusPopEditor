/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import kp.populous.api.IOUtils;

/**
 *
 * @author Asus
 */
public final class UByte extends AbstractShort<UByte>
{
    public static final UByte ZERO = new UByte((short) 0);
    public static final UByte ONE = new UByte((short) 1);
    
    public static final UByte MAX = new UByte((short) 0xff);
    public static final UByte MIN = ZERO;
    
    public UByte(short value) { super((short) (StrictMath.abs(value) % 0xff)); }

    @Override
    public UByte changeBitState(int index, boolean state) { return new UByte(bitstate(value, index, state)); }

    @Override
    public boolean equals(UByte o) { return value == o.value; }

    @Override
    public int compareTo(UByte o) { return Short.compare(value, o.value); }
    
    public static final UByte valueOf(byte value) { return new UByte(value); }
    public static final UByte valueOf(short value) { return new UByte(value); }
    public static final UByte valueOf(int value) { return new UByte((short) value); }
    public static final UByte valueOf(long value) { return new UByte((short) value); }
    
    public static final UByte valueOf(Data data) { return new UByte(data.toShort()); }
    
    public static final void write(byte[] buffer, int offset, UByte value) { IOUtils.writeUnsignedByte(buffer, offset, value.value); }
    public static final UByte read(byte[] buffer, int offset) { return new UByte(IOUtils.readUnsignedByte(buffer, offset)); }
    
    public static final void write(OutputStream os, UByte value) throws IOException { IOUtils.writeUnsignedByte(os, value.value); }
    public static final UByte read(InputStream is) throws IOException { return new UByte(IOUtils.readUnsignedByte(is)); }
}
