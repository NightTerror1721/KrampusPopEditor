/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.entity;

import kp.populous.api.data.UByte;

/**
 *
 * @author Asus
 */
public enum EntityType
{
    PERSON(1),
    BUILDING(2),
    CREATURE(3),
    VEHICLE(4),
    SCENERY(5),
    GENERAL(6),
    EFFECT(7),
    SHOT(8),
    SHAPE(9),
    INTERNAL(10),
    SPELL(11);
    
    private final UByte id;
    
    private EntityType(int id)
    {
        this.id = UByte.valueOf(id);
    }
    
    public final UByte getId() { return id; }
    
    public static final EntityType decode(int code)
    {
        switch(code)
        {
            case 1: return PERSON;
            case 2: return BUILDING;
            case 3: return CREATURE;
            case 4: return VEHICLE;
            case 5: return SCENERY;
            case 6: return GENERAL;
            case 7: return EFFECT;
            case 8: return SHOT;
            case 9: return SHAPE;
            case 10: return INTERNAL;
            case 11: return SPELL;
            default: return null;
        }
    }
    public static final EntityType decode(UByte code) { return decode(code.toInt()); }
}
