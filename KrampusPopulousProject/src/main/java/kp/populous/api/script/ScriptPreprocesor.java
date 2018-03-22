/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import kp.populous.api.script.op.Operator;
import kp.populous.api.script.op.OperatorParser;
import kp.populous.api.utils.TokenizerExtractor;
import static kp.populous.api.utils.TokenizerExtractor.EOF;

/**
 *
 * @author Asus
 */
final class ScriptPreprocesor
{
    private final MacroPool macros;
    private final CompilationResult result;
    private final CodeReader reader;
    private final LineIterator lines = new LineIterator();
    
    private ScriptPreprocesor(CodeReader reader, CompilationResult result, MacroPool macros)
    {
        this.reader = reader;
        this.result = result;
        this.macros = Objects.requireNonNull(macros);
    }
    ScriptPreprocesor(CodeReader reader, CompilationResult result, Collection<ScriptMacro> parentMacros)
    {
        this(reader, result);
        if(parentMacros != null)
            macros.registerMacros(parentMacros);
    }
    ScriptPreprocesor(CodeReader reader, CompilationResult result) { this(reader, result, new MacroPool()); }
    
    public final CodeReader compile() { return parseAll(ParseMode.NORMAL).toCodeReader(); }
    
    
    
    
    
    private CodeWriter parseAll(ParseMode mode)
    {
        CodeWriter writer = new CodeWriter();
        boolean state = true;
        String line;
        while(state && (line = lines.nextLine()) != null)
        {
            if(!line.isEmpty())
            {
                Tokenizer tokens = new Tokenizer(line, lines.min);
                String token = tokens.nextToken();
                if(token != null)
                {
                    try
                    {
                        if(token.equals("#"))
                        {
                            state = computeOp(mode, tokens, writer);
                        }
                        else do {
                            token = parseTokenForMacros(tokens, token);
                            if(tokens.befSpaces > 0)
                                writer.append(' ');
                            writer.append(token);
                            if(tokens.afSpaces > 0)
                                writer.append(' ');
                        } while((token = tokens.nextToken()) != null);
                    }
                    catch(CompilationException ex)
                    {
                        lines.registerError(ex);
                    }
                }
            }
            writer.closeLines(lines.max - lines.min);
        }
        if(mode != ParseMode.NORMAL && state)
            lines.registerError("Expected #endif directive but not found");
        return writer;
    }
    
    private String parseTextForMacros(String text, int line) throws CompilationException
    {
        Tokenizer tokens = new Tokenizer(text, line);
        StringBuilder sb = new StringBuilder(64);
        
        String token;
        while((token = tokens.nextToken()) != null)
        {
            token = parseTokenForMacros(tokens, token);
            if(tokens.befSpaces > 0)
                sb.append(' ');
            sb.append(token);
            if(tokens.afSpaces > 0)
                sb.append(' ');
        }
        return sb.toString();
    }
    
    private boolean computeOp(ParseMode mode, Tokenizer tokens, CodeWriter writer) throws CompilationException
    {
        String token = tokens.nextToken();
        if(token == null)
            throw new CompilationException(tokens.getLine(), "Unexpected Error: Exopected valid Preprocessor operation");
        switch(token)
        {
            case "define":
                doDefine(tokens);
                break;
            case "undef":
                doUndef(tokens);
                break;
            case "include":
                doInclude(tokens, writer);
                break;
            case "echo":
                doEcho(tokens);
                break;
            case "warning":
                doWarning(tokens);
                break;
            case "error":
                doError(tokens);
                break;
            case "ifdef":
                doIfdefIfndef(tokens, true);
                break;
            case "ifndef":
                doIfdefIfndef(tokens, false);
                break;
            case "if":
                doIf(tokens);
                break;
            case "else":
            case "elif":
                if(mode == ParseMode.ENDIF)
                {
                    findConditionalTag("endif");
                    return false;
                }
                throw new CompilationException(tokens.getLine(), token + " directive can only put after ifdef, ifndef, if, else or elif direvtives");
            case "endif":
                if(mode == ParseMode.ENDIF)
                    return false;
                throw new CompilationException(tokens.getLine(), token + " directive can only put after ifdef, ifndef, if, else or elif direvtives");
            default:
                throw new CompilationException(tokens.getLine(), "Invalid preprocessor directive: " + token);
        }
        return true;
    }
    
    
    private void doDefine(Tokenizer tokens) throws CompilationException
    {
        String name = tokens.nextToken();
        if(name == null)
            throw new CompilationException(tokens.getLine(), "Invalid #define command. Require: #define <macro_name> <macro_value>");
        String[] pars = extractDeclarationMacroParameters(tokens);
        String text = tokens.getMacroText();
        if(text == null)
            throw new CompilationException(tokens.getLine(), "Invalid #define command. Require: #define <macro_name> <macro_value>");
        
        Macro macro = new Macro(tokens.line, name, pars, text);
        macros.registerMacro(macro, tokens.getLine());
    }
    
    private void doUndef(Tokenizer tokens) throws CompilationException
    {
        String name = tokens.nextToken();
        if(name == null)
            throw new CompilationException(tokens.getLine(), "Invalid #undef command. Require: #undef <macro_name>");
        
        macros.unregisterMacro(name);
    }
    
    private void doInclude(Tokenizer tokens, CodeWriter writer) throws CompilationException
    {
        tokens.seek('\"');
        String text = tokens.getTextUntil('\"');
        Path path = new File(text).toPath();
        File file = !path.isAbsolute() ? new File("includes/" + path.toString()) : path.toFile();
        if(!file.exists() || !file.isFile())
        {
            if(file.getName().endsWith(".popscr"))
                throw new CompilationException(tokens.getLine(), "Included file " + file.getAbsolutePath() + " not found.");
            file = new File(file.getAbsoluteFile() + ".popscr");
            if(!file.exists() || !file.isFile())
                throw new CompilationException(tokens.getLine(), "Included file " + file.getAbsolutePath() + " not found.");
        }
        String code = importFile(file);
        writer.append(code);
    }
    
    private void doEcho(Tokenizer tokens) throws CompilationException
    {
        tokens.seek('\"');
        String text = tokens.getTextUntil('\"');
        result.insertMessage(parseTextForMacros(text, tokens.getLine()));
    }
    
    private void doWarning(Tokenizer tokens) throws CompilationException
    {
        tokens.seek('\"');
        String text = tokens.getTextUntil('\"');
        text = "[WARNING]" + parseTextForMacros(text, tokens.getLine());
        result.insertMessage(text);
    }
    
    private void doError(Tokenizer tokens) throws CompilationException
    {
        tokens.seek('\"');
        String text = tokens.getTextUntil('\"');
        lines.registerError(parseTextForMacros(text, tokens.getLine()));
    }
    
    private void doIfdefIfndef(Tokenizer tokens, boolean mode) throws CompilationException
    {
        String macro = tokens.nextToken();
        if(macro == null)
            throw new CompilationException(tokens.getLine(), "Invalid " + (mode ? "#ifdef" : "#ifndef") +
                    " command. Require: " + (mode ? "#ifdef" : "#ifndef") + " <macro_name>");
        resolveConditionals(() -> (mode && macros.hasMacro(macro)) || (!mode && !macros.hasMacro(macro)));
    }
    
    private void doIf(Tokenizer tokens) throws CompilationException
    {
        String text = parseTextForMacros(tokens.getMacroText(), tokens.getLine());
        Operator op = OperatorParser.parse(text, tokens.getLine());
        resolveConditionals(() -> op.apply() != 0);
    }
    
    
    private String importFile(File file) throws CompilationException
    {
        try(FileInputStream fis = new FileInputStream(file))
        {
            CompilationResult cresult = new CompilationResult(null);
            ScriptPreprocesor prep = new ScriptPreprocesor(new CodeReader(fis), cresult, macros);
            String ppcode = prep.parseAll(ParseMode.NORMAL).toString();
            if(cresult.hasErrors())
                for(String error : cresult.getErrors())
                    lines.registerError(error);
            if(cresult.hasMessages())
                for(String msg : cresult.getMessages())
                    result.insertMessage(msg);
            return ppcode;
        }
        catch(IOException ex)
        {
            throw new CompilationException(lines.min, ex.getMessage());
        }
    }
    
    
    private String findConditionalTag(String... tags) throws CompilationException
    {
        int startLine = lines.max;
        int br = 0;
        String line;
        while((line = lines.nextLine()) != null)
        {
            Tokenizer tokens = new Tokenizer(line, lines.min);
            String token = tokens.nextToken();
            if(token == null || !token.equals("#"))
                continue;
            token = tokens.nextToken();
            if(token == null)
                continue;
            if(br == 0)
                for(String tag : tags)
                    if(token.equals(tag))
                        return line;
            switch(token)
            {
                case "if":
                //case "else":
                //case "elif":
                case "ifdef":
                case "ifndef":
                    br++;
                    break;
                case "endif":
                    br--;
                    break;
            }
            if(br < 0)
                throw new CompilationException(lines.min, "Unexpected #endif directive");
        }
        throw new CompilationException(startLine, "Unexpected End of Line");
    }
    
    
    private void resolveConditionals(BooleanSupplier conds) throws CompilationException
    {
        if(conds.getAsBoolean())
            parseAll(ParseMode.ENDIF);
        else computeElses(findConditionalTag("else", "elif", "endif"), lines.min);
    }
    
    
    private void computeElses(String line, int lineIdx) throws CompilationException
    {
        Tokenizer tokens = new Tokenizer(line, lineIdx);
        String token = tokens.nextToken();
        if(token == null || !token.equals("#"))
            throw new CompilationException(lineIdx, "Expected \"#\"");
        token = tokens.nextToken();
        if(token == null)
            throw new CompilationException(lineIdx, "Expected #else, #elif or #endif directives");
        switch(token)
        {
            case "endif":
                break;
            case "elif": //TODO: implement
                doIf(tokens);
                break;
            case "else":
                parseAll(ParseMode.ENDIF);
                break;
            default:
                throw new CompilationException(lineIdx, "Expected #else, #elif or #endif directives");
        }
    }
    
    
    
    
    
    
    private String expandMacro(Tokenizer tokens, ScriptMacro macro) throws CompilationException
    {
        String[] pars = extractInvokationMacroParameters(tokens, macro);
        return macro.expand(macros, pars);
    }
    
    private String parseTokenForMacros(Tokenizer tokens, String token) throws CompilationException
    {
        if(!macros.hasMacro(token))
            return token;
        return expandMacro(tokens, macros.getMacro(token));
    }
    
    private String[] extractDeclarationMacroParameters(Tokenizer tokens) throws CompilationException
    {
        if(tokens.peekChar() != '(')
            return new String[0];
        LinkedList<String> pars = new LinkedList<>();
        boolean afterComma = false;
        tokens.nextToken(); //skip "(" token
        String token;
        while((token = tokens.nextToken()) != null)
        {
            if(token.equals(")"))
            {
                if(afterComma)
                    throw new CompilationException(tokens.getLine(), "Unexpected \")\" after \",\" in Macro parameters list");
                return pars.toArray(new String[pars.size()]);
            }
            pars.add(parseTokenForMacros(tokens, token));
            token = tokens.nextToken();
            switch(token)
            {
                case ")": return pars.toArray(new String[pars.size()]);
                case ",":
                    afterComma = true;
                    break;
                default: throw new CompilationException(tokens.getLine(), "Expected \",\" or \")\" tokens in Macro parameters list");
            }
        }
        throw new CompilationException(tokens.getLine(), "Unexpected End of File");
    }
    
    private String[] extractInvokationMacroParameters(Tokenizer tokens, ScriptMacro macro) throws CompilationException
    {
        if(tokens.peekChar() != '(')
            return new String[0];
        LinkedList<String> pars = new LinkedList<>();
        boolean afterComma = false;
        tokens.nextToken(); //skip "(" token
        int count = -1;
        String token;
        while((token = tokens.getTextUntil(',', ')')) != null)
        {
            count++;
            char last = tokens.peekChar();
            token = macro.expandParameter(count) ? parseTextForMacros(token, tokens.getLine()) : token;
            pars.add(token);
            switch(last)
            {
                case ')': return pars.toArray(new String[pars.size()]);
                case ',':
                    afterComma = true;
                    break;
                default: throw new CompilationException(tokens.getLine(), "Expected \",\" or \")\" tokens in Macro parameters list");
            }
        }
        throw new CompilationException(tokens.getLine(), "Unexpected End of File");
    }
    
    
    
    private final class Macro implements ScriptMacro
    {
        private final String name;
        private final HashMap<String, Integer> pars;
        private final LinkedList<MacroPart> parts;
        private final boolean varargs;
        
        public Macro(int line, String name, String[] pars, String text) throws CompilationException
        {
            this.name = Objects.requireNonNull(name);
            this.pars = new HashMap<>();
            for(int i=0;i<pars.length;i++)
                this.pars.put(pars[i], i);
            this.parts = new LinkedList<>();
            this.varargs = pars.length > 0 && pars[pars.length - 1].equals("...");
            init(text, line);
        }
        
        private void init(String text, int line) throws CompilationException
        {
            Tokenizer tokens = new Tokenizer(text, line);
            StringBuilder sb = new StringBuilder(64);
            String token;
            while((token = tokens.nextToken(true)) != null)
            {
                boolean vargs;
                if((vargs = token.equals("__ARGS__")) || pars.containsKey(token))
                {
                    if(sb.length() > 0)
                    {
                        if(tokens.befSpaces > 0)
                            sb.append(' ');
                        parts.add(MacroPart.text(sb.toString()));
                        sb.delete(0, sb.length());
                    }
                    parts.add(vargs ? MacroPart.VARARGS : MacroPart.parameter(token, pars.get(token)));
                    if(tokens.afSpaces > 0)
                        sb.append(' ');
                }
                else if(macros.hasMacro(token))
                {
                    if(sb.length() > 0)
                    {
                        if(tokens.befSpaces > 0)
                            sb.append(' ');
                        parts.add(MacroPart.text(sb.toString()));
                        sb.delete(0, sb.length());
                    }
                    parts.add(MacroPart.text(expandMacro(tokens, macros.getMacro(token))));
                    if(tokens.afSpaces > 0)
                        sb.append(' ');
                }
                else
                {
                    if(tokens.befSpaces > 0)
                        sb.append(' ');
                    sb.append(token);
                    
                }
            }
            if(sb.length() > 0)
            {
                if(tokens.befSpaces > 0)
                    sb.append(' ');
                parts.add(MacroPart.text(sb.toString()));
                if(tokens.afSpaces > 0)
                    sb.append(' ');
            }
        }
        
        @Override
        public final String getName() { return name; }
        
        @Override
        public final boolean expandParameter(int index) { return true; }
        
        public final List<String> getParameters() { return new ArrayList<>(pars.keySet()); }
        
        public final boolean isVarargs() { return varargs; }
        
        public final String expand(String[] args) { return expand(macros, args); }
        
        @Override
        public final String expand(MacroPool macros, String[] args)
        {
            StringBuilder sb = new StringBuilder(64);
            final int len = pars.size();
            for(MacroPart part : parts)
            {
                switch(part.getType())
                {
                    case MacroPart.TYPE_TEXT:
                        sb.append(part.getText());
                        break;
                    case MacroPart.TYPE_PAR: {
                        int idx = part.getIndex();
                        if(idx < args.length)
                            sb.append(args[idx]);
                    } break;
                    case MacroPart.TYPE_VARARGS: {
                        if(varargs)
                        {
                            int idx = len - 1;
                            if(idx < args.length)
                            {
                                String[] vargs = new String[args.length - idx];
                                System.arraycopy(args, idx, vargs, 0, vargs.length);
                                sb.append(String.join(", ", vargs));
                            }
                        }
                    } break;
                    default: throw new IllegalStateException();
                }
            }
            return sb.toString();
        }
    }
    
    private static final class MacroPart
    {
        public static final byte TYPE_TEXT = 0;
        public static final byte TYPE_PAR = 1;
        public static final byte TYPE_VARARGS = 2;
        
        private static final MacroPart VARARGS = new MacroPart(TYPE_VARARGS, "", -1);
        
        private final byte type;
        private final String text;
        private final int index;
        
        private MacroPart(byte type, String text, int index)
        {
            this.type = type;
            this.text = text;
            this.index = index;
        }
        
        public final byte getType() { return type; }
        public final String getText() { return text; }
        public final int getIndex() { return index; }
        
        public static final MacroPart text(String text) { return new MacroPart(TYPE_TEXT, text, -1); }
        public static final MacroPart parameter(String name, int index) { return new MacroPart(TYPE_PAR, name, index); }
        public static final MacroPart varargs() { return VARARGS; }
    }
    
    private static String tokenizedParse(String text, int line, Function<String, String> action)
    {
        StringBuilder result = new StringBuilder(text.length());
        tokenize(text, line, (token, c) -> result.append(action.apply(token)).append(c));
        return result.toString();
    }
    
    private static void tokenize(String text, int line, TokenizeAction action)
    {
        Tokenizer t = new Tokenizer(text, line);
        String token;
        while((token = t.nextToken()) != null)
            action.apply(token, t.peekChar());
    }
    
    private static final class Tokenizer
    {
        private final TokenizerExtractor source;
        private int afSpaces, befSpaces;
        private int line;
        private char prevChar = EOF;
        
        private Tokenizer(String text, int line)
        {
            this.line = line;
            this.source = new TokenizerExtractor()
            {
                private final char[] chars = text.toCharArray();
                private int idx = -1;
                
                @Override
                public final char nextChar()
                {
                    return idx + 1 >= chars.length ? EOF : chars[++idx];
                }
                
                @Override
                public final char peekChar(int amount)
                {
                    amount = this.idx + amount;
                    return amount < 0 || amount >= chars.length ? EOF : chars[amount];
                }
            };
        }
        private Tokenizer(TokenizerExtractor tokenizer, int line)
        {
            if(tokenizer == null)
                throw new NullPointerException();
            this.line = line;
            this.source = tokenizer;
        }
        
        public final int getLine() { return line; }
        
        public final char peekChar() { return prevChar != EOF ? prevChar : source.peekChar(); }
        public final char peekChar(int idx) { return source.peekChar(prevChar != EOF ? idx - 1 : idx); }
        public final char nextChar()
        {
            if(prevChar != EOF)
            {
                char c = prevChar;
                prevChar = EOF;
                return c;
            }
            return source.nextChar();
        }
        
        public final String getMacroText()
        {
            StringBuilder sb = new StringBuilder(64);
            boolean start = false;
            char c;
            
            while((c = nextChar()) != EOF)
            {
                switch(c)
                {
                    case '\r':
                        break;
                    case '\n':
                    case ' ':
                    case '\t':
                        if(start)
                            sb.append(c);
                        break;
                    default:
                        if(!start)
                            start = true;
                        sb.append(c);
                        break;
                }
            }
            
            return sb.toString();
        }
        
        public final String getTextUntil(char... chars) throws CompilationException
        {
            StringBuilder sb = new StringBuilder(16);
            
            char c;
            while((c = nextChar()) != EOF)
            {
                for(int i=0;i<chars.length;i++)
                    if(c == chars[i])
                        return sb.toString();
                sb.append(c);
            }
            throw new CompilationException(line, "Expected " + Arrays.toString(chars) + " characters. But not found. Unexpected End of File");
        }
        
        public final void seek(char target) throws CompilationException
        {
            char c;
            while((c = nextChar()) != EOF)
                if(c == target)
                    return;
            throw new CompilationException(line, "Expected " + target + " characters. But not found. Unexpected End of File");
        }
        
        public final String nextToken() { return nextToken(false); }
        public final String nextToken(boolean catchEOL)
        {
            StringBuilder sb = new StringBuilder(64);
            afSpaces = befSpaces = 0;
            
            char c;
            while((c = nextChar()) != EOF)
            {
                switch(c)
                {
                    case '\r':
                        break;
                    case '\n':
                        if(catchEOL)
                        {
                            if(sb.length() > 0)
                            {
                                prevChar = c;
                                return sb.toString();
                            }
                            return Character.toString(c);
                        }
                    case ' ':
                    case '\t':
                        if(sb.length() > 0)
                        {
                            afSpaces++;
                            return sb.toString();
                        }
                        befSpaces++;
                        break;
                    case ',':
                    case '#':
                    case '(': case ')':
                    case '{': case '}':
                    case '=': case '!': case '>': case '<':
                    case '+': case '-': case '*': case '/':
                        if(sb.length() > 0)
                        {
                            prevChar = c;
                            return sb.toString();
                        }
                        return Character.toString(c);
                    default:
                        sb.append(c);
                        break;
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
        
        public final boolean checkNextChar(char c) { return nextChar() == c; }
        public final boolean checkPeekNextChar(char c) { return peekChar(1) == c; }
        public final boolean checkPeekCurrentChar(char c) { return peekChar() == c; }
        
        public final boolean checkNextToken(String token)
        {
            String t = nextToken();
            return t != null && t.equals(token);
        }
    }
    
    @FunctionalInterface
    private interface TokenizeAction { void apply(String token, char lastChar); }
    
    private final class LineIterator
    {
        private int min, max;
        private final StringBuilder sb = new StringBuilder(128);
        private String currentLine;
        boolean end = false;
        
        public final String nextLine()
        {
            min = reader.getCurrentLine();
            try
            {
                main_loop:
                for(;;)
                {
                    char c = reader.next();
                    switch(c)
                    {
                        case '\\':
                            c = reader.next();
                            if(c == '\n')
                            {
                                sb.append('\n');
                                break;
                            }
                            sb.append('\\');
                            if(c == EOF)
                                break main_loop;
                            sb.append(c);
                        case '\n':
                            break main_loop;
                        default:
                            sb.append(c);
                            break;
                    }
                }
            }
            catch(EOFException ex) { end = true; }
            max = reader.getCurrentLine();
            String line = sb.toString();
            sb.delete(0, sb.length());
            return currentLine = (end && line.isEmpty() ? null : line);
        }
        
        public final void registerError(CompilationException ex)
        {
            for(int i=min;i<=max;i++)
                result.registerError(ex, i);
        }
        
        public final void registerError(String errorMessage)
        {
            for(int i=min;i<=max;i++)
                result.registerError(errorMessage, i);
        }
        
        public final String getCurrentLine() { return currentLine; }
    }
    
    private static enum ParseMode { NORMAL, ENDIF }
}
