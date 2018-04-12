/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

/**
 *
 * @author Asus
 */
public final class InstructionId implements UnparsedOperand
{
    private InstructionId() {}
    
    @Override
    public TokenType getTokenType() { return TokenType.INSTRUCTION_ID; }
    
    public static final InstructionId
            OPERATION = new InstructionId(),
            SET = new InstructionId(),
            INC = new InstructionId(),
            DEC = new InstructionId(),
            MUL = new InstructionId(),
            DIV = new InstructionId(),
            IF = new InstructionId(),
            ELSE = new InstructionId(),
            EVERY = new InstructionId(),
            SCOPE = new InstructionId();
}
