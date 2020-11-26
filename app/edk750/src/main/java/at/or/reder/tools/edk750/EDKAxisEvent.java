/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import java.util.EventObject;

/**
 *
 * @author Wolfgang Reder
 */
public final class EDKAxisEvent extends EventObject
{

  private final EDKAxis axis;
  private final float value;

  public EDKAxisEvent(EDK750 source,
                      EDKAxis axis,
                      float value)
  {
    super(source);
    this.axis = axis;
    this.value = value;
  }

  @Override
  public EDK750 getSource()
  {
    return (EDK750) super.source;
  }

  public EDKAxis getAxis()
  {
    return axis;
  }

  public float getValue()
  {
    return value;
  }

}
