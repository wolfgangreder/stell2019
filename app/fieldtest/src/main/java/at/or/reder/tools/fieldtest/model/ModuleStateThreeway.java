package at.or.reder.tools.fieldtest.model;

import java.util.Objects;

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
/**
 *
 * @author Wolfgang Reder
 */
public final class ModuleStateThreeway implements ModuleState
{

  private final ModuleType module;
  private final LedState ledState;
  private final ThreewayState state;

  public ModuleStateThreeway(ModuleType module,
                             LedState ledState,
                             ThreewayState state)
  {
    switch (Objects.requireNonNull(module,
                                   "module is null")) {
      case DW1:
      case DW2:
      case DW3:
        break;
      default:
        throw new IllegalArgumentException("module is not a threeway turnout");
    }
    this.module = module;
    this.ledState = Objects.requireNonNull(ledState,
                                           "ledState is null");
    this.state = Objects.requireNonNull(state,
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
    return ledState;
  }

  public ThreewayState getState()
  {
    return state;
  }

  @Override
  public int getMagic()
  {
    return (state.getMagic() << 4) + (ledState.getMagic()) + (module.getMagic() << 8);
  }

  @Override
  public int getStateMagic()
  {
    return state.getMagic();
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 71 * hash + Objects.hashCode(this.module);
    hash = 71 * hash + Objects.hashCode(this.ledState);
    hash = 71 * hash + Objects.hashCode(this.state);
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
    final ModuleStateThreeway other = (ModuleStateThreeway) obj;
    if (this.module != other.module) {
      return false;
    }
    if (this.ledState != other.ledState) {
      return false;
    }
    if (this.state != other.state) {
      return false;
    }
    return true;
  }

}
