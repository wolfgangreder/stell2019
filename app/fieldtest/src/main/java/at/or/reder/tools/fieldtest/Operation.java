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
public enum Operation
{
  READ(0),
  WRITE(1),
  INCREMENT(2),
  DECREMENT(3),
  COMPLEMENT(4);
  private final byte magic;

  private Operation(int m)
  {
    this.magic = (byte) m;
  }

  public byte getMagic()
  {
    return magic;
  }

}
