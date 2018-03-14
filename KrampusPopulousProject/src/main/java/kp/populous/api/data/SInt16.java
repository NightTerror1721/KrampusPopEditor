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
public final class SInt16 extends AbstractShort<SInt16>
{
    public static final SInt16 MINUSONE = new SInt16((short) -1);
    public static final SInt16 ZERO = new SInt16((short) 0);
    public static final SInt16 ONE = new SInt16((short) 1);
    
    public static final SInt16 MAX = new SInt16((short) 0x7fff);
    public static final SInt16 MIN = new SInt16((short) 0x8000);
    
    public SInt16(short value) { super(value); }

    @Override
    public SInt16 changeBitState(int index, boolean state) { return new SInt16(bitstate(value, index, state)); }

    @Override
    public boolean equals(SInt16 o) { return value == o.value; }

    @Override
    public int compareTo(SInt16 o) { return Short.compare(value, o.value); }
    
    public static final SInt16 valueOf(byte value) { return new SInt16(value); }
    public static final SInt16 valueOf(short value) { return new SInt16(value); }
    public static final SInt16 valueOf(int value) { return new SInt16((short) value); }
    public static final SInt16 valueOf(long value) { return new SInt16((short) value); }
    
    public static final SInt16 valueOf(Data data) { return new SInt16(data.toShort()); }
    
    public static final void write(byte[] buffer, int offset, SInt16 value) { IOUtils.writeSignedInt16(buffer, offset, value.value); }
    public static final SInt16 read(byte[] buffer, int offset) { return new SInt16(IOUtils.readSignedInt16(buffer, offset)); }
    
    public static final void writeB(byte[] buffer, int offset, SInt16 value) { IOUtils.writeSignedInt16B(buffer, offset, value.value); }
    public static final SInt16 readB(byte[] buffer, int offset) { return new SInt16(IOUtils.readSignedInt16B(buffer, offset)); }
    
    public static final void write(OutputStream os, SInt16 value) throws IOException { IOUtils.writeSignedInt16(os, value.value); }
    public static final SInt16 read(InputStream is) throws IOException { return new SInt16(IOUtils.readSignedInt16(is)); }
    
    public static final void writeB(OutputStream os, SInt16 value) throws IOException { IOUtils.writeSignedInt16B(os, value.value); }
    public static final SInt16 readB(InputStream is) throws IOException { return new SInt16(IOUtils.readSignedInt16B(is)); }
}
