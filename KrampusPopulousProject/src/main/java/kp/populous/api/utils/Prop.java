/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Proplates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Marc
 */
public final class Prop
{
    private static final File FILE = new File("config.json");
    private static JSONObject PROPS;
    
    private static JSONObject props()
    {
        if(PROPS == null)
        {
            if(!FILE.exists() || !FILE.isFile())
            {
                PROPS = new JSONObject();
                store();
            }
            else try { PROPS = load(); }
            catch(IOException ex) { PROPS = new JSONObject(); store(); }
        }
        return PROPS;
    }
    
    private static String[] splitPath(String name)
    {
        String[] parts = name.split("\\.", 2);
        return parts.length < 2
                ? new String[] { "root", parts[0] }
                : parts;
    }
    
    private static JSONObject load() throws IOException, JSONException
    {
        JSONObject json;
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FILE)))
        {
            json = new JSONObject(new JSONTokener(bis));
        }
        return json;
    }
    
    public static final void store()
    {
        try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(FILE)))
        {
            PROPS.write(osw, 4, 0);
        }
        catch(IOException ex) { ex.printStackTrace(System.err); }
    }
    
    private static JSONObject prop(String[] path)
    {
        if(path.length == 1)
            return props();
        return prop(props(), path, 0, false);
    }
    
    private static JSONObject prop(JSONObject base, String[] path, int index, boolean store)
    {
        JSONObject child = base.optJSONObject(path[index]);
        if(child == null)
        {
            child = new JSONObject();
            base.put(path[index], child);
            store = true;
        }
        if(index + 2 >= path.length)
        {
            if(store)
                store();
            return child;
        }
        return prop(child, path, index + 1, store);
    }
    
    private static void set(String[] path, Object value) { prop(path).put(path[path.length - 1], value); }
    private static void set(String[] path, Collection<?> value) { prop(path).put(path[path.length - 1], value); }
    private static void set(String[] path, Map<?, ?> value) { prop(path).put(path[path.length - 1], value); }
    private static <T> T get(String[] path, T defaultValue, Type type)
    {
        JSONObject base = prop(path);
        T value;
        switch(type)
        {
            default: throw new IllegalStateException();
            case STRING: value = (T) base.optString(path[path.length - 1]); break;
            case INT: value = (T) (Integer) base.optInt(path[path.length - 1]); break;
            case LONG: value = (T) (Long) base.optLong(path[path.length - 1]); break;
            case FLOAT: value = (T) (Float) base.optFloat(path[path.length - 1]); break;
            case DOUBLE: value = (T) (Double) base.optDouble(path[path.length - 1]); break;
            case BOOLEAN: value = (T) (Boolean) base.optBoolean(path[path.length - 1]); break;
            case LIST: {
                JSONArray temp = base.optJSONArray(path[path.length - 1]);
                value = temp == null ? defaultValue : (T) temp.toList();
            } break;
            case MAP: {
                JSONObject temp = base.optJSONObject(path[path.length - 1]);
                value = temp == null ? defaultValue : (T) temp.toMap();
            } break;
        }
        return value == null ? defaultValue : value;
    }
    private static <T> T get(String name, T defaultValue, Type type) { return get(splitPath(name), defaultValue, type); }
    
    
    public static final String getString(String name, String defaultValue) { return get(name, defaultValue, Type.STRING); }
    public static final String getString(String name) { return get(name, "", Type.STRING); }
    
    public static final boolean getBoolean(String name, boolean defaultValue) { return get(name, defaultValue, Type.BOOLEAN); }
    public static final boolean getBoolean(String name) { return get(name, false, Type.STRING); }
    
    public static final int getInt(String name, int defaultValue) { return get(name, defaultValue, Type.INT); }
    public static final int getInt(String name) { return get(name, 0, Type.INT); }
    
    public static final long getLong(String name, long defaultValue) { return get(name, defaultValue, Type.LONG); }
    public static final long getLong(String name) { return get(name, 0L, Type.LONG); }
    
    public static final float getFloat(String name, float defaultValue) { return get(name, defaultValue, Type.FLOAT); }
    public static final float getFloat(String name) { return get(name, 0f, Type.FLOAT); }
    
    public static final double getDouble(String name, double defaultValue) { return get(name, defaultValue, Type.DOUBLE); }
    public static final double getDouble(String name) { return get(name, 0D, Type.DOUBLE); }
    
    public static final List<Object> getList(String name, List<Object> defaultValue) { return get(name, defaultValue, Type.LIST); }
    public static final List<Object> getList(String name) { return get(name, Collections.emptyList(), Type.LIST); }
    
    public static final Map<String, Object> getMap(String name, Map<String, Object> defaultValue) { return get(name, defaultValue, Type.MAP); }
    public static final Map<String, Object> getMap(String name) { return get(name, Collections.emptyMap(), Type.MAP); }
    
    public static final List<String> getStringList(String name)
    {
        return get(name, Collections.emptyList(), Type.LIST)
                .stream().map(o -> o.toString()).collect(Collectors.toList());
    }
    
    
    private static void set0(String name, Object value)
    {
        set(splitPath(name), value);
        store();
    }
    
    public static final void set(String name, String value) { set0(name, value == null ? "" : value); }
    public static final void set(String name, int value) { set0(name, value); }
    public static final void set(String name, long value) { set0(name, value); }
    public static final void set(String name, float value) { set0(name, value); }
    public static final void set(String name, double value) { set0(name, value); }
    public static final void set(String name, boolean value) { set0(name, value); }
    public static final void set(String name, List<?> value)
    {
        set(splitPath(name), value == null ? Collections.emptyList() : value);
        store();
    }
    public static final void set(String name, Map<?, ?> value)
    {
        set(splitPath(name), value == null ? Collections.emptyMap() : value);
        store();
    }
    
    public static final void setAll(Consumer<PropBlockSetter> action)
    {
        action.accept(BLOCK);
        store();
    }
    
    
    public static final class PropBlockSetter
    {
        private PropBlockSetter() {}
        
        private void set0(String name, Object value) { Prop.set(splitPath(name), value); }
        
        public final void set(String name, String value) { set0(name, value == null ? "" : value); }
        public final void set(String name, int value) { set0(name, value); }
        public final void set(String name, long value) { set0(name, value); }
        public final void set(String name, float value) { set0(name, value); }
        public final void set(String name, double value) { set0(name, value); }
        public final void set(String name, boolean value) { set0(name, value); }
        public final void set(String name, List<?> value)
        {
            Prop.set(splitPath(name), value == null ? Collections.emptyList() : value);
        }
        public final void set(String name, Map<?, ?> value)
        {
            Prop.set(splitPath(name), value == null ? Collections.emptyMap() : value);
        }
    }
    private static final PropBlockSetter BLOCK = new PropBlockSetter();
    
    private enum Type { STRING, INT, LONG, FLOAT, DOUBLE, BOOLEAN, LIST, MAP }
}
