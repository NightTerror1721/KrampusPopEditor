/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.utils;

/**
 *
 * @author Asus
 */
public interface TokenizerExtractor
{
    public static final char EOF = '\uffff';
    public static final char EOL = '\n';
    
    char nextChar();
    char peekChar(int idx);
    
    default char peekChar() { return peekChar(0); }
}
