/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.data;

/**
 *
 * @author Asus
 */
abstract class AbstractLong<D extends AbstractLong<D>> extends Data<D>
{
    final long value;
    
    AbstractLong(long value) { this.value = value; }

    @Override
    public final byte toByte() { return (byte) value; }

    @Override
    public final short toShort() { return (short) value; }

    @Override
    public final int toInt() { return (int) value; }

    @Override
    public final long toLong() { return value; }

    @Override
    public final boolean getBitState(int index) { return bitstate(value, index); }

    @Override
    public final int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + (int) (this.value ^ (this.value >>> 32));
        return hash;
    }
    
    @Override
    public final String toString() { return Long.toString(value); }
}
