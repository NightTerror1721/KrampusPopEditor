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
public class Raw
{
    protected final byte[] data;

    public Raw(int size) { data = new byte[size]; }
    
    public final int length() { return data.length; }

    public final SByte signedByte(int index) { return SByte.read(data, index); }
    public final SInt16 signedInt16(int index) { return SInt16.read(data, index); }
    public final SInt32 signedInt32(int index) { return SInt32.read(data, index); }
    public final SInt64 signedInt64(int index) { return SInt64.read(data, index); }
    public final UByte unsignedByte(int index) { return UByte.read(data, index); }
    public final UInt16 unsignedInt16(int index) { return UInt16.read(data, index); }
    public final UInt32 unsignedInt32(int index) { return UInt32.read(data, index); }

    public final void signedByte(int index, SByte value) { SByte.write(data, index, value); }
    public final void signedInt16(int index, SInt16 value) { SInt16.write(data, index, value); }
    public final void signedInt32(int index, SInt32 value) { SInt32.write(data, index, value); }
    public final void signedInt64(int index, SInt64 value) { SInt64.write(data, index, value); }
    public final void unsignedByte(int index, UByte value) { UByte.write(data, index, value); }
    public final void unsignedInt16(int index, UInt16 value) { UInt16.write(data, index, value); }
    public final void unsignedInt32(int index, UInt32 value) { UInt32.write(data, index, value); }
    
    public final void read(InputStream is, int off, int len) throws IOException { IOUtils.readFully(is, data, off, len); }
    public final void read(InputStream is) throws IOException { IOUtils.readFully(is, data); }
    
    public final void write(OutputStream os, int off, int len) throws IOException { os.write(data, off, len); }
    public final void write(OutputStream os) throws IOException { os.write(data); }
}
