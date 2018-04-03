/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.io.EOFException;

/**
 *
 * @author Marc
 */
public final class ScriptCommentRemover
{
    private ScriptCommentRemover() {}
    
    private static final int NO_COMMENT = 0;
    private static final int LINE_COMMENT = 1;
    private static final int MULTILINE_COMMENT = 2;
    
    public static final CodeReader removeComments(CodeReader source)
    {
        if(!source.hasNext())
            return source;
        CodeWriter writer = new CodeWriter();
        int mode = 0;
        
        try
        {
            for(;;)
            {
                char c = source.next();
                switch(c)
                {
                    case '\n':
                        writer.closeLine();
                        if(mode == LINE_COMMENT)
                            mode = NO_COMMENT;
                        continue;
                    case '/':
                        if(mode == NO_COMMENT && source.canPeek(1))
                        {
                            if(source.peek(1) == '*')
                            {
                                source.next();
                                mode = MULTILINE_COMMENT;
                                continue;
                            }
                            else if(source.peek(1) == '/')
                            {
                                source.next();
                                mode = LINE_COMMENT;
                                continue;
                            }
                        }
                        break;
                    case '*':
                        if(mode == MULTILINE_COMMENT && source.canPeek(1) && source.peek(1) == '/')
                        {
                            source.next();
                            mode = NO_COMMENT;
                            continue;
                        }
                        break;
                }
                if(mode == NO_COMMENT)
                    writer.append(c);
            }
        }
        catch(EOFException ex) {}
        
        writer.closeLine();
        return writer.toCodeReader();
    }
}
