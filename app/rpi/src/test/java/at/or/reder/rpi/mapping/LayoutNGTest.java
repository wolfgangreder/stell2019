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
package at.or.reder.rpi.mapping;

import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Wolfgang Reder
 */
public class LayoutNGTest
{

  private static JAXBContext jaxb;

  public LayoutNGTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
    jaxb = JAXBContext.newInstance(Layout.class,
                                   TrackMapping.class,
                                   TurnoutMapping.class);
  }

  @Test
  public void loadA() throws Exception
  {
    Unmarshaller um = jaxb.createUnmarshaller();
    Object o;
    try (InputStream is = getClass().getResourceAsStream("a.xml")) {
      o = um.unmarshal(is);
    }
    assertTrue(o instanceof Layout);
    Layout l = (Layout) o;
    List<FieldMapping> mappings = l.getMappings();
    assertEquals(11,
                 mappings.size());
  }

}
