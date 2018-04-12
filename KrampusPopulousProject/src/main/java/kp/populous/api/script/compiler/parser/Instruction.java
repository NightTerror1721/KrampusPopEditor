/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.compiler.parser;

import kp.populous.api.script.compiler.CodePool;
import kp.populous.api.script.compiler.FieldPool;

/**
 *
 * @author Asus
 */
public abstract class Instruction
{
    public abstract InstructionId getInstructionId();
    
    public abstract void compile(CodePool code, FieldPool fields);
}
