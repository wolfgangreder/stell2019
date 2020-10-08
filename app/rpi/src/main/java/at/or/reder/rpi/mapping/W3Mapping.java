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
import at.or.reder.rpi.field.ThreewayState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "w3")
public class W3Mapping extends FieldMapping
{

  public static class W3StatePair
  {

    private ThreewayState state;
    private int portValue;

    public W3StatePair()
    {
    }

    public W3StatePair(Map.Entry<ThreewayState, Integer> e)
    {
      this.state = e.getKey();
      this.portValue = e.getValue();
    }

    @XmlAttribute(name = "state")
    public ThreewayState getState()
    {
      return state;
    }

    public void setState(ThreewayState state)
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

  private int decoderAddress2;
  private int port2;

  @XmlAttribute(name = "decoder2")
  public int getDecoderAddress2()
  {
    return decoderAddress2;
  }

  public void setDecoderAddress2(int decoderAddress2)
  {
    this.decoderAddress2 = decoderAddress2;
  }

  @XmlAttribute(name = "port2")
  public int getPort2()
  {
    return port2;
  }

  public void setPort2(int port2)
  {
    this.port2 = port2;
  }

  private final List<W3StatePair> values = new ArrayList<>();

  public Map<ThreewayState, Integer> getMappings()
  {
    return values.stream().
            filter((e) -> e != null && e.state != null).
            collect(Collectors.toMap(W3StatePair::getState,
                                     W3StatePair::getPortValue));
  }

  public void setMappings(Map<ThreewayState, Integer> m)
  {
    values.clear();
    m.entrySet().
            stream().
            map(W3StatePair::new).
            forEach(values::add);
  }

  @Override
  public void setType(ModuleType type)
  {
    switch (type) {
      case DW1:
      case DW2:
      case DW3:
        super.setType(type);
        break;
      default:
        super.setType(ModuleType.DW1);
    }
  }

  @XmlElement(name = "mapping",
              type = W3Mapping.W3StatePair.class)
  public List<W3StatePair> getMappingLists()
  {
    return values;
  }
}
