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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "layout")
public class Layout
{

  private final List<FieldMapping> mappings = new ArrayList<>();

  @XmlElementWrapper(name = "fields")
  @XmlElements({
    @XmlElement(type = TurnoutMapping.class, name = "turnout"),
    @XmlElement(type = TrackMapping.class, name = "track")})
  public List<FieldMapping> getMappings()
  {
    return mappings;
  }

}
