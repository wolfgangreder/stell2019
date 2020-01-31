/*
 * Copyright 2020 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.or.reder.rpi.field;

public enum Register
{
  STATE(0),
  LED(1),
  BLINK_MASK(2),
  BLINK_PHASE(3),
  PWM(4),
  MODULE_STATE(5),
  VCC(6),
  BLINK_DIVIDER(7),
  ADDRESS_MSB(8),
  ADDRESS_LSB(9),
  MODULE_TYPE(10),
  DEBOUNCE(11),
  VCC_CALIBRATION(12),
  DEFAULT_PWM(13),
  FW_VERSION(14),
  REG_VCC_REFERENCE(15),
  REG_TWI_BAUD(16),
  REG_BLINK_COUNTER_H(17),
  REG_BLINK_COUNTER_L(18),
  REG_BLINK_PRESCALE(19);
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
