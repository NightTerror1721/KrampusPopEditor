/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.Parser;

/**
 *
 * @author Asus
 */
public final class CompilationResult
{
    private final DefaultParseResult parserResult;
    private final LinkedList<String> errors = new LinkedList<>();
    private int lastErrorLine = -1;
    private Script script;
    
    public CompilationResult(Parser parser)
    {
        this.parserResult = parser != null ? new DefaultParseResult(parser) : null;
    }
    
    public final void setScript(Script script) { this.script = script; }
    public final Script getScript() { return script; }
    
    public final void registerError(CompilationException ex, int line)
    {
        if(lastErrorLine == line)
            return;
        lastErrorLine = line;
        createParserError(ex, line);
        errors.add(ex.getMessage());
    }
    
    private void createParserError(CompilationException ex, int line)
    {
        if(parserResult == null)
            return;
        
        DefaultParserNotice notice = new DefaultParserNotice(parserResult.getParser(), ex.getErrorMessage(), line);
        parserResult.addNotice(notice);
    }
    
    public final List<String> getErrors() { return Collections.unmodifiableList(errors); }
    public final boolean hasErrors() { return !errors.isEmpty(); }
    
    public final ParseResult getParseResult() { return parserResult; }
    public final boolean hasParseResult() { return parserResult != null; }
    
    public final String getErrorLog()
    {
        return String.join("\n", errors);
    }
}
