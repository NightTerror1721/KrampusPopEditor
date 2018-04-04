/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import kp.populous.api.script.ScriptConstant;
import kp.populous.api.script.ScriptFunctions;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Marc
 */
public final class Utils
{
    private Utils() {}
    
    public static final String VERSION = "0.1.1";
    
    public static final String POP_SCRIPT_TEXT_TYPE = "text/popscript";
    
    private static Image LOGO;
    
    public static final <T> T[] fillArray(T[] array, T defaultValue)
    {
        for (int i = 0, len = array.length; i < len; i++)
            array[i] = defaultValue;
        return array;
    }
    
    public static final void initPopScriptLanguage()
    {
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping(POP_SCRIPT_TEXT_TYPE, "kp.populous.api.script.PopScriptMaker");
        
        FoldParserManager.get().addFoldParserMapping(POP_SCRIPT_TEXT_TYPE, new CurlyFoldParser(true, false));
    }
    
    public static final void useSystemLookAndFeel()
    {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch(ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) { ex.printStackTrace(System.err); }
    }
    
    public static void focus(Window frame)
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = frame.getSize();
        frame.setLocation((screen.width - window.width) / 2,
                        (screen.height - window.height) / 2);
    }
    
    public static void focus(JDialog dialog)
    {
        Container parent = dialog.getParent();
        if(!(parent instanceof JDialog) && !(parent instanceof JFrame))
        {
            focus((Window) dialog);
            return;
        }
        
        Point p = parent.getLocation();
        Dimension screen = parent.getSize();
        Dimension window = dialog.getSize();
        p.x += (screen.width - window.width) / 2;
        p.y += (screen.height - window.height) / 2;
        
        dialog.setLocation(p);
    }
    
    public static final void setIcon(JFrame frame)
    {
        if(LOGO == null)
        {
            try { LOGO = ImageIO.read(openInnerResource("/logo.png")); }
            catch(IOException ex) { ex.printStackTrace(System.err); }
        }
        if(LOGO != null)
            frame.setIconImage(LOGO);
    }
    
    public static final File getUserDirFile() { return new File(System.getProperty("user.dir")); }
    
    public static final InputStream openInnerResource(String path)
    {
        if(!path.startsWith("/"))
            path = "/" + path;
        return Utils.class.getResourceAsStream(path);
    }
    
    public static final URL getInnerResource(String path)
    {
        if(!path.startsWith("/"))
            path = "/" + path;
        return Utils.class.getResource(path);
    }
    
    public static final String getFileName(File file)
    {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index < 0 ? name : name.substring(0, index);
    }
    
    public static final String getFileExtension(File file)
    {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1);
    }
    
    
    private static JSONObject generateDefaultCompletionFunction(String name, String... params)
    {
        JSONArray jpars = new JSONArray();
        for(String param : params)
            jpars.put(new JSONObject().put("name", param).put("desc", ""));
        return new JSONObject().put("name", name).put("desc", "").put("relevance", 1).put("params", jpars);
    }
    public static final JSONObject generateDefaultCompletions()
    {
        JSONArray jconsts = ScriptConstant.generateJsonCompletions();
        JSONArray jfuncs = ScriptFunctions.generateFunctionsCompletionJson();
        JSONArray jothers = new JSONArray();
        
        jothers.put(new JSONObject().put("name", "if").put("desc", "").put("relevance", 1));
        jothers.put(new JSONObject().put("name", "else").put("desc", "").put("relevance", 1));
        jothers.put(new JSONObject().put("name", "every").put("desc", "").put("relevance", 1));
        
        jconsts.put(new JSONObject().put("name", "on").put("desc", "").put("relevance", 1));
        jconsts.put(new JSONObject().put("name", "off").put("desc", "").put("relevance", 1));
        
        jfuncs.put(generateDefaultCompletionFunction("set", "var", "value"));
        jfuncs.put(generateDefaultCompletionFunction("inc", "var", "value"));
        jfuncs.put(generateDefaultCompletionFunction("dec", "var", "value"));
        jfuncs.put(generateDefaultCompletionFunction("mul", "var", "operand1", "operand2"));
        jfuncs.put(generateDefaultCompletionFunction("div", "var", "operand1", "operand2"));
        
        return new JSONObject().put("functions", jfuncs).put("constants", jconsts).put("others", jothers);
    }
    
    public static final void printDefaultCompletions(File file)
    {
        JSONObject base = generateDefaultCompletions();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            base.write(bw, 4, 0);
        }
        catch(IOException ex) { ex.printStackTrace(System.err); }
    }
    
    public static final void printStringToFile(File file, String text)
    {
        try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file)))
        {
            osw.write(text);
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
}
