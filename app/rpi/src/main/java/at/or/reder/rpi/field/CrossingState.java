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

public enum CrossingState
{
  STRAIT_0(0),
  DEFLECTION_0(1),
  STRAIT_1(2),
  DEFLECTION_1(3),
  UNKNOWN(0xff);

  private final int magic;

  private CrossingState(int magic)
  {
    this.magic = magic;
  }

  public int getMagic()
  {
    return magic;
  }

  public static CrossingState valueOfMagic(int magic)
  {
    for (CrossingState s : values()) {
      if (s.magic == (magic & 0xff)) {
        return s;
      }
    }
    return UNKNOWN;
  }

}
