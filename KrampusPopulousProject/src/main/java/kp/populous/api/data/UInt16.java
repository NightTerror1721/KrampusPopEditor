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
public final class UInt16 extends AbstractInt<UInt16>
{
    public static final UInt16 ZERO = new UInt16(0);
    public static final UInt16 ONE = new UInt16(1);
    
    public static final UInt16 MAX = new UInt16(0xffff);
    public static final UInt16 MIN = ZERO;
    
    public UInt16(int value) { super(StrictMath.abs(value) % 0xffff); }

    @Override
    public UInt16 changeBitState(int index, boolean state) { return new UInt16(bitstate(value, index, state)); }

    @Override
    public boolean equals(UInt16 o) { return value == o.value; }

    @Override
    public int compareTo(UInt16 o) { return Integer.compare(value, o.value); }
    
    public static final UInt16 valueOf(byte value) { return new UInt16(value); }
    public static final UInt16 valueOf(short value) { return new UInt16(value); }
    public static final UInt16 valueOf(int value) { return new UInt16(value); }
    public static final UInt16 valueOf(long value) { return new UInt16((int) value); }
    
    public static final UInt16 valueOf(Data data) { return new UInt16(data.toInt()); }
    
    public static final void write(byte[] buffer, int offset, UInt16 value) { IOUtils.writeUnsignedInt16(buffer, offset, value.value); }
    public static final UInt16 read(byte[] buffer, int offset) { return new UInt16(IOUtils.readUnsignedInt16(buffer, offset)); }
    
    public static final void writeB(byte[] buffer, int offset, UInt16 value) { IOUtils.writeUnsignedInt16B(buffer, offset, value.value); }
    public static final UInt16 readB(byte[] buffer, int offset) { return new UInt16(IOUtils.readUnsignedInt16B(buffer, offset)); }
    
    public static final void write(OutputStream os, UInt16 value) throws IOException { IOUtils.writeUnsignedInt16(os, value.value); }
    public static final UInt16 read(InputStream is) throws IOException { return new UInt16(IOUtils.readUnsignedInt16(is)); }
    
    public static final void writeB(OutputStream os, UInt16 value) throws IOException { IOUtils.writeUnsignedInt16B(os, value.value); }
    public static final UInt16 readB(InputStream is) throws IOException { return new UInt16(IOUtils.readUnsignedInt16B(is)); }
}
