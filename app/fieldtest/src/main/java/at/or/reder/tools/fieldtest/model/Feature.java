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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Wolfgang Reder
 */
public enum Feature
{
  BLINK_GENERATOR(0x01);
  private final int magic;

  private Feature(int m)
  {
    magic = m;
  }

  public int getMagic()
  {
    return magic;
  }

  public static Set<Feature> valuesOfMagic(int magic)
  {
    EnumSet<Feature> result = EnumSet.noneOf(Feature.class);
    for (Feature f : values()) {
      if ((f.getMagic() & magic) != 0) {
        result.add(f);
      }
    }
    return result;
  }

  public static int magicOfValues(Collection<Feature> features)
  {
    int result = 0;
    for (Feature f : features) {
      result |= f.getMagic();
    }
    return result;
  }

}
