/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.CompilationError;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public interface Operand extends SourceToken
{
    void resolve(CodePool code, FieldPool fields, Environment env) throws CompilationError;
    
    public enum Environment
    {
        SUPERFICIAL,
        DEEP,
        COND_SUPERFICIAL,
        COND_DEEP;
        
        public final boolean isSuperficial() { return this == SUPERFICIAL || this == COND_SUPERFICIAL; }
        public final boolean isDeep() { return this == DEEP || this == COND_DEEP; }
        public final boolean isConditional() { return this == COND_SUPERFICIAL || this == COND_DEEP; }
    }
}
