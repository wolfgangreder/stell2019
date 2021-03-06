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

import java.util.Collection;
import java.util.EnumSet;

public enum State
{
  KEY_PRESSED(0x01),
  BO_ERROR(0x40),
  WDT_ERROR(0x80);
  private final int magic;

  private State(int magic)
  {
    this.magic = magic;
  }

  public int getMagic()
  {
    return magic;
  }

  public static EnumSet<State> bitfieldToSet(int field)
  {
    EnumSet<State> result = EnumSet.noneOf(State.class);
    for (State s : values()) {
      if ((s.getMagic() & field) != 0) {
        result.add(s);
      }
    }
    return result;
  }

  public static int setToBitfield(Collection<State> states)
  {
    int result = 0;
    if (states != null) {
      for (State s : states) {
        result += s.getMagic();
      }
    }
    return result;
  }

}
