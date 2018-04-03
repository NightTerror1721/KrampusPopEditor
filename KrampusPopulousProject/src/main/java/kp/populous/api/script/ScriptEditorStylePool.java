/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import kp.populous.api.utils.Prop;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.json.JSONObject;

/**
 *
 * @author Asus
 */
public final class ScriptEditorStylePool
{
    public static final Font DEFAULT_FONT = new Font("monospaced", Font.PLAIN, 13);
    public static final Color DEFAULT_FG_COLOR = Color.BLACK;
    public static final Color DEFAULT_BG_COLOR = Color.WHITE;
    
    private final Style[] styles;
    
    public ScriptEditorStylePool()
    {
        this.styles = StyleToken.styleArray();
        generateDefaults();
        load();
    }
    
    private ScriptEditorStylePool(ScriptEditorStylePool parent)
    {
        this.styles = new Style[parent.styles.length];
        for(int i=0;i<styles.length;i++)
            styles[i] = new Style(parent.styles[i].foreground, parent.styles[i].background, parent.styles[i].font);
    }
    
    private void generateDefaults()
    {
        setStyle(StyleToken.RESERVED_WORD, Color.BLUE);
        setStyle(StyleToken.CONSTANT, Color.GREEN.darker());
        setStyle(StyleToken.FUNCTION, new Color(153, 153, 0));
        setStyle(StyleToken.IDENTIFIER);
        setStyle(StyleToken.SEPARATOR, new Color(255, 102, 0));
        setStyle(StyleToken.OPERATOR, new Color(255, 102, 0));
        setStyle(StyleToken.NUMBER, new Color(153, 0, 153));
        setStyle(StyleToken.COMMENT_LINE, Color.GRAY);
        setStyle(StyleToken.COMMENT_MULTILINE, Color.GRAY);
    }
    
    private void load()
    {
        try
        {
            List<?> all = Prop.getList("scripteditor.styles");
            for(Object obj : all)
            {
                if(!(obj instanceof JSONObject))
                    continue;
                JSONObject jstyle = (JSONObject) obj;
                loadStyle(jstyle);
            }
        }
        catch(Exception ex) { ex.printStackTrace(System.err); }
    }
    
    private void loadStyle(JSONObject jstyle)
    {
        String name = jstyle.optString("name");
        if(name == null)
            return;
        StyleToken token = StyleToken.fromPropertyName(name);
        if(token == StyleToken.UNKNOWN)
            return;
        Style style = styles[token.ordinal() - 1];
        style.font = loadFont(jstyle);
        style.foreground = loadColor(jstyle, "fg", DEFAULT_FG_COLOR);
        style.background = loadColor(jstyle, "bg", DEFAULT_BG_COLOR);
    }
    
    private Font loadFont(JSONObject jstyle)
    {
        String name = jstyle.optString("font_name");
        int type = jstyle.optInt("font_style", -1);
        int size = jstyle.optInt("font_size", -1);
        if(name == null || type < 0 || size < 1)
            return DEFAULT_FONT;
        return new Font(name, type, size);
    }
    
    private Color loadColor(JSONObject jstyle, String pname, Color defaultColor)
    {
        Number n = jstyle.optNumber(pname);
        if(n == null)
            return defaultColor;
        return new Color(n.intValue());
    }
    
    public final void save()
    {
        List<JSONObject> list = new LinkedList<>();
        for(int i=0;i<styles.length;i++)
        {
            StyleToken token = StyleToken.decode(i + 1);
            Style style = styles[i];
            JSONObject jstyle = new JSONObject();
            
            jstyle.put("name", token.getPropertyName());
            
            jstyle.put("font_name", style.font.getName());
            jstyle.put("font_style", style.font.getStyle());
            jstyle.put("font_size", style.font.getSize());
            
            jstyle.put("fg", style.foreground == null ? DEFAULT_FG_COLOR.getRGB() : style.foreground.getRGB());
            jstyle.put("bg", style.background == null ? DEFAULT_BG_COLOR.getRGB() : style.background.getRGB());
            
            list.add(jstyle);
        }
        Prop.set("scripteditor.styles", list);
    }
    
    public final Style getStyle(StyleToken token)
    {
        return token == null || token == StyleToken.UNKNOWN ? null : styles[token.ordinal() - 1];
    }
    
    public final void setStyle(StyleToken token, Font font, Color fg, Color bg)
    {
        Style s = getStyle(token);
        if(s == null)
            return;
        s.font = Objects.requireNonNull(font);
        s.foreground = fg;
        s.background = bg;
    }
    public final void setStyle(StyleToken token, Color fg, Color bg) { setStyle(token, DEFAULT_FONT, fg, bg); }
    public final void setStyle(StyleToken token, Font font, Color fg) { setStyle(token, font, fg, DEFAULT_BG_COLOR); }
    public final void setStyle(StyleToken token, Color fg) { setStyle(token, DEFAULT_FONT, fg, DEFAULT_BG_COLOR); }
    public final void setStyle(StyleToken token) { setStyle(token, DEFAULT_FONT, DEFAULT_FG_COLOR, DEFAULT_BG_COLOR); }
    
    public final void fillScheme(SyntaxScheme scheme)
    {
        for(int i=0;i<styles.length;i++)
        {
            StyleToken token = StyleToken.decode(i + 1);
            scheme.setStyle(token.getTokenCode(), styles[i]);
        }
    }
    
    
    public final ScriptEditorStylePool copy() { return new ScriptEditorStylePool(this); }
    
    public final void copyTo(ScriptEditorStylePool target)
    {
        for(int i=0;i<target.styles.length&&i<styles.length;i++)
        {
            Style ts = target.styles[i];
            Style s = styles[i];
            
            ts.font = s.font;
            ts.foreground = s.foreground;
            ts.background = s.background;
        }
    }
    
    
    
    
    public enum StyleToken
    {
        UNKNOWN(TokenTypes.NULL),
        CONSTANT(TokenTypes.RESERVED_WORD),
        RESERVED_WORD(TokenTypes.RESERVED_WORD_2),
        FUNCTION(TokenTypes.FUNCTION),
        IDENTIFIER(TokenTypes.IDENTIFIER),
        SEPARATOR(TokenTypes.SEPARATOR),
        OPERATOR(TokenTypes.OPERATOR),
        NUMBER(TokenTypes.LITERAL_NUMBER_DECIMAL_INT),
        COMMENT_LINE(TokenTypes.COMMENT_EOL),
        COMMENT_MULTILINE(TokenTypes.COMMENT_MULTILINE);
        
        private final int tokenCode;
        private final String propName;
        private final String strName;
        
        private StyleToken(int tokenCode)
        {
            this.tokenCode = tokenCode;
            this.propName = name().toLowerCase();
            
            char[] chars = propName.replace('_', ' ').toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            this.strName = new String(chars);
        }
        
        public final int getTokenCode() { return tokenCode; }
        public final String getPropertyName() { return propName; }
        
        private static final StyleToken[] VALUES = values();
        public static final StyleToken decode(int code)
        {
            return code < 0 || code >= VALUES.length ? UNKNOWN : VALUES[code];
        }
        
        public static final StyleToken fromPropertyName(String name)
        {
            try { return StyleToken.valueOf(name.toUpperCase()); }
            catch(IllegalArgumentException ex) { return UNKNOWN; }
        }
        
        public static final StyleToken[] getAvailableTokens()
        {
            StyleToken[] tokens = new StyleToken[VALUES.length - 1];
            System.arraycopy(VALUES, 1, tokens, 0, tokens.length);
            Arrays.sort(tokens, (t0, t1) -> t0.strName.compareTo(t1.strName));
            return tokens;
        }
        
        private static Style[] styleArray()
        {
            Style[] styles = new Style[VALUES.length - 1];
            for(int i=0;i<styles.length;i++)
                styles[i] = new Style(DEFAULT_FG_COLOR, DEFAULT_BG_COLOR, DEFAULT_FONT);
            return styles;
        }
        
        @Override
        public final String toString() { return strName; }
    }
}
