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
package at.or.reder.tools.fieldtest.model;

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
  DEFAULT_PWM(103),
  FW_VERSION(120);
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
