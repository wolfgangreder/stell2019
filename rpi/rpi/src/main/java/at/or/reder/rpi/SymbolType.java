/*
 * Copyright 2019 Wolfgang Reder.
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
package at.or.reder.rpi;

import eu.hansolo.steelseries.tools.LedColor;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Wolfgang Reder
 */
public enum SymbolType
{
  EMPTY(null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.emptyList()),
  G1(null,
     null,
     null,
     LedColor.YELLOW_LED,
     LedColor.YELLOW_LED,
     null,
     null,
     LedColor.YELLOW_LED,
     null,
     Arrays.asList(4,
                   5)),
  D1(null,
     null,
     LedColor.YELLOW_LED,
     null,
     null,
     LedColor.YELLOW_LED,
     null,
     LedColor.YELLOW_LED,
     null,
     Arrays.asList(3,
                   6)),
  B1(LedColor.YELLOW_LED,
     null,
     null,
     null,
     LedColor.YELLOW_LED,
     null,
     null,
     LedColor.YELLOW_LED,
     null,
     Arrays.asList(1,
                   5));

  private final Map<Integer, LedColor> visibleLeds;
  private final Color buttonColor;
  private final Set<Integer> lines;

  private SymbolType(LedColor led1,
                     LedColor led2,
                     LedColor led3,
                     LedColor led4,
                     LedColor led5,
                     LedColor led6,
                     LedColor led8,
                     LedColor led9,
                     Color buttonColor,
                     Collection<Integer> lines)
  {
    this.lines = Collections.unmodifiableSet(new HashSet<>(lines));
    Map<Integer, LedColor> map = new HashMap<>();
    if (led1 != null) {
      map.put(1,
              led1);
    }
    if (led2 != null) {
      map.put(2,
              led2);
    }
    if (led3 != null) {
      map.put(3,
              led3);
    }
    if (led4 != null) {
      map.put(4,
              led4);
    }
    if (led5 != null) {
      map.put(5,
              led5);
    }
    if (led6 != null) {
      map.put(6,
              led6);
    }
    if (led8 != null) {
      map.put(8,
              led8);
    }
    if (led9 != null) {
      map.put(9,
              led9);
    }
    this.visibleLeds = Collections.unmodifiableMap(map);
    this.buttonColor = buttonColor;
  }

  public Map<Integer, LedColor> getVisibleLeds()
  {
    return visibleLeds;
  }

  public Color getButtonColor()
  {
    return buttonColor;
  }

  public Set<Integer> getLines()
  {
    return lines;
  }

}
