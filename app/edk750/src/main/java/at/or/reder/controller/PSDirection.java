/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller;

public enum PSDirection
{
  UNKNOWN(false,
          false,
          false,
          false),
  NORTH(true,
        false,
        false,
        false),
  NORTH_EAST(true,
             true,
             false,
             false),
  EAST(false,
       true,
       false,
       false),
  SOUTH_EAST(false,
             true,
             true,
             false),
  SOUTH(false,
        false,
        true,
        false),
  SOUTH_WEST(false,
             false,
             true,
             true),
  WEST(false,
       false,
       false,
       true),
  NORTH_WEST(true,
             false,
             false,
             true);
  private final boolean n;
  private final boolean e;
  private final boolean s;
  private final boolean w;

  private PSDirection(boolean n,
                      boolean e,
                      boolean s,
                      boolean w)
  {
    this.n = n;
    this.e = e;
    this.s = s;
    this.w = w;
  }

  public boolean isNorth()
  {
    return n;
  }

  public boolean isEast()
  {
    return e;
  }

  public boolean isSouth()
  {
    return s;
  }

  public boolean isWest()
  {
    return w;
  }

}
