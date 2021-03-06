/*
 * Copyright 2020 wolfi.
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
package at.or.reder.rpi.mapping;

import at.or.reder.rpi.field.ModuleType;
import at.or.reder.rpi.field.TurnoutState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tournout")
public class TurnoutMapping extends FieldMapping
{

  public static class TurnoutStatePair
  {

    private TurnoutState state;
    private int portValue;

    public TurnoutStatePair()
    {
    }

    public TurnoutStatePair(Map.Entry<TurnoutState, Integer> e)
    {
      this.state = e.getKey();
      this.portValue = e.getValue();
    }

    @XmlAttribute(name = "state")
    public TurnoutState getState()
    {
      return state;
    }

    public void setState(TurnoutState state)
    {
      this.state = state;
    }

    @XmlAttribute(name = "value")
    public int getPortValue()
    {
      return portValue;
    }

    public void setPortValue(int portValue)
    {
      this.portValue = portValue;
    }

  }

  private final List<TurnoutStatePair> values = new ArrayList<>();

  public Map<TurnoutState, Integer> getMappings()
  {
    return values.stream().
            filter((e) -> e != null && e.state != null).
            collect(Collectors.toMap(TurnoutStatePair::getState,
                                     TurnoutStatePair::getPortValue));
  }

  public void setMappings(Map<TurnoutState, Integer> m)
  {
    values.clear();
    m.entrySet().
            stream().
            map(TurnoutStatePair::new).
            forEach(values::add);
  }

  @Override
  public void setType(ModuleType type)
  {
    switch (type) {
      case W1:
      case W2:
      case W3:
      case W4:
        super.setType(type);
        break;
      default:
        super.setType(ModuleType.W1);
    }
  }

  @XmlElement(name = "mapping",
              type = TurnoutMapping.TurnoutStatePair.class)
  public List<TurnoutStatePair> getMappingLists()
  {
    return values;
  }

}
