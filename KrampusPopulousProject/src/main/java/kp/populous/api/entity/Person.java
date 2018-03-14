/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.entity;

import java.util.Set;
import kp.populous.api.Tribe;
import kp.populous.api.data.UByte;

/**
 *
 * @author Asus
 */
public class Person extends Entity
{
    @EntityModelId
    public static final UByte
            MODEL_WILDMAN = UByte.valueOf(1),
            MODEL_BRAVE = UByte.valueOf(2),
            MODEL_WARRIOR = UByte.valueOf(3),
            MODEL_PREACHER = UByte.valueOf(4),
            MODEL_SPY = UByte.valueOf(5),
            MODEL_FIREWARRIOR = UByte.valueOf(6),
            MODEL_SHAMAN = UByte.valueOf(7),
            MODEL_ANGEL_OF_DEAD = UByte.valueOf(8);
    private static final Set<UByte> CHECKER = collectModelIds(Person.class);
    
    Person(UByte model) throws InvalidEntity
    {
        super(EntityType.PERSON, model);
        if(!CHECKER.contains(model))
            throw new InvalidEntity();
    }
    @Override
    public final boolean isValidTribeForEntity(Tribe tribe) { return tribe.isColorTribe() || tribe == Tribe.NEUTRAL; }

    @Override
    final Tribe defaultTribe() { return Tribe.BLUE; }

    @Override
    protected void serialize(RawEntity raw) {}

    @Override
    protected void unserialize(RawEntity raw) {}
    
}
