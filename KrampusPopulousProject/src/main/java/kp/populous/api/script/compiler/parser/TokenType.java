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
public enum TokenType
{
    CONSTANT,
    INTERNAL,
    FUNCTION,
    VARIABLE,
    SPECIAL_TOKEN,
    PARENTHESIS,
    ARGUMENT_LIST,
    CONTROL,
    STOP_CHAR,
    INSTRUCTION_ID,
    OPERATOR_SYMBOL,
    OPERATOR;
}
