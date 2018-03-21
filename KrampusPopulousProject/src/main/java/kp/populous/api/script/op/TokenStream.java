/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script.op;

/**
 *
 * @author Asus
 */
public interface TokenStream
{
    String nextToken();
    boolean hasMoreTokens();
}
