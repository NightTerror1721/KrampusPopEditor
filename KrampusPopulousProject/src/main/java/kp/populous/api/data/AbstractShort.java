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
abstract class AbstractShort<D extends AbstractShort<D>> extends Data<D>
{
    final short value;
    
    AbstractShort(short value) { this.value = value; }

    @Override
    public final byte toByte() { return (byte) value; }

    @Override
    public final short toShort() { return value; }

    @Override
    public final int toInt() { return value; }

    @Override
    public final long toLong() { return value; }

    @Override
    public final boolean getBitState(int index) { return bitstate(value, index); }

    @Override
    public final int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + this.value;
        return hash;
    }
    
    @Override
    public final String toString() { return Short.toString(value); }
}
