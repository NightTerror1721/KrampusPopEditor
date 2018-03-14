/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api;

import kp.populous.api.utils.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import kp.populous.api.data.SByte;
import kp.populous.api.data.UByte;
import kp.populous.api.data.UInt32;
import kp.populous.api.entity.EntityCoords;

/**
 *
 * @author Asus
 */
public final class PopulousLevelHeader
{
    /* Spells Available */
    private UInt32 spells = UInt32.ZERO;
    
    /* Building Available */
    private UInt32 buildings = UInt32.ZERO;
    
    /* Building Avaliable - Level */
    private UInt32 levelBuildings = UInt32.ZERO;
    
    /* Building Available - Once */
    private UInt32 onceBuildings = UInt32.ZERO;
    
    /* Spells Available - Level - Not charging */
    private UInt32 levelSpells = UInt32.ZERO;
    
    /* Spells Available - Once */
    private final UByte[] onceSpells = Utils.fillArray(new UByte[32], UByte.ZERO);
    
    /* Vehicles Available */
    private UByte boat = UByte.ZERO;
    private UByte ballon = UByte.ZERO;
    
    /* Training Mana Off */
    private UByte trMana = UByte.ZERO;
    
    /* Flags - unnused */
    private static final UByte FLAGS = UByte.ZERO;
    
    /* Level Name */
    private final SByte[] levelName = Utils.fillArray(new SByte[32], SByte.ZERO);
    
    /* Number of Players */
    private UByte numPlayers = UByte.valueOf(2);
    
    /* Computer Script idx */
    private UByte redScript = UByte.ZERO;
    private UByte yellowScript = UByte.ZERO;
    private UByte greenScript = UByte.ZERO;
    
    /* Allies */
    private UByte[] allies = Utils.fillArray(new UByte[4], UByte.ZERO);
    
    /* Level Type - Color Scheme */
    private UByte colorScheme = UByte.ZERO;
    
    /* Object Bank - Tree Style */
    private UByte treeStyle = UByte.ZERO;
    
    /* Level Flags */
    private UByte levelFlags = UByte.ZERO;
    
    /* Bonus - Padding byte */
    private static final UByte BONUS = UByte.ZERO;
    
    /* Markers */
    private final UByte[] markers = Utils.fillArray(new UByte[510], UByte.ZERO);
    
    /* Camera Start Position */
    private UByte startX = UByte.ZERO;
    private UByte startY = UByte.ZERO;
    
    /* Camera Start Angle */
    private UByte angle = UByte.ZERO;
    
    public final void setAvailableSpell(Spell spell, boolean state)
    {
        spells = spells.changeBitState(spellbit(spell), state);
    }
    public final boolean isAvailableSpell(Spell spell) { return spells.getBitState(spellbit(spell)); }
    
    public final void setStartSpellCharging(Spell spell, boolean state)
    {
        levelSpells = levelSpells.changeBitState(spellbit(spell), state);
    }
    public final boolean isStartSpellCharging(Spell spell) { return levelSpells.getBitState(spellbit(spell)); }
    
    public final void setAvailableOnceSpell(Spell spell, int times)
    {
        onceSpells[spellbit(spell)] = UByte.valueOf(times);
    }
    public final int igetAvailableOnceSpell(Spell spell) { return onceSpells[spellbit(spell)].toInt(); }
    
    
    public final void setAvailableBuilding(Building building, boolean state)
    {
        buildings = buildings.changeBitState(buildingbit(building), state);
    }
    public final boolean isAvailableBuilding(Building building) { return buildings.getBitState(buildingbit(building)); }
    
    public final void setAvailableLevelBuilding(Building building, boolean state)
    {
        levelBuildings = levelBuildings.changeBitState(buildingbit(building), state);
    }
    public final boolean isAvailableLevelBuilding(Building building) { return levelBuildings.getBitState(buildingbit(building)); }
    
    public final void setAvailableOnceBuilding(Building building, boolean state)
    {
        onceBuildings = onceBuildings.changeBitState(buildingbit(building), state);
    }
    public final boolean isAvailableOnceBuilding(Building building) { return onceBuildings.getBitState(buildingbit(building)); }
    
    
    public final void setAvailableBoats(boolean state) { boat = state ? UByte.MAX : UByte.ZERO; }
    public final boolean isAvailableBoats() { return !boat.equals(UByte.ZERO); }
    
    public final void setAvailableBallons(boolean state) { ballon = state ? UByte.MAX : UByte.ZERO; }
    public final boolean isAvailableBallons() { return !ballon.equals(UByte.ZERO); }
    
    public final void setEnabledTrainingMana(boolean enabled) { trMana = enabled ? UByte.ZERO : UByte.ONE; }
    public final boolean isEnabledTrainingMana() { return trMana.equals(UByte.ZERO); }
    
    public final void setLevelName(String name)
    {
        byte[] bname = name.getBytes();
        int len = bname.length > levelName.length ? levelName.length : bname.length;
        for(int i=0;i<len;i++)
            levelName[i] = SByte.valueOf(bname[i]);
        if(len < levelName.length)
            for(int i=len;i<levelName.length;i++)
                levelName[i] = SByte.ZERO;
    }
    public final String getLevelName()
    {
        int len = 0;
        for(int i=0;i<levelName.length;i++)
            if(!levelName[i].equals(SByte.ZERO))
                len = i;
        byte[] bname = new byte[len];
        for(int i=0;i<len;i++)
            bname[i] = levelName[i].toByte();
        return new String(bname);
    }
    
    public final void setLastAvailablePlayer(Tribe tribe)
    {
        if(tribe == Tribe.BLUE)
            throw new IllegalArgumentException();
        numPlayers = UByte.valueOf(tribeid(tribe));
    }
    public final int getNumberPlayers() { return numPlayers.toInt(); }
    public final Tribe getLastAvailablePlayer() { return Tribe.values()[numPlayers.toInt() - 1]; }
    
    public final void setScriptIndex(Tribe tribe, int index)
    {   
        switch(tribe)
        {
            case RED: redScript = UByte.valueOf(index); break;
            case YELLOW: yellowScript = UByte.valueOf(index); break;
            case GREEN: greenScript = UByte.valueOf(index); break;
            default: throw new IllegalArgumentException();
        }
    }
    public final int getScriptIndex(Tribe tribe)
    {
        switch(tribe)
        {
            case RED: return redScript.toInt();
            case YELLOW: return yellowScript.toInt();
            case GREEN: return greenScript.toInt();
            default: throw new IllegalArgumentException();
        }
    }
    
    public final void setAllies(Tribe tribe0, Tribe tribe1, boolean allies)
    {
        this.allies[tribebit(tribe0)] = this.allies[tribebit(tribe0)].changeBitState(tribebit(tribe1), allies);
    }
    public final boolean areAllies(Tribe tribe0, Tribe tribe1)
    {
        return allies[tribebit(tribe0)].getBitState(tribebit(tribe1));
    }
    
    public final void setLevelScheme(int schemeIndex)
    {
        if(schemeIndex < 0 || schemeIndex > 0x23)
            throw new IllegalStateException();
        colorScheme = UByte.valueOf(schemeIndex);
    }
    public final int getLevelScheme() { return colorScheme.toInt(); }
    
    public final void setTreeStyle(int treeStyle)
    {
        if(treeStyle < 0 || treeStyle > 0x07)
            throw new IllegalStateException();
        this.treeStyle = UByte.valueOf(treeStyle);
    }
    public final int getTreeStyle() { return treeStyle.toInt(); }
    
    public final void setEnabledFogOfWar(boolean enabled) { levelFlags = levelFlags.changeBitState(0, enabled); }
    public final boolean isEnabledFogOfWar() { return levelFlags.getBitState(0); }
    
    public final void setEnabledGodMode(boolean enabled) { levelFlags = levelFlags.changeBitState(1, enabled); }
    public final boolean isEnabledGodMode() { return levelFlags.getBitState(1); }
    
    public final void setEnabledForce640x480(boolean enabled) { levelFlags = levelFlags.changeBitState(2, enabled); }
    public final boolean isEnabledForce640x480() { return levelFlags.getBitState(2); }
    
    public final void setEnabledForgeWorld(boolean enabled) { levelFlags = levelFlags.changeBitState(3, enabled); }
    public final boolean isEnabledForgeWorld() { return levelFlags.getBitState(3); }
    
    public final void setEnabledNoGuestSpells(boolean enabled) { levelFlags = levelFlags.changeBitState(4, enabled); }
    public final boolean isEnabledNoGuestSpells() { return levelFlags.getBitState(4); }
    
    public final void setMarkerPosition(UByte markerIndex, EntityCoords coords)
    {
        markers[markerIndex.toInt() * 2] = coords.getX();
        markers[markerIndex.toInt() * 2 + 1] = coords.getY();
    }
    public final void setMarkerPosition(int markerIndex, EntityCoords coords) { setMarkerPosition(UByte.valueOf(markerIndex), coords); }
    public final EntityCoords getMarkerPosition(UByte markerIndex)
    {
        return new EntityCoords(markers[markerIndex.toInt() * 2], markers[markerIndex.toInt() * 2 + 1]);
    }
    public final EntityCoords getMarkerPosition(int markerIndex) { return getMarkerPosition(UByte.valueOf(markerIndex)); }
    
    public final void setStartCameraPosition(EntityCoords coords)
    {
        startX = coords.getX();
        startY = coords.getY();
    }
    public final EntityCoords getStartCameraPosition() { return new EntityCoords(startX, startY); }
    
    public final void setStartAngle(UByte angle) { this.angle = Objects.requireNonNull(angle); }
    public final void setStartAngle(int angle) { this.angle = UByte.valueOf(angle); }
    public final UByte getStartAngle() { return angle; }
    
    
    public final void write(OutputStream output) throws IOException
    {
        BufferedOutputStream bos = new BufferedOutputStream(output);
        UInt32.write(bos, spells);
        UInt32.write(bos, buildings);
        UInt32.write(bos, levelBuildings);
        UInt32.write(bos, onceBuildings);
        UInt32.write(bos, levelSpells);
        for(UByte b : onceSpells)
            UByte.write(bos, b);
        UByte.write(bos, boat);
        UByte.write(bos, ballon);
        UByte.write(bos, trMana);
        UByte.write(bos, FLAGS);
        for(SByte b : levelName)
            SByte.write(bos, b);
        UByte.write(bos, numPlayers);
        UByte.write(bos, redScript);
        UByte.write(bos, yellowScript);
        UByte.write(bos, greenScript);
        for(UByte b : allies)
            UByte.write(bos, b);
        UByte.write(bos, colorScheme);
        UByte.write(bos, treeStyle);
        UByte.write(bos, levelFlags);
        UByte.write(bos, BONUS);
        for(UByte b : markers)
            UByte.write(bos, b);
        UByte.write(bos, startX);
        UByte.write(bos, startY);
        UByte.write(bos, angle);
        bos.flush();
    }
    public final void write(File file) throws IOException
    {
        try(FileOutputStream fos = new FileOutputStream(file)) { write(fos); }
    }
    
    public final void read(InputStream is) throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(is);
        spells = UInt32.read(bis);
        buildings = UInt32.read(bis);
        levelBuildings = UInt32.read(bis);
        onceBuildings = UInt32.read(bis);
        levelSpells = UInt32.read(bis);
        for(int i=0;i<onceSpells.length;i++)
            onceSpells[i] = UByte.read(bis);
        boat = UByte.read(bis);
        ballon = UByte.read(bis);
        trMana = UByte.read(bis);
        //FLAGS = UByte.read(bis);
        for(int i=0;i<levelName.length;i++)
            levelName[i] = SByte.read(bis);
        numPlayers = UByte.read(bis);
        redScript = UByte.read(bis);
        yellowScript = UByte.read(bis);
        greenScript = UByte.read(bis);
        for(int i=0;i<allies.length;i++)
            allies[i] = UByte.read(bis);
        colorScheme = UByte.read(bis);
        treeStyle = UByte.read(bis);
        levelFlags = UByte.read(bis);
        //BONUS = UByte.read(bis);
        for(int i=0;i<markers.length;i++)
            markers[i] = UByte.read(bis);
        startX = UByte.read(bis);
        startY = UByte.read(bis);
        angle = UByte.read(bis);
    }
    public final void read(File file) throws IOException
    {
        try(FileInputStream fis = new FileInputStream(file)) { read(fis); }
    }
    
    
    private static int spellbit(Spell spell)
    {
        switch(spell)
        {
            case BLAST: return 2;
            case LIGHTNING: return 3;
            case TORNADO: return 4;
            case SWARM: return 5;
            case INVISIBILITY: return 6;
            case HYPNOTISE: return 7;
            case FIRESTORM: return 8;
            case GHOST_ARMY: return 9;
            case ERODE: return 10;
            case SWAMP: return 11;
            case LANDBRIDGE: return 12;
            case ANGEL_OF_DEAD: return 13;
            case EARTHQUAKE: return 14;
            case FLATTEN: return 15;
            case VOLCANO: return 16;
            case CONVERT: return 17;
            case MAGICAL_SHIELD: return 19;
            default: throw new IllegalArgumentException();
        }
    }
    
    private static int buildingbit(Building building)
    {
        switch(building)
        {
            case HUT: return 1;
            case GUARD_TOWER: return 4;
            case TEMPLE: return 5;
            case SPY_HUT: return 6;
            case WARRIOR_HUT: return 7;
            case FIREWARRIOR_HUT: return 8;
            case BOAT_HUT: return 13;
            case BALLON_HUT: return 15;
            default: throw new IllegalArgumentException();
        }
    }
    
    private static int tribeid(Tribe tribe)
    {
        switch(tribe)
        {
            case BLUE:
            case RED:
            case YELLOW:
            case GREEN:
                return tribe.getId().toInt();
            default: throw new IllegalArgumentException();
        }
    }
    private static int tribebit(Tribe tribe)
    {
        switch(tribe)
        {
            case BLUE: return 0;
            case RED: return 1;
            case YELLOW: return 2;
            case GREEN: return 3;
            default: throw new IllegalArgumentException();
        }
    }
}
