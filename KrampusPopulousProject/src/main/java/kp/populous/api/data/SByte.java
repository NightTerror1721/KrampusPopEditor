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
public final class SByte extends Data<SByte>
{
    public static final SByte MINUSONE = new SByte((byte) -1);
    public static final SByte ZERO = new SByte((byte) 0);
    public static final SByte ONE = new SByte((byte) 1);
    
    public static final SByte MAX = new SByte((byte) 0x7f);
    public static final SByte MIN = new SByte((byte) 0x80);
    
    private final byte value;
    
    public SByte(byte value) { this.value = value; }

    @Override
    public byte toByte() { return value; }

    @Override
    public short toShort() { return value; }

    @Override
    public int toInt() { return value; }

    @Override
    public long toLong() { return value; }

    @Override
    public SByte changeBitState(int index, boolean state) { return new SByte(bitstate(value, index, state)); }

    @Override
    public boolean getBitState(int index) { return bitstate(value, index); }

    @Override
    public boolean equals(SByte o) { return value == o.value; }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + this.value;
        return hash;
    }

    @Override
    public int compareTo(SByte o) { return Byte.compare(value, o.value); }
    
    @Override
    public String toString() { return Byte.toString(value); }
    
    public static final SByte valueOf(byte value) { return new SByte(value); }
    public static final SByte valueOf(short value) { return new SByte((byte) value); }
    public static final SByte valueOf(int value) { return new SByte((byte) value); }
    public static final SByte valueOf(long value) { return new SByte((byte) value); }
    
    public static final SByte valueOf(Data data) { return new SByte(data.toByte()); }
    
    
    public static final void write(byte[] buffer, int offset, SByte value) { IOUtils.writeSignedByte(buffer, offset, value.value); }
    public static final SByte read(byte[] buffer, int offset) { return new SByte(IOUtils.readSignedByte(buffer, offset)); }
    
    public static final void write(OutputStream os, SByte value) throws IOException { IOUtils.writeSignedByte(os, value.value); }
    public static final SByte read(InputStream is) throws IOException { return new SByte(IOUtils.readSignedByte(is)); }
}
