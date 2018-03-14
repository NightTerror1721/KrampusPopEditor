/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import kp.populous.api.Tribe;
import kp.populous.api.data.Raw;
import kp.populous.api.data.SInt16;
import kp.populous.api.data.UByte;
import kp.populous.api.data.UInt16;

/**
 *
 * @author Asus
 */
public abstract class Entity
{
    private static final int SIZE = 0x37;
    
    UInt16 uiid;
    private final EntityType type;
    private final UByte model;
    private Tribe owner = defaultTribe();
    private EntityCoords position = EntityCoords.ZERO;
    
    protected Entity(EntityType type, UByte model)
    {
        this.type = Objects.requireNonNull(type);
        this.model = Objects.requireNonNull(model);
    }
    
    public final EntityType getEntityType() { return type; }
    public final UByte getEntityModel() { return model; }
    
    public final void setOwner(Tribe tribe)
    {
        if(tribe != null && isValidTribeForEntity(tribe))
            this.owner = tribe;
    }
    public final Tribe getOwner() { return owner; }
    public abstract boolean isValidTribeForEntity(Tribe tribe);
    abstract Tribe defaultTribe();
    
    public final void setPosition(UByte x, UByte y) { position = new EntityCoords(x, y); }
    public final void setPosition(EntityCoords coords) { position = Objects.requireNonNull(coords); }
    public final EntityCoords getPosition() { return position; }
    
    protected abstract void serialize(RawEntity raw);
    protected abstract void unserialize(RawEntity raw);
    
    public static final void write(OutputStream os, Entity entity) throws IOException
    {
        RawEntity raw = new RawEntity();
        entity.serialize(raw);
        raw.unsignedByte(0, entity.model);
        raw.unsignedByte(1, entity.type.getId());
        raw.signedByte(2, entity.owner.getId());
        raw.signedInt16(3, SInt16.valueOf(entity.position.getX()));
        raw.signedInt16(5, SInt16.valueOf(entity.position.getY()));
        raw.write(os);
    }
    
    public static final Entity read(InputStream is) throws IOException, InvalidEntity
    {
        RawEntity raw = new RawEntity();
        raw.read(is);
        UByte model = raw.unsignedByte(0);
        if(model.equals(UByte.ZERO))
            throw new InvalidEntity();
        EntityType type = EntityType.decode(raw.unsignedByte(1));
        if(type == null)
            throw new InvalidEntity();
        Entity e = EntityFactory.create(type, model);
        e.unserialize(raw);
        e.owner = Tribe.decode(raw.signedByte(2));
        UByte cx = UByte.valueOf(raw.signedInt16(3));
        UByte cy = UByte.valueOf(raw.signedInt16(5));
        e.position = new EntityCoords(cx, cy);
        return e;
    }
    
    static final <T> Set<T> collectModelIds(Class<? extends Entity> eClass)
    {
        HashSet<T> set = new HashSet<>();
        try
        {
            Field[] fields = eClass.getDeclaredFields();
            for(Field field : fields)
            {
                if(!Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(EntityModelId.class))
                    continue;
                T value = (T) field.get(null);
                set.add(value);
            }
        }
        catch(IllegalAccessException | IllegalArgumentException | SecurityException ex) {}
        return Collections.unmodifiableSet(set);
    }
    
    
    public static final class RawEntity extends Raw
    {
        private RawEntity() { super(SIZE); }
        
        public final void throwInvalid() throws InvalidEntity { throw new InvalidEntity(); }
    }
    
    public static final class InvalidEntity extends Exception
    {
        InvalidEntity() {}
    }
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    static @interface EntityModelId {}
}
