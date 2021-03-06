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
package at.or.reder.rpi.field;

public interface ModuleState
{

  public static int moduleTypeMagic(int magic)
  {
    return (magic & 0xff00) >> 8;
  }

  public static int ledStateMagic(int magic)
  {
    return magic & 0xf;
  }

  public static int moduleStateMagic(int magic)
  {
    return (magic & 0xf0) >> 4;
  }

  public static ModuleState valueOf(ModuleType type,
                                    int stateMagic)
  {
    LedState ls = LedState.valueOfMagic(ledStateMagic(stateMagic));
    int ms = moduleStateMagic(stateMagic);
    if (type != null && ls != null) {
      return valueOf(type,
                     ls,
                     ms);
    }
    return null;
  }

  public static ModuleState valueOf(ModuleType type,
                                    LedState ls,
                                    int ms)
  {
    if (type == ModuleType.UNKNOWN) {
      throw new IllegalArgumentException("Unknown Module type");
    }
    if (type == ModuleType.UNDEFINED) {
      return null;
    }
    if (type == null) {
      type = ModuleType.K2;
    }
    switch (type) {
      case B1:
        return new ModuleStateTrack(type,
                                    ls);
      case B2:
        return new ModuleStateTrack(type,
                                    ls);
      case D1:
        return new ModuleStateTrack(type,
                                    ls);
      case DW1:
        return new ModuleStateThreeway(type,
                                       ls,
                                       ThreewayState.valueOfMagic(ms));
      case DW2:
        return new ModuleStateThreeway(type,
                                       ls,
                                       ThreewayState.valueOfMagic(ms));
      case DW3:
        return new ModuleStateThreeway(type,
                                       ls,
                                       ThreewayState.valueOfMagic(ms));
      case F1:
        return new ModuleStateTrack(type,
                                    ls);
      case F2:
        return new ModuleStateTrack(type,
                                    ls);
      case F3:
        return new ModuleStateTrack(type,
                                    ls);
      case F4:
        return new ModuleStateTrack(type,
                                    ls);
      case G1:
        return new ModuleStateTrack(type,
                                    ls);
      case K1:
        return new ModuleStateCrossing(type,
                                       ls,
                                       CrossingState.valueOfMagic(ms));
      case K2:
        return new ModuleStateCrossing(type,
                                       ls,
                                       CrossingState.valueOfMagic(ms));
      case SEM_E:
        return new ModuleStateSemaphore(type,
                                        ls,
                                        SemaphoreState.valueOfMagic(ms));
      case SEM_W:
        return new ModuleStateSemaphore(type,
                                        ls,
                                        SemaphoreState.valueOfMagic(ms));
      case W1:
        return new ModuleStateTurnout(type,
                                      ls,
                                      TurnoutState.valueOfMagic(ms));
      case W2:
        return new ModuleStateTurnout(type,
                                      ls,
                                      TurnoutState.valueOfMagic(ms));
      case W3:
        return new ModuleStateTurnout(type,
                                      ls,
                                      TurnoutState.valueOfMagic(ms));
      case W4:
        return new ModuleStateTurnout(type,
                                      ls,
                                      TurnoutState.valueOfMagic(ms));
      default:
        throw new IllegalArgumentException("Unknown Module type");
    }
  }

  public static ModuleState valueOf(int magic)
  {
    if (magic != -1) {
      ModuleType type = ModuleType.valueOfMagic(moduleTypeMagic(magic));
      int ls = ledStateMagic(magic);
      int ms = moduleStateMagic(magic);
      if (type == ModuleType.UNKNOWN) {
        throw new IllegalArgumentException("Unknown Module type");
      }
      if (type == ModuleType.UNDEFINED) {
        return null;
      }
      switch (type) {
        case B1:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case B2:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case D1:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case DW1:
          return new ModuleStateThreeway(type,
                                         LedState.valueOfMagic(ls),
                                         ThreewayState.valueOfMagic(ms));
        case DW2:
          return new ModuleStateThreeway(type,
                                         LedState.valueOfMagic(ls),
                                         ThreewayState.valueOfMagic(ms));
        case DW3:
          return new ModuleStateThreeway(type,
                                         LedState.valueOfMagic(ls),
                                         ThreewayState.valueOfMagic(ms));
        case F1:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case F2:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case F3:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case F4:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case G1:
          return new ModuleStateTrack(type,
                                      LedState.valueOfMagic(ls));
        case K1:
          return new ModuleStateCrossing(type,
                                         LedState.valueOfMagic(ls),
                                         CrossingState.valueOfMagic(ms));
        case K2:
          return new ModuleStateCrossing(type,
                                         LedState.valueOfMagic(ls),
                                         CrossingState.valueOfMagic(ms));
        case SEM_E:
          return new ModuleStateSemaphore(type,
                                          LedState.valueOfMagic(ls),
                                          SemaphoreState.valueOfMagic(ms));
        case SEM_W:
          return new ModuleStateSemaphore(type,
                                          LedState.valueOfMagic(ls),
                                          SemaphoreState.valueOfMagic(ms));
        case W1:
          return new ModuleStateTurnout(type,
                                        LedState.valueOfMagic(ls),
                                        TurnoutState.valueOfMagic(ms));
        case W2:
          return new ModuleStateTurnout(type,
                                        LedState.valueOfMagic(ls),
                                        TurnoutState.valueOfMagic(ms));
        case W3:
          return new ModuleStateTurnout(type,
                                        LedState.valueOfMagic(ls),
                                        TurnoutState.valueOfMagic(ms));
        case W4:
          return new ModuleStateTurnout(type,
                                        LedState.valueOfMagic(ls),
                                        TurnoutState.valueOfMagic(ms));
        default:
          throw new IllegalArgumentException("Unknown Module type");
      }
    }
    throw new IllegalArgumentException("Cannot read Module state");
  }

  public ModuleType getModuleType();

  public LedState getLedState();

  public int getMagic();

  public int getStateMagic();

}
