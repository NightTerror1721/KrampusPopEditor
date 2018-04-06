/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.util.Objects;


public final class Pair<L, R>
{
    public final L left;
    public final R right;
    
    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public final boolean equals(Object o)
    {
        if(o == this)
            return true;
        if(o instanceof Pair)
        {
            Pair p = (Pair) o;
            return left.equals(p.left) && right.equals(p.right);
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.left);
        hash = 43 * hash + Objects.hashCode(this.right);
        return hash;
    }
    
    @Override
    public final String toString() { return "<" + left + ", " + right + ">"; }
}
