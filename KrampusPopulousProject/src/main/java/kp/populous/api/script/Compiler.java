/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.io.EOFException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import kp.populous.api.data.SInt32;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant.Internal;
import kp.populous.api.script.ScriptConstant.Token;
import kp.populous.api.script.ScriptFunctions.Function;
import kp.populous.api.script.ScriptFunctions.Parameter;
import org.fife.ui.rsyntaxtextarea.parser.Parser;

/**
 *
 * @author Asus
 */
final class Compiler
{
    private CodeReader reader;
    private final CodeManager codes = new CodeManager();
    private final FieldManager fields = new FieldManager();
    private final CompilationResult result;
    private int cline;
    
    Compiler(InputStream is, Parser parser)
    {
        reader = new CodeReader(is);
        result = new CompilationResult(parser);
    }
    Compiler(String code, Parser parser)
    {
        reader = new CodeReader(code);
        result = new CompilationResult(parser);
    }
    
    public final CompilationResult compile()
    {
        removeComments();
        compilePreprocessor();
        compileBody(true);
        if(result.hasErrors())
            return result;
        
        Script script = new Script();
        codes.fillScriptCode(script);
        fields.fillScriptFields(script);
        result.setScript(script);
        
        return result;
    }
    
    private void removeComments()
    {
        reader = ScriptCommentRemover.removeComments(reader);
    }
    
    private void compilePreprocessor()
    {
        ScriptPreprocesor prep = new ScriptPreprocesor(reader, result);
        reader = prep.compile();
    }
    
    @SuppressWarnings("empty-statement")
    private void compileBody(boolean first)
    {
        try
        {
            codes.add(Token.BEGIN);
            if(!first && checkOneLineBody())
                compileInstruction(first);
            else while(!compileInstruction(first));
            codes.add(Token.END);
            if(first)
                codes.add(Token.SCRIPT_END);
        }
        catch(CompilationException ex)
        {
            result.registerError(ex, reader.getCurrentLine());
            if(!reader.hasNext())
                return;
            reader.seekOrEnd('\n');
        }
    }
    
    private boolean compileInstruction(boolean allowEnd)
    {
        int initialLine = reader.getCurrentLine();
        try
        {
            SourceToken token = nextToken();
            if(token == null)
            {
                if(allowEnd)
                    return true;
                error("Unexpected end of file. Expected '}'. But not found");
                return true;
            }
            switch(token.toString())
            {
                case "}":
                    if(allowEnd)
                        error("Unexpected '}' out of any body");
                    return true;
                case "set": compileSetIncDec(Token.SET); break;
                case "inc": compileSetIncDec(Token.INCREMENT); break;
                case "dec": compileSetIncDec(Token.DECREMENT); break;
                case "mul": compileMulDiv(Token.MULTIPLY); break;
                case "div": compileMulDiv(Token.DIVIDE); break;
                case "every": compileEvery(); break;
                case "if": compileIf(); break;
                default: compileFunction(token);
            }
            return false;
        }
        catch(CompilationException ex)
        {
            result.registerError(ex, cline);
            if(!reader.hasNext())
                return true;
            reader.seekOrEnd('\n');
            return false;
        }
    }
    
    private SourceToken[] extractParameters(String command, int min, int max) throws CompilationException
    {
        if(min > max)
            throw new IllegalStateException();
        if(max < 1)
        {
            checkNextToken(SourceToken.PARENTHESIS_END);
            return new SourceToken[0];
        }
        ArrayList<SourceToken> tokens = new ArrayList<>(max);
        for(int i=0;i<max;i++)
        {
            SourceToken token = nextToken();
            if(token == null) {
                error("Unexpected end of file"); return null;
            }
            if(token.equals(SourceToken.PARENTHESIS_END))
            {
                if(i < min)
                    error("Expected minimum " + min + " parameters in '" + command + "' command. But found " + i);
                return tokens.toArray(new SourceToken[tokens.size()]);
            }
            if(!token.isValidValue())
                error("Expected valid parameter value (variable, constant, internal or token). But found '" + token + "'");
            tokens.add(token);
            token = nextToken();
            if(token == null) {
                error("Unexpected end of file"); return null;
            }
            switch(token.toString())
            {
                case ")":
                    if(i + 1 < min)
                        error("Expected minimum " + min + " parameters in '" + command + "' command. But found " + i);
                    return tokens.toArray(new SourceToken[tokens.size()]);
                case ",": break;
                default: error("Expected ')' or ',' but found '" + token + "'");
            }
        }
        error("Expected maximum " + max + " parameters in '" + command + "' command. But found more");
        return null;
    }
    
    private void compileFieldParameter(SourceToken token) throws CompilationException
    {
        codes.addField(token);
    }
    
    private void compileConstantParameter(SourceToken token) throws CompilationException
    {
        if(!token.isValidConstant())
            error("Expected valid constant. But found '" + token + "'");
        UInt16 index = fields.registerConstant(token);
        if(index == null)
            error("Expected valid constant. But found '" + token + "'");
        codes.add(index);
    }
    
    private void compileParameter(SourceToken token, Parameter par) throws CompilationException
    {
        if(token.isValidToken())
        {
            Token t = token.toToken();
            if(!par.isValidToken(t))
                error("Expected valid token from list: " + par.getTokensStringList() + ". But found " + token);
            codes.add(t);
            return;
        }
        if(!par.allowField())
            error("Unexpected field value. Expected token from list: " + par.getTokensStringList() + ". But found " + token);
        compileFieldParameter(token);
    }
    
    private void compileSetIncDec(Token token) throws CompilationException
    {
        checkNextToken(SourceToken.PARENTHESIS_BEGIN);
        
        SourceToken[] pars = extractParameters(token.getTokenName(), 2, 2);
        codes.add(token);
        compileFieldParameter(pars[0]);
        compileFieldParameter(pars[1]);
    }
    
    private void compileMulDiv(Token token) throws CompilationException
    {
        checkNextToken(SourceToken.PARENTHESIS_BEGIN);
        
        SourceToken[] pars = extractParameters(token.getTokenName(), 3, 3);
        codes.add(token);
        compileFieldParameter(pars[0]);
        compileFieldParameter(pars[1]);
        compileFieldParameter(pars[2]);
    }
    
    private void compileEvery() throws CompilationException
    {
        checkNextToken(SourceToken.PARENTHESIS_BEGIN);
        
        SourceToken[] pars = extractParameters("every", 1, 2);
        codes.add(Token.EVERY);
        for(SourceToken par : pars)
            compileConstantParameter(par);
        compileBody(false);
    }
    
    private void compileIf() throws CompilationException
    {
        codes.add(Token.IF);
        checkNextToken(SourceToken.PARENTHESIS_BEGIN);
        
        compileCondition();
        compileBody(false);
        
        if(checkHasElse())
        {
            codes.add(Token.ELSE);
            compileBody(false);
        }
        codes.add(Token.ENDIF);
    }
    
    private void compileFunction(SourceToken token) throws CompilationException
    {
        checkNextToken(SourceToken.PARENTHESIS_BEGIN);
        
        if(!token.isValidToken())
            error("Function '" + token + "' does not exists");
        Function func = ScriptFunctions.get(token.toToken());
        if(func == null)
        {
            error("Function '" + token + "' does not exists");
            return;
        }
        SourceToken[] pars = extractParameters(token.toString(), func.getParameterCount(), func.getParameterCount());
        
        codes.add(Token.DO);
        codes.add(func.getCommandToken());
        for(int i=0;i<pars.length;i++)
            compileParameter(pars[i], func.getParameter(i));
    }
    
    private SourceToken[] extractUntil(SourceToken end) throws CompilationException
    {
        LinkedList<SourceToken> tokens = new LinkedList<>();
        for(;;)
        {
            SourceToken token = nextToken();
            if(token == null) {
                error("Unexpected end of file"); return null;
            }
            if(token.equals(end))
                break;
            tokens.add(token);
        }
        return tokens.toArray(new SourceToken[tokens.size()]);
    }
    
    private void compileCondition() throws CompilationException
    {
        SourceToken[] tokens = extractUntil(SourceToken.PARENTHESIS_END);
        TokenIterator it = new TokenIterator(tokens);
        Operation op = generateOperation(it);
        op.compile();
    }
    
    
    
    
    
    
    
    private SourceToken nextToken() throws CompilationException
    {
        SourceTokenBuilder sb = new SourceTokenBuilder();
        cline = reader.getCurrentLine();
        try
        {
            main_loop:
            for(;;)
            {
                char c = reader.next();
                OUTER:
                switch(c)
                {
                    case '\r': break;
                    case '\n':
                    case ' ':
                    case '\t':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        cline = reader.getCurrentLine();
                        break;
                    case '(':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        return SourceToken.PARENTHESIS_BEGIN;
                    case ')':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        return SourceToken.PARENTHESIS_END;
                    case '{':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        return SourceToken.BODY_BEGIN;
                    case '}':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        return SourceToken.BODY_END;
                    case ',':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        return SourceToken.COMMA;
                    case '/':
                        if(reader.canPeek(1))
                        {
                            c = reader.next();
                            switch(c)
                            {
                                case '*': {
                                    if(!sb.isEmpty())
                                    {
                                        reader.move(-1);
                                        break main_loop;
                                    }
                                    reader.seekOrEnd('*', '/');
                                } break;
                                case '/': {
                                    if(!sb.isEmpty())
                                    {
                                        reader.move(-1);
                                        break main_loop;
                                    }
                                    reader.seekOrEnd('\n');
                                } break;
                                default:
                                    reader.move(-2);
                                    error("Invalid token: '/" + c + "'");
                                    break;
                            }
                            break;
                        }
                        error("Invalid token: '" + c + "'");
                    case '=':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        if(reader.canPeek(1))
                        {
                            if(reader.peek(1) == '=')
                            {
                                reader.next();
                                return SourceToken.EQUAL_TO;
                            }
                            error("Invalid token: '" + c + reader.peek(1) + "'");
                        }
                        error("Invalid token: '" + c + "'");
                    case '!':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        if(reader.canPeek(1))
                        {
                            if(reader.peek(1) == '=')
                            {
                                reader.next();
                                return SourceToken.NOT_EQUAL_TO;
                            }
                            //error("Invalid token: '" + c + reader.peek(1) + "'");
                        }
                        return SourceToken.NEGATE;
                    case '>':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        if(reader.canPeek(1))
                        {
                            if(reader.peek(1) == '=')
                            {
                                reader.next();
                                return SourceToken.GREATER_THAN_EQUAL_TO;
                            }
                            //error("Invalid token: '" + c + reader.peek(1) + "'");
                        }
                        return SourceToken.GREATER_THAN;
                    case '<':
                        if(!sb.isEmpty())
                        {
                            reader.move(-1);
                            break main_loop;
                        }
                        if(reader.canPeek(1))
                        {
                            if(reader.peek(1) == '=')
                            {
                                reader.next();
                                return SourceToken.LESS_THAN_EQUAL_TO;
                            }
                            //error("Invalid token: '" + c + reader.peek(1) + "'");
                        }
                        return SourceToken.LESS_THAN;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }
        catch(EOFException ex) {}
        if(sb.isEmpty())
            return null;
        return sb.extract();
    }
    
    private boolean checkOneLineBody() throws CompilationException
    {
        if(!reader.hasNext())
            return false;
        int index = reader.getCurrentIndex();
        SourceToken token = nextToken();
        if(token == null)
            error("Unexpected end of file");
        if(token.equals(SourceToken.BODY_BEGIN))
            return false;
        reader.setIndex(index);
        return true;
    }
    
    private void checkNextToken(SourceToken token) throws CompilationException
    {
        SourceToken stoken = nextToken();
        if(stoken == null)
            error("Unexpected end of file");
        else if(!token.equals(stoken))
            error("Expected token '" + token + "' but found '" + stoken + "'");
    }
    
    private boolean checkHasElse() throws CompilationException
    {
        if(!reader.hasNext())
            return false;
        int index = reader.getCurrentIndex();
        SourceToken token = nextToken();
        if(token != null && token.toString().equals("else"))
            return true;
        reader.setIndex(index);
        return false;
    }
    
    private void error(String message) throws CompilationException
    {
        throw new CompilationException(reader, message);
    }
    
    private final class CodeManager
    {
        private final UInt16[] codes = new UInt16[ScriptConstant.MAX_CODES];
        private int it = 1;
        
        public final void add(UInt16 code) throws CompilationException
        {
            if(code == null)
                throw new NullPointerException();
            if(it + 1 >= ScriptConstant.MAX_CODES)
                error("Code count overflow");
            codes[it++] = code;
        }
        public final void add(Token token) throws CompilationException { add(token.getCode()); }
        public final void add(Internal internal) throws CompilationException { add(internal.getCode()); }
        public final void addField(SourceToken token) throws CompilationException
        {
            UInt16 fieldId = fields.register(token);
            if(fieldId == null)
                error("Expected valid field (variable, constant or internal). But found: " + token);
            else add(fieldId);
        }
        
        public final void fillScriptCode(Script script)
        {
            for(int i=1;i<codes.length;i++)
            {
                if(i < it)
                    script.codes.setCode(i, codes[i]);
                else script.codes.setCode(i, UInt16.ZERO);
            }
            script.codes.setVersion();
        }
    }
    
    private final class FieldManager
    {
        private final HashMap<Integer, UInt16> constants = new HashMap<>();
        private final HashMap<Internal, UInt16> internals = new HashMap<>();
        private final HashMap<String, UInt16> variables = new HashMap<>();
        private final LinkedList<Field> list = new LinkedList<>();
        
        private void checkSize() throws CompilationException
        {
            if(list.size() >= ScriptConstant.MAX_FIELDS)
                error("Field count overflow");
        }
        
        public final UInt16 register(SourceToken token) throws CompilationException
        {
            if(token.isValidConstant())
                return registerConstant(token);
            if(token.isValidVariable())
                return registerVariable(token);
            if(token.isValidInternal())
                return registerInternal(token);
            return null;
        }
        
        private UInt16 registerVariable(SourceToken token) throws CompilationException
        {
            if(variables.containsKey(token.toString()))
                return variables.get(token.toString());
            checkSize();
            if(variables.size() >= ScriptConstant.MAX_VARS)
                error("Variable count overflow");
            Field field = Field.user(SInt32.valueOf(variables.size()));
            UInt16 index = UInt16.valueOf(list.size());
            list.add(field);
            variables.put(token.toString(), index);
            return index;
        }
        
        private UInt16 registerConstant(SourceToken token) throws CompilationException
        {
            Integer value = token.toInteger();
            if(constants.containsKey(value))
                return constants.get(value);
            checkSize();
            Field field = Field.constant(SInt32.valueOf(value));
            UInt16 index = UInt16.valueOf(list.size());
            list.add(field);
            constants.put(value, index);
            return index;
        }
        
        private UInt16 registerInternal(SourceToken token) throws CompilationException
        {
            Internal in = token.toInternal();
            if(internals.containsKey(in))
                return internals.get(in);
            checkSize();
            Field field = Field.internal(SInt32.valueOf(in.getCode()));
            UInt16 index = UInt16.valueOf(list.size());
            list.add(field);
            internals.put(in, index);
            return index;
        }
        
        public final void fillScriptFields(Script script)
        {
            int count = 0;
            for(Field field : list)
                script.fields.setField(count++, field);
            for(; count < ScriptConstant.MAX_FIELDS; count++)
                script.fields.setField(count, Field.INVALID);
        }
    }
    
    private abstract class Operation
    {
        public abstract void compile() throws CompilationException;
    }
    
    private final class Operand extends Operation
    {
        private final UInt16 value;
        
        private Operand(UInt16 value) { this.value = Objects.requireNonNull(value); }

        @Override
        public void compile() throws CompilationException { codes.add(value); }
    }
    
    private final class Operator extends Operation
    {
        private final Token token;
        private final Operation op1;
        private final Operation op2;
        
        private Operator(Token token, Operation op1, Operation op2)
        {
            this.token = Objects.requireNonNull(token);
            this.op1 = Objects.requireNonNull(op1);
            this.op2 = Objects.requireNonNull(op2);
        }

        @Override
        public void compile() throws CompilationException
        {
            codes.add(token);
            op1.compile();
            op2.compile();
        }
    }
    
    private final class TokenIterator implements Iterator<SourceToken>
    {
        private final SourceToken[] tokens;
        private int it = 0;
        
        public TokenIterator(SourceToken[] tokens) { this.tokens = Objects.requireNonNull(tokens); }

        @Override
        public final boolean hasNext() { return it < tokens.length; }

        @Override
        public final SourceToken next() { return tokens[it++]; }
        
        public final TokenIterator extractUntil(SourceToken end) throws CompilationException
        {
            LinkedList<SourceToken> list = new LinkedList<>();
            for(; hasNext(); it++)
            {
                SourceToken token = tokens[it];
                if(token.equals(end))
                    return new TokenIterator(list.toArray(new SourceToken[list.size()]));
                list.add(token);
            }
            error("Expected '" + end + "'");
            return null;
        }
        
        public final TokenIterator sub(int start, int end)
        {
            SourceToken[] array = new SourceToken[end - start];
            System.arraycopy(tokens, start, array, 0, array.length);
            return new TokenIterator(array);
        }
        
        public final int length() { return tokens.length; }
    }
    
    private Operator unaryOperator(Operation op, boolean negate) throws CompilationException
    {
        Token token = negate ? Token.EQUAL_TO : Token.NOT_EQUAL_TO;
        UInt16 index = fields.registerConstant(SourceToken.ZERO);
        return new Operator(token, op, new Operand(index));
    }
    //private Operator unaryOperator(UInt16 index, boolean negate) throws CompilationException { return unaryOperator(new Operand(index), negate); }
    
    private Operation fieldOperand(SourceToken token) throws CompilationException
    {
        UInt16 index = fields.register(token);
        if(index == null)
            error("Expected valid field (variable, constant or internal). But found: " + token);
        return new Operand(fields.register(token));
    }
    
    private Operation parseOperand(SourceToken token, TokenIterator it) throws CompilationException
    {
        switch(token.toString())
        {
            case "(": {
                TokenIterator it2 = it.extractUntil(SourceToken.PARENTHESIS_END);
                return generateOperation(it2);
            }
            case "!": {
                token = it.next();
                return unaryOperator(fieldOperand(token), true);
            }
            default: return fieldOperand(token);
        }
    }
    
    private Operation generateOperation(TokenIterator it) throws CompilationException
    {
        if(!it.hasNext())
            throw new IllegalStateException();
        SourceToken op1 = it.next();
        if(!it.hasNext())
        {
            if(it.length() == 1)
                return unaryOperator(fieldOperand(op1), false);
            return fieldOperand(op1);
        }
        return subGenerateOperation(parseOperand(op1, it), it);
    }
    
    private Operation generatePart(TokenIterator it) throws CompilationException
    {
        if(!it.hasNext())
            throw new IllegalStateException();
        SourceToken op1 = it.next();
        return parseOperand(op1, it);
    }
    
    private Operator subGenerateOperation(Operation op1, TokenIterator it) throws CompilationException
    {
        SourceToken op = it.next();
        Token opToken = checkValidOperatorToken(op);
        
        Operation op2 = generateOperationPart(opToken, it);
        Operator operator = new Operator(opToken, op1, op2);
        
        if(!it.hasNext())
            return operator;
        return subGenerateOperation(operator, it);
    }
    
    private Operation generateOperationPart(Token op, TokenIterator it) throws CompilationException
    {
        Token nextOperator = findNextOperatorToken(it);
        if(nextOperator != null && operatorPriority(op) >= operatorPriority(nextOperator))
            nextOperator = null;
        
        Operation op2;
        if(nextOperator != null)
            op2 = generateSuperOperatorScope(op, it);
        else op2 = generatePart(it);
        
        return op2;
    }
    
    private Token findNextOperatorToken(TokenIterator it) throws CompilationException
    {
        for(int i=it.it;i<it.length();i++)
            if(isValidOperatorToken(it.tokens[i]) != null)
                return isValidOperatorToken(it.tokens[i]);
        return null;
    }
    
    private Operation generateSuperOperatorScope(Token opBase, TokenIterator it) throws CompilationException
    {
        int start = it.it;
        for(; it.hasNext(); it.it++)
        {
            if(isValidOperatorToken(it.tokens[it.it]) == null)
                continue;
            Token op = isValidOperatorToken(it.tokens[it.it]);
            if(operatorPriority(opBase) > operatorPriority(op))
                return generateOperation(it.sub(start, it.it));
        }
        it.it = start;
        return generateOperation(it);
    }
    
    private Token checkValidOperatorToken(SourceToken op) throws CompilationException
    {
        Token token = isValidOperatorToken(op);
        if(token == null)
            error("Expected a valid condition operator (==, !=, >, >=, <, <=, &&, ||)");
        return token;
    }
    
    private Token isValidOperatorToken(SourceToken op) throws CompilationException
    {
        switch(op.toString())
        {
            case "==": return Token.EQUAL_TO;
            case "!=": return Token.NOT_EQUAL_TO;
            case ">": return Token.GREATER_THAN;
            case ">=": return Token.GREATER_THAN_EQUAL_TO;
            case "<": return Token.LESS_THAN;
            case "<=": return Token.LESS_THAN_EQUAL_TO;
            case "&&": return Token.AND;
            case "||": return Token.OR;
            default: return null;
        }
    }
    
    private static int operatorPriority(Token op)
    {
        if(op == null)
            throw new NullPointerException();
        switch(op)
        {
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL_TO:
            case LESS_THAN:
            case LESS_THAN_EQUAL_TO:
                return 2;
            case AND: return 1;
            case OR: return 0;
            default: throw new IllegalStateException();
        }
    }
}
