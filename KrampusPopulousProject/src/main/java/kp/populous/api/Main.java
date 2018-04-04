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
        Utils.useSystemLookAndFeel();
        Utils.initPopScriptLanguage();
        
        //RSTAUIDemoApp.main(args);
        ScriptEditor.open();
    }
}
