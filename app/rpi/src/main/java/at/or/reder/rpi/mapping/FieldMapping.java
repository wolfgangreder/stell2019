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
import javax.xml.bind.annotation.XmlAttribute;

public abstract class FieldMapping
{

  private ModuleType type;
  private String label;
  private int decoderAddress;
  private int fieldAddress;
  private int port;

  @XmlAttribute(name = "type")
  public ModuleType getType()
  {
    return type;
  }

  public void setType(ModuleType type)
  {
    this.type = type;
  }

  @XmlAttribute(name = "label")
  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  @XmlAttribute(name = "decoder")
  public int getDecoderAddress()
  {
    return decoderAddress;
  }

  public void setDecoderAddress(int decoderAddress)
  {
    this.decoderAddress = decoderAddress;
  }

  @XmlAttribute(name = "field")
  public int getFieldAddress()
  {
    return fieldAddress;
  }

  public void setFieldAddress(int fieldAddress)
  {
    this.fieldAddress = fieldAddress;
  }

  @XmlAttribute(name = "port")
  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }

}
