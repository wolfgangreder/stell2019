/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

/**
 *
 * @author Wolfgang Reder
 */
public class Command
{

  private final byte[] buffer;

  public Command(byte[] buffer)
  {
    this.buffer = buffer;
  }

  public byte[] getBuffer()
  {
    return buffer;
  }

}
