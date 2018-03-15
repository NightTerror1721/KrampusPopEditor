/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.autocomplete.VariableCompletion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Asus
 */
public final class CompletionProviderLoader
{
    private final DefaultCompletionProvider provider;
    private final LinkedList<BasicCompletion> others = new LinkedList<>();
    private final LinkedList<VariableCompletion> consts = new LinkedList<>();
    private final LinkedList<FunctionCompletion> funcs = new LinkedList<>();
    
    public CompletionProviderLoader(DefaultCompletionProvider provider)
    {
        this.provider = Objects.requireNonNull(provider);
        initProvider();
    }
    
    private void initProvider()
    {
        provider.setParameterizedCompletionParams('(', ", ", ')');
    }
    
    
    public final void loadExtern(InputStream is)
    {
        JSONObject base = load(is);
        loadFunctions(base.optJSONArray("functions"));
        loadConstants(base.optJSONArray("constants"));
        loadOthers(base.optJSONArray("others"));
    }
    
    private void loadFunctions(JSONArray base)
    {
        each(base, jfunc -> {
            String name = prop(jfunc, "name");
            if(name.isEmpty())
                return;
            String desc = prop(jfunc, "desc");
            List<Parameter> pars = loadParameters(jfunc.optJSONArray("params"));
            
            FunctionCompletion fc = new FunctionCompletion(provider, name, null);
            fc.setParams(pars);
            if(!desc.isEmpty())
                fc.setShortDescription(desc);
            funcs.add(fc);
            provider.addCompletion(fc);
        });
    }
    
    private List<Parameter> loadParameters(JSONArray base)
    {
        if(base == null)
            return Collections.emptyList();
        
        LinkedList<Parameter> pars = new LinkedList<>();
        int len = base.length();
        for(int i=0;i<len;i++)
        {
            JSONObject jpar = base.optJSONObject(i);
            if(jpar == null)
                continue;
            
            String name = prop(jpar, "name");
            if(name.isEmpty())
                continue;
            String desc = prop(jpar, "desc");
            
            Parameter par = new Parameter(null, name, (i + 1 >= len));
            if(!desc.isEmpty())
                par.setDescription(desc);
            pars.add(par);
        }
        return pars;
    }
    
    private void loadConstants(JSONArray base)
    {
        each(base, jconst -> {
            String name = prop(jconst, "name");
            if(name.isEmpty())
                return;
            String type = jconst.optString("type");
            String desc = prop(jconst, "desc");
            
            VariableCompletion vc = new VariableCompletion(provider, name, type);
            if(!desc.isEmpty())
                vc.setShortDescription(desc);
            vc.setRelevance(proprel(jconst));
            consts.add(vc);
            provider.addCompletion(vc);
        });
    }
    
    private void loadOthers(JSONArray base)
    {
        each(base, jother -> {
            String name = prop(jother, "name");
            if(name.isEmpty())
                return;
            String desc = prop(jother, "desc");
            
            BasicCompletion bc = new BasicCompletion(provider, name);
            if(!desc.isEmpty())
                bc.setSummary(desc);
            bc.setRelevance(proprel(jother));
            others.add(bc);
            provider.addCompletion(bc);
        });
    }
    
    
    private static void each(JSONArray array, Consumer<JSONObject> action)
    {
        if(array == null)
            return;
        int len = array.length();
        for(int i=0;i<len;i++)
        {
            JSONObject obj = array.optJSONObject(i);
            if(obj != null)
                action.accept(obj);
        }
    }
    
    private static String prop(JSONObject json, String name)
    {
        Object value = json.opt(name);
        return value == null ? "" : value.toString();
    }
    
    private static int proprel(JSONObject json)
    {
        return json.optInt("relevance");
    }
    
    private static JSONObject load(InputStream is)
    {
        try { return new JSONObject(new JSONTokener(is)); }
        catch(JSONException ex) { ex.printStackTrace(System.err); return new JSONObject(); }
    }
}
