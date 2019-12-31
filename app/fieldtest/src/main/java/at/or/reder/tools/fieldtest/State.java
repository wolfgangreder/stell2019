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
public enum State
{
  KEY_PRESSED(0x01),
  KEY_ERROR(0x80);
  private final int magic;

  private State(int magic)
  {
    this.magic = magic;
  }

  public int getMagic()
  {
    return magic;
  }

}
