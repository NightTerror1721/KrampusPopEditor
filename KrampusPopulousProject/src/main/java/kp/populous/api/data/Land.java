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
public final class Land
{
    public static final int MAX_HEIGHT = 1408;
    public static final int MIN_HEIGHT = 0;
    
    private static final int SIZE = 0x4000;
    private static final int ROWS = 0x80;
    private static final int COLUMNS = 0x80;
    
    private final byte[] heights = new byte[SIZE * 2];
    private final byte[] walk = new byte[SIZE];
    
    public final void setHeight(LandCoords coords, int height)
    {
        if(height < MIN_HEIGHT || height > MAX_HEIGHT)
            throw new IllegalArgumentException();
        IOUtils.writeSignedInt16(heights, id(coords), (short) height);
    }
    
    public final int getHeight(LandCoords coords)
    {
        return IOUtils.readSignedInt16(heights, id(coords));
    }
    
    public final void setEnabledWalk(LandCoords coords, boolean canWalk)
    {
        walk[id(coords)] = (byte) (canWalk ? 1 : 0);
    }
    
    public final boolean isEnabledWalk(LandCoords coords)
    {
        return walk[id(coords)] != 0;
    }
    
    private static int id(LandCoords coords)
    {
        return (coords.getY().toInt() * COLUMNS + coords.getX().toInt()) * 2;
    }
    
    public final void write(OutputStream os) throws IOException
    {
        os.write(heights);
        os.write(new byte[SIZE * 2]);
        os.write(walk);
        
    }
    public final void read(InputStream is) throws IOException
    {
        IOUtils.readFully(is, heights);
        IOUtils.skipFully(is, SIZE * 2);
        IOUtils.readFully(is, walk);
    }
}
