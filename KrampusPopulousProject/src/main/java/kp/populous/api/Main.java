/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api;

import java.io.IOException;
import kp.populous.api.script.CompilationException;
import kp.populous.api.script.ScriptEditor;
import kp.populous.api.script.ScriptIOException;
import kp.populous.api.utils.Utils;

/**
 *
 * @author Marc
 */
public final class Main
{
    public static void main(String[] args) throws IOException, ScriptIOException, CompilationException
    {
        /*System.out.println(SByte.MAX);
        
        Script script = new Script();
        script.read(new File("cpscr038.dat"));
        script.write(new File("test.dat"));
        script.read(new File("test.dat"));
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("scriptDec.txt")))))
        {
            bw.write(script.decompile().getCode());
            
        }
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("internals.txt")))))
        {
            bw.write(Internal.generateOnePerLine());
            
        }
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("functions.txt")))))
        {
            bw.write(Token.generateOnePerLineFunctions());
            
        }
        
        Script s2 = Script.compile(new File("scriptDec.txt"));
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("scriptDec2.txt")))))
        {
            bw.write(s2.decompile());
            
        }
        
        ScriptFunctions.printXmlCompletions(new File("FunctionCompletions.xml"));
        ScriptConstant.printXmlCompletions(new File("ConstantCompletions.xml"));*/
        
        //Utils.printDefaultCompletions(new File("PopLangCompletion.json"));
        
        Utils.useSystemLookAndFeel();
        Utils.initPopScriptLanguage();
        //ScriptEditorTest.main(args);
        
        ScriptEditor.open();
        
        /*for(Token token : Token.values())
            System.out.println(token.getFunctionName());*/
    }
    
    
}
