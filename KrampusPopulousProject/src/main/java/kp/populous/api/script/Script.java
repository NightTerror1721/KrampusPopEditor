/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import kp.populous.api.IOUtils;
import kp.populous.api.data.Raw;
import kp.populous.api.data.SByte;
import kp.populous.api.data.UInt16;
import org.fife.ui.rsyntaxtextarea.parser.Parser;

/**
 *
 * @author Asus
 */
public final class Script
{
    public final ScriptCodeData codes = new ScriptCodeData();
    public final ScriptFieldData fields = new ScriptFieldData();
    
    public final void read(InputStream is) throws IOException
    {
        codes.data.read(is);
        fields.read(is);
    }
    public final void read(File file) throws IOException
    {
        try(FileInputStream fis = new FileInputStream(file)) { read(fis); }
    }
    
    public final void write(OutputStream os) throws IOException
    {
        codes.data.write(os);
        fields.write(os);
        os.write(new byte[264]);
    }
    public final void write(File file) throws IOException
    {
        try(FileOutputStream fos = new FileOutputStream(file)) { write(fos); }
    }
    
    public final DecompileResult decompile() throws ScriptIOException
    {
        Decompiler dec = new Decompiler(this);
        return new DecompileResult(dec);
    }
    
    public static final CompilationResult compile(InputStream is, Parser parser)
    {
        Compiler cmp = new Compiler(is, parser);
        return cmp.compile();
    }
    public static final CompilationResult compile(InputStream is)
    {
        Compiler cmp = new Compiler(is, null);
        return cmp.compile();
    }
    public static final CompilationResult compile(String source, Parser parser)
    {
        Compiler cmp = new Compiler(source, parser);
        return cmp.compile();
    }
    public static final CompilationResult compile(String source)
    {
        Compiler cmp = new Compiler(source, null);
        return cmp.compile();
    }
    public static final CompilationResult compile(File file, Parser parser) throws IOException
    {
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)))
        {
            return compile(bis, parser);
        }
    }
    public static final CompilationResult compile(File file) throws CompilationException, IOException
    {
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)))
        {
            return compile(bis);
        }
    }
    
    public static final class ScriptCodeData
    {
        private final Raw data = new Raw(ScriptConstant.MAX_CODES * 2);
        public final int length = data.length();

        public final UInt16 getCode(int index)
        {
            if(index < 0 || index >= ScriptConstant.MAX_CODES)
                throw new IllegalArgumentException();
            return data.unsignedInt16(index * 2);
        }

        public final void setCode(int index, UInt16 code)
        {
            if(index < 0 || index >= ScriptConstant.MAX_CODES)
                throw new IllegalArgumentException();
            data.unsignedInt16(index * 2, code);
        }
        
        public final void setVersion()
        {
            data.signedByte(0, SByte.valueOf(12));
            data.signedByte(1, SByte.ZERO);
        }
        public final int getVersion() { return data.signedByte(0).toByte(); }
        
        public final void copyFrom(ScriptCodeData other) { data.copyFrom(other.data); }
        public final void copyFrom(Raw rawCode) { data.copyFrom(rawCode); }
    }
    
    public static final class ScriptFieldData
    {
        private final Field[] data = new Field[ScriptConstant.MAX_FIELDS];
        public final int length = data.length;

        public final Field getField(int index) throws ScriptIOException
        {
            if(index < 0 || index >= ScriptConstant.MAX_FIELDS)
                throw new IllegalArgumentException();
            Field field = data[index];
            return field == null ? Field.INVALID : field;
        }

        public final void setField(int index, Field field)
        {
            if(index < 0 || index >= ScriptConstant.MAX_FIELDS)
                throw new IllegalArgumentException();
            data[index] = field == null ? Field.INVALID : field;
        }
        
        private void read(InputStream is) throws IOException
        {
            byte[] buffer = IOUtils.readFully(is, ScriptConstant.MAX_FIELDS * 8);
            for(int i=0;i<ScriptConstant.MAX_FIELDS;i++)
                data[i] = Field.read(buffer, i * 8);
        }
        
        private void write(OutputStream os) throws IOException
        {
            byte[] buffer = new byte[ScriptConstant.MAX_FIELDS * 8];
            for(int i=0;i<ScriptConstant.MAX_FIELDS;i++)
                Field.write(buffer, i * 8, data[i]);
            os.write(buffer);
        }
    }
    
    public static final class DecompileResult
    {
        private final String code;
        private final List<String> errors;
        
        private DecompileResult(Decompiler dec) throws ScriptIOException
        {
            dec.decompile();
            code = dec.getSource();
            errors = dec.hasErrors() ? Collections.emptyList() : dec.getErrors();
        }
        
        public final boolean hasErrors() { return !errors.isEmpty(); }
        public final List<String> getErrors() { return errors; }
        public final String getCode() { return code; }
    }
}
