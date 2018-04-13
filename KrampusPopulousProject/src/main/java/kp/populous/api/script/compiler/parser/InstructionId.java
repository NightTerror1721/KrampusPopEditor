/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public final class InstructionId implements UnparsedOperand
{
    private final String name;
    
    private InstructionId(String name) { this.name = name; }
    
    @Override
    public TokenType getTokenType() { return TokenType.INSTRUCTION_ID; }
    
    @Override
    public final String toString() { return name; }
    
    @Override
    public final boolean equals(Object o)
    {
        if(o instanceof InstructionId)
        {
            InstructionId ii = (InstructionId) o;
            return name.equals(ii.name);
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    public static final InstructionId
            OPERATION = new InstructionId("<operation>"),
            SET = new InstructionId("<set>"),
            INC = new InstructionId("<inc>"),
            DEC = new InstructionId("<dec>"),
            MUL = new InstructionId("<mul>"),
            DIV = new InstructionId("<div>"),
            IF = new InstructionId("<if>"),
            ELSE = new InstructionId("<else>"),
            EVERY = new InstructionId("<every>"),
            SCOPE = new InstructionId("<scope>");
}
