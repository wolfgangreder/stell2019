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

/**
 *
 * @author Wolfgang Reder
 */
public enum LedState
{
  OFF(0),
  BLINK(1),
  ON(2),
  LAMP_TEST(3),
  UNKNOWN(0xff);
  private final int magic;

  private LedState(int magic)
  {
    this.magic = magic;
  }

  public int getMagic()
  {
    return magic;
  }

  public static LedState valueOfMagic(int magic)
  {
    for (LedState s : values()) {
      if (s.magic == (magic & 0xff)) {
        return s;
      }
    }
    return LedState.UNKNOWN;
  }

}
