/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 *
 * @author Asus
 */
public final class CodeWriter implements Iterable<char[]>
{
    private static final char[] EMPTY_LINE = {};
    
    private final LinkedList<char[]> lines = new LinkedList<>();
    private final StringBuilder cline = new StringBuilder(128);
    
    public final CodeWriter append(String code) { cline.append(code); return this; }
    public final CodeWriter append(char code) { cline.append(code); return this; }
    public final CodeWriter append(byte code) { cline.append(code); return this; }
    public final CodeWriter append(short code) { cline.append(code); return this; }
    public final CodeWriter append(int code) { cline.append(code); return this; }
    public final CodeWriter append(long code) { cline.append(code); return this; }
    public final CodeWriter append(float code) { cline.append(code); return this; }
    public final CodeWriter append(double code) { cline.append(code); return this; }
    public final CodeWriter append(boolean code) { cline.append(code); return this; }
    public final CodeWriter append(Object code) { cline.append(code); return this; }
    
    public final void closeLine()
    {
        if(cline.length() > 0)
        {
            lines.add(cline.toString().toCharArray());
            cline.delete(0, cline.length());
        }
        else lines.add(EMPTY_LINE);
    }
    
    public final void closeLines(int lines)
    {
        while(lines-- > 0)
            closeLine();
    }
    
    public final CodeReader toCodeReader() { return new CodeReader(this); }

    @Override
    public final Iterator<char[]> iterator() { return lines.iterator(); }
    
    public final Stream<char[]> stream() { return lines.stream(); }
    
    @Override
    public final String toString()
    {
        return String.join("\n", stream().map(String::new).toArray(size -> new String[size]));
    }
}
