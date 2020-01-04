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

import java.util.Objects;

/**
 *
 * @author Wolfgang Reder
 */
public final class ModuleStateTurnout implements ModuleState
{

  private final ModuleType module;
  private final LedState ledState;
  private final TurnoutState state;

  public ModuleStateTurnout(ModuleType module,
                            LedState ledState,
                            TurnoutState turnoutState)
  {
    switch (Objects.requireNonNull(module,
                                   "module is null")) {
      case W1:
      case W2:
      case W3:
      case W4:
        break;
      default:
        throw new IllegalArgumentException("module is not a simple turnout");
    }
    this.module = module;
    this.ledState = Objects.requireNonNull(ledState,
                                           "ledState is null");
    this.state = Objects.requireNonNull(turnoutState,
                                        "tournoutState is null");
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

  public TurnoutState getState()
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
    hash = 29 * hash + Objects.hashCode(this.module);
    hash = 29 * hash + Objects.hashCode(this.ledState);
    hash = 29 * hash + Objects.hashCode(this.state);
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
    final ModuleStateTurnout other = (ModuleStateTurnout) obj;
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
