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
package at.or.reder.tools.fieldtest;

import java.util.Objects;

/**
 *
 * @author Wolfgang Reder
 */
public final class ModuleStateTrack implements ModuleState
{

  private final ModuleType module;
  private final LedState state;

  public ModuleStateTrack(ModuleType m,
                          LedState s)
  {
    if (Objects.requireNonNull(m,
                               "moduletype is null").isStateful()) {
      throw new IllegalArgumentException("Module is not a track");
    }
    module = m;
    state = Objects.requireNonNull(s,
                                   "state is null");
  }

  @Override
  public ModuleType getModuleType()
  {
    return module;
  }

  @Override
  public LedState getLedState()
  {
    return state;
  }

  @Override
  public int getMagic()
  {
    return (state.getMagic()) + (module.getMagic() << 8);
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(this.module);
    hash = 31 * hash + Objects.hashCode(this.state);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ModuleStateTrack other = (ModuleStateTrack) obj;
    if (this.module != other.module) {
      return false;
    }
    if (this.state != other.state) {
      return false;
    }
    return true;
  }

}
