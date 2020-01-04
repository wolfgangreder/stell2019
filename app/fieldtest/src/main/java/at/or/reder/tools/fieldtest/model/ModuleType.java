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

public enum ModuleType
{
  UNKNOWN(0),
  G1(1),
  D1(2),
  B1(3),
  B2(4),
  F1(0x81),
  F2(0x82),
  F3(0x83),
  F4(0x84),
  W1(0xc1),
  W2(0xc2),
  W3(0xc3),
  W4(0xc4),
  K1(0xc5),
  K2(0xc6),
  DW1(0xc7),
  DW2(0xc8),
  DW3(0xc9),
  SEM_E(0xca),
  SEM_W(0xcb),
  UNDEFINED(0xff);

  public static final int MASK_FUNCTION = 0x80;
  public static final int MASK_STATEFUL = 0x40;

  private final int magic;

  private ModuleType(int m)
  {
    this.magic = m;
  }

  public int getMagic()
  {
    return magic;
  }

  public boolean isFunction()
  {
    return (magic & MASK_FUNCTION) != 0;
  }

  public boolean isStateful()
  {
    return (magic & MASK_STATEFUL) != 0;
  }

  public static ModuleType valueOfMagic(int m)
  {
    for (ModuleType t : values()) {
      if (t.getMagic() == (m & 0xff)) {
        return t;
      }
    }
    return ModuleType.UNDEFINED;
  }

}
