/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.entity;

import kp.populous.api.data.UByte;
import kp.populous.api.entity.Entity.InvalidEntity;

/**
 *
 * @author Asus
 */
public final class EntityFactory
{
    public static final Entity create(EntityType type, UByte model) throws InvalidEntity
    {
        switch(type)
        {
            default: throw new InvalidEntity();
            case PERSON: return new Person(model);
        }
    }
}
