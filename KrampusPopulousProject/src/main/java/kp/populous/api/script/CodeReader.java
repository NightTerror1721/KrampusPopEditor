/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 *
 * @author Marc
 */
public final class CodeReader
{
    public final class Line
    {
        private final int num, offset, len, endset;
        private final char[] chars;
        
        private Line(int num, int offset, char[] buffer)
        {
            this.num = num;
            this.offset = offset;
            chars = buffer;
            len = chars.length + 1;
            endset = offset + chars.length;
        }
        
        public final int getLineNumber() { return num; }
        public final int getOffset() { return offset; }
        public final int getEndset() { return endset; }
        public final char getChar(int index) { return index == chars.length ? '\n' : chars[index]; }
        public final int getLength() { return len; }
    }
    
    private final Line[] source;
    private int index, size, start;
    private Line lcur;
    
    public CodeReader(InputStream is)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        index = 1;
        size = 0;
        source = br.lines().map(bline -> {
                int old = size;
                size += bline.length() + 1;
                return new Line(index++,old,bline.replace("\n","").toCharArray());
            }).toArray(len -> new Line[len]);
        index = -1;
        start = 0;
        lcur = null;
    }
    public CodeReader(String scode)
    {
        String[] lines = scode.replace("\r","").split("\n");
        index = 1;
        size = 0;
        source = Arrays.stream(lines).map(bline -> {
                int old = size;
                size += bline.length() + 1;
                return new Line(index++,old,bline.toCharArray());
        }).toArray(len -> new Line[len]);
        index = -1;
        start = 0;
        lcur = null;
    }
    private CodeReader(CodeReader other)
    {
        source = other.source;
        size = other.size;
        index = other.index;
        start = other.start;
        lcur = other.lcur;
    }
    private CodeReader()
    {
        source = new Line[0];
        size = 0;
        index = 0;
        start = 0;
        lcur = null;
    }
    
    public static final CodeReader empty()
    {
        return new CodeReader();
    }
    
    public final CodeReader subpart(int from, int to)
    {
        CodeReader cr = new CodeReader(this);
        cr.setIndex(from);
        cr.index = cr.start = from;
        cr.size = to;
        return cr;
    }
    
    public final int getCurrentLine()
    {
        return lcur == null || source.length == 0 ? 0 : lcur.num;
    }
    
    public final void reset()
    {
        index = -1;
        lcur = null;
    }
    
    public final char next() throws EOFException
    {
        if(source.length == 0 || index >= size)
            throw new EOFException();
        if(index < 0)
        {
            index = 0;
            lcur = source[0];
            if(lcur.chars.length == 0 || index == lcur.chars.length)
                return '\n';
            return lcur.chars[index];
        }
        index++;
        int offset = index - lcur.offset;
        if(offset == lcur.chars.length) return '\n';
        if(offset > lcur.chars.length)
        {
            if(lcur.num >= source.length)
                throw new EOFException();
            lcur = source[lcur.num];
            offset = index - lcur.offset;
            if(lcur.chars.length == 0 || offset == lcur.chars.length)
                return '\n';
        }
        return lcur.chars[offset];
    }
    
    public final char[] nextArray(int count) throws EOFException
    {
        char[] array = new char[count];
        for(int i=0;i<count;i++)
            array[i] = next();
        return array;
    }
    
    private char move(int to, boolean fix) throws IllegalArgumentException
    {
        if(to < start || to >= size)
            throw new IllegalArgumentException("Out of range 'to' offset");
        int line = lcur == null ? 0 : lcur.num-1;
        if(to == index)
        {
            int idx = index - source[line].offset;
            return idx == source[line].chars.length
                    ? '\n'
                    : source[line].chars[idx];
        }
        if(to > index)
        {
            while(source[line].endset < to)
                line++;
        }
        else
        {
            while(source[line].offset > to)
                line--;
        }
        if(fix)
        {
            lcur = source[line];
            index = to;
            int offset = index - lcur.offset;
            if(lcur.chars.length == 0 || offset == lcur.chars.length)
                return '\n';
            return lcur.chars[offset];
        }
        Line l = source[line];
        int offset = to - l.offset;
        if(l.chars.length == 0 || offset == l.chars.length)
            return '\n';
        return l.chars[offset];
    }
    
    public final char peek() { return move(index,false); }
    public final char peek(int positions) { return move(index+positions,false); }
    public final boolean canPeek(int positions)
    {
        try { move(index+positions,false); }
        catch(IllegalArgumentException ex) { return false; }
        return true;
    }
    
    public final char move(int positions) throws EOFException
    {
        try { return move(index+positions,true); }
        catch(IllegalArgumentException ex) { throw new EOFException(); }
    }
    
    @SuppressWarnings("empty-statement")
    public final void seekOrEnd(char c)
    {
        try { while(next() != c); }
        catch(EOFException ex) {  }
    }
    
    @SuppressWarnings("empty-statement")
    public final void seekOrEnd(char c0, char c1)
    {
        try { 
            for(;;)
            {
                char c = next();
                if(c == c0 && canPeek(1) && peek(1) == c1)
                {
                    next();
                    return;
                }
            }
        }
        catch(EOFException ex) {  }
    }
    
    public final boolean hasNext() { return source.length != 0 && index < size; }
    
    public final int getMaxIndex() { return size; }
    
    public final int getCurrentIndex() { return index; }
    
    public final char setIndex(int index) { return move(index,true); }
    
    public final boolean findIgnoreSpaces(char c)
    {
        int idx = index;
        try
        {
            for(;;)
            {
                char c2 = next();
                if(c2 == ' ' || c2 == '\t')
                    continue;
                if(c2 == c)
                    return true;
                setIndex(idx);
                return false;
            }
        }
        catch(EOFException ex)
        {
            setIndex(idx);
            return false;
        }
    }
}
