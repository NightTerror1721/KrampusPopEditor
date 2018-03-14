/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import kp.populous.api.data.SByte;
import kp.populous.api.data.UByte;

/**
 *
 * @author Asus
 */
public final class EntityCoords
{
    private static final UByte MAX = UByte.valueOf(254);
    public static final EntityCoords ZERO = new EntityCoords(UByte.ZERO, UByte.ZERO);
    
    private final UByte x;
    private final UByte y;
    
    public EntityCoords(UByte x, UByte y)
    {
        this.x = check(x);
        this.y = check(y);
    }
    public EntityCoords(int x, int y) { this(UByte.valueOf(x), UByte.valueOf(y)); }
    
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
    
    public static final void write(byte[] buffer, int offset, EntityCoords coords)
    {
        UByte.write(buffer, offset, coords.x);
        UByte.write(buffer, offset + 1, coords.y);
    }
    public static final EntityCoords read(byte[] buffer, int offset)
    {
        return new EntityCoords(UByte.read(buffer, offset), UByte.read(buffer, offset + 1));
    }
    
    public static final void write(OutputStream os, EntityCoords coords) throws IOException
    {
        UByte.write(os, coords.x);
        UByte.write(os, coords.y);
    }
    public static final EntityCoords read(InputStream is) throws IOException
    {
        return new EntityCoords(UByte.read(is), UByte.read(is));
    }
}
