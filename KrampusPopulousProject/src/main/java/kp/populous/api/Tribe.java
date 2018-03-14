/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api;

import kp.populous.api.data.SByte;

/**
 *
 * @author Asus
 */
public enum Tribe
{
    BLUE(1),
    RED(2),
    YELLOW(3),
    GREEN(4),
    NEUTRAL(0),
    NONE(255);
    
    private final SByte id;
    
    private Tribe(int id)
    {
        this.id = SByte.valueOf(id);
    }
    
    public final SByte getId() { return id; }
    
    public final boolean isColorTribe()
    {
        switch(this)
        {
            case BLUE:
            case RED:
            case YELLOW:
            case GREEN:
                return true;
            default: return false;
        }
    }
    
    public static final Tribe decode(int code)
    {
        switch(code)
        {
            case 0: return NEUTRAL;
            case 1: return BLUE;
            case 2: return RED;
            case 3: return YELLOW;
            case 4: return GREEN;
            default: return NONE;
        }
    }
    public static final Tribe decode(SByte code) { return decode(code.toInt()); }
}
