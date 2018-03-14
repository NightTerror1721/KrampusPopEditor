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
public final class UInt32 extends AbstractLong<UInt32>
{
    public static final UInt32 ZERO = new UInt32(0L);
    public static final UInt32 ONE = new UInt32(1L);
    
    public static final UInt32 MAX = new UInt32(0xffffffffL);
    public static final UInt32 MIN = ZERO;
    
    public UInt32(long value) { super(StrictMath.abs(value) % 0xffffffffL); }

    @Override
    public UInt32 changeBitState(int index, boolean state) { return new UInt32(bitstate(value, index, state)); }

    @Override
    public boolean equals(UInt32 o) { return value == o.value; }

    @Override
    public int compareTo(UInt32 o) { return Long.compare(value, o.value); }
    
    public static final UInt32 valueOf(byte value) { return new UInt32(value); }
    public static final UInt32 valueOf(short value) { return new UInt32(value); }
    public static final UInt32 valueOf(int value) { return new UInt32(value); }
    public static final UInt32 valueOf(long value) { return new UInt32(value); }
    
    public static final UInt32 valueOf(Data data) { return new UInt32(data.toLong()); }
    
    public static final void write(byte[] buffer, int offset, UInt32 value) { IOUtils.writeUnsignedInt32(buffer, offset, value.value); }
    public static final UInt32 read(byte[] buffer, int offset) { return new UInt32(IOUtils.readUnsignedInt32(buffer, offset)); }
    
    public static final void writeB(byte[] buffer, int offset, UInt32 value) { IOUtils.writeUnsignedInt32B(buffer, offset, value.value); }
    public static final UInt32 readB(byte[] buffer, int offset) { return new UInt32(IOUtils.readUnsignedInt32B(buffer, offset)); }
    
    public static final void write(OutputStream os, UInt32 value) throws IOException { IOUtils.writeUnsignedInt32(os, value.value); }
    public static final UInt32 read(InputStream is) throws IOException { return new UInt32(IOUtils.readUnsignedInt32(is)); }
    
    public static final void writeB(OutputStream os, UInt32 value) throws IOException { IOUtils.writeUnsignedInt32B(os, value.value); }
    public static final UInt32 readB(InputStream is) throws IOException { return new UInt32(IOUtils.readUnsignedInt32B(is)); }
}
