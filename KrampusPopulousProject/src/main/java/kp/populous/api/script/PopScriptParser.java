/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

/**
 *
 * @author Asus
 */
public final class PopScriptParser extends AbstractParser
{
    public PopScriptParser()
    {
        super.setEnabled(true);
    }
    
    @Override
    public ParseResult parse(RSyntaxDocument doc, String style)
    {
        try
        {
            String text = doc.getText(0, doc.getLength());
            return Script.compile(text, this).getParseResult();
        }
        catch(BadLocationException ex)
        {
            ex.printStackTrace(System.err);
            return new CompilationResult(this).getParseResult();
        }
    }
}
