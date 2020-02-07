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

import at.or.reder.rpi.field.ModuleType;

public class TrackMapping extends FieldMapping
{

  @Override
  public void setType(ModuleType type)
  {
    switch (type) {
      case B1:
      case B2:
      case G1:
        super.setType(type);
        break;
      default:
        super.setType(ModuleType.G1);
    }
  }

}
