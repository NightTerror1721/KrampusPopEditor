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
public final class SInt64 extends AbstractLong<SInt64>
{
    public static final SInt64 MINUSONE = new SInt64(-1L);
    public static final SInt64 ZERO = new SInt64(0L);
    public static final SInt64 ONE = new SInt64(1L);
    
    public static final SInt64 MAX = new SInt64(0x7fffffffffffffffL);
    public static final SInt64 MIN = new SInt64(0x8000000000000000L);
    
    public SInt64(long value) { super(value); }

    @Override
    public SInt64 changeBitState(int index, boolean state) { return new SInt64(bitstate(value, index, state)); }

    @Override
    public boolean equals(SInt64 o) { return value == o.value; }

    @Override
    public int compareTo(SInt64 o) { return Long.compare(value, o.value); }
    
    public static final SInt64 valueOf(byte value) { return new SInt64(value); }
    public static final SInt64 valueOf(short value) { return new SInt64(value); }
    public static final SInt64 valueOf(int value) { return new SInt64(value); }
    public static final SInt64 valueOf(long value) { return new SInt64(value); }
    
    public static final SInt64 valueOf(Data data) { return new SInt64(data.toLong()); }
    
    public static final void write(byte[] buffer, int offset, SInt64 value) { IOUtils.writeSignedInt64(buffer, offset, value.value); }
    public static final SInt64 read(byte[] buffer, int offset) { return new SInt64(IOUtils.readSignedInt64(buffer, offset)); }
    
    public static final void writeB(byte[] buffer, int offset, SInt64 value) { IOUtils.writeSignedInt64B(buffer, offset, value.value); }
    public static final SInt64 readB(byte[] buffer, int offset) { return new SInt64(IOUtils.readSignedInt64B(buffer, offset)); }
    
    public static final void write(OutputStream os, SInt64 value) throws IOException { IOUtils.writeSignedInt64(os, value.value); }
    public static final SInt64 read(InputStream is) throws IOException { return new SInt64(IOUtils.readSignedInt64(is)); }
    
    public static final void writeB(OutputStream os, SInt64 value) throws IOException { IOUtils.writeSignedInt64B(os, value.value); }
    public static final SInt64 readB(InputStream is) throws IOException { return new SInt64(IOUtils.readSignedInt64B(is)); }
}
