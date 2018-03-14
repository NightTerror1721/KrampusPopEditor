/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 *
 * @author Asus
 */
public final class LandCoords
{
    private static final UByte MAX = UByte.valueOf(127);
    public static final LandCoords ZERO = new LandCoords(UByte.ZERO, UByte.ZERO);
    
    private final UByte x;
    private final UByte y;
    
    public LandCoords(UByte x, UByte y)
    {
        this.x = check(x);
        this.y = check(y);
    }
    public LandCoords(int x, int y) { this(UByte.valueOf(x), UByte.valueOf(y)); }
    
    public final UByte getX() { return x; }
    public final SByte getSignedX() { return SByte.valueOf(x); }
    
    public final UByte getY() { return y; }
    public final SByte getSignedY() { return SByte.valueOf(y); }
    
    private UByte check(UByte coord)
    {
        if(Objects.requireNonNull(coord).compareTo(MAX) > 0 ||
                coord.compareTo(UByte.ZERO) < 0)
            throw new IllegalArgumentException();
        return coord;
    }
    
    public static final void write(byte[] buffer, int offset, LandCoords coords)
    {
        UByte.write(buffer, offset, coords.x);
        UByte.write(buffer, offset + 1, coords.y);
    }
    public static final LandCoords read(byte[] buffer, int offset)
    {
        return new LandCoords(UByte.read(buffer, offset), UByte.read(buffer, offset + 1));
    }
    
    public static final void write(OutputStream os, LandCoords coords) throws IOException
    {
        UByte.write(os, coords.x);
        UByte.write(os, coords.y);
    }
    public static final LandCoords read(InputStream is) throws IOException
    {
        return new LandCoords(UByte.read(is), UByte.read(is));
    }
}
