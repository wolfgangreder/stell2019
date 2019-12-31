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
public enum Register
{
  STATE(0),
  LED(1),
  BLINK_MASK(2),
  BLINK_PHASE(3),
  PWM(4),
  MODULE_STATE(5),
  VCC(6),
  BLINK_DIVIDER(8),
  ADDRESS(96),
  MODULE_TYPE(98),
  DEBOUNCE(99),
  SOFTSTART(100),
  SOFTSTOP(101),
  VCC_CALIBRATION(102),
  FW_VERSION(120),
  FW_BUILD(122);
  private final byte index;

  private Register(int i)
  {
    index = (byte) i;
  }

  public byte getIndex()
  {
    return index;
  }

}
