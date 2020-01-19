/*
 * File:   symbols.h
 * Author: wolfi
 *
 * Created on 01. Jänner 2020, 11:45
 */

#ifndef SYMBOLS_H
#define	SYMBOLS_H

#include "config.h"

#ifdef	__cplusplus
extern "C" {
#endif

  typedef enum {
    UNKNOWN = 0,
    G1 = 1,
    D1 = 2,
    B1 = 3,
    B2 = 4,
    F1 = 0x81,
    F2 = 0x82,
    F3 = 0x83,
    F4 = 0x84,
    W1 = 0xc1,
    W2 = 0xc2,
    W3 = 0xc3,
    W4 = 0xc4,
    K1 = 0xc5,
    K2 = 0xc6,
    DW1 = 0xc7,
    DW2 = 0xc8,
    DW3 = 0xc9,
    SEM_E = 0xca,
    SEM_W = 0xcb,
    UNDEFINED = 0xff
  } module_t;

#define IS_FUNCTION_MODULE(s) ((s)&0x80)
#define IS_STATEFUL_MODULE(S) ((s)&0x40)

  typedef enum {
    LED_OFF = 0x0,
    LED_BLINK = 0x01,
    LED_ON = 0x02,
    LED_LAMPTEST = 0x3
  } state_led_t;

  typedef enum {
    T_STRAIT = 0x0,
    T_DEFLECTION = 0x1
  } state_t_t;

  typedef enum {
    K_STRAIT_0 = 0x0,
    K_DEFLECTION_0 = 0x1,
    K_STRAIT_1 = 0x2,
    K_DEFLECTION_1 = 0x3
  } state_k_t;

  typedef enum {
    D_STRAIT = 0x0,
    D_DEFLECTION_0 = 0x1,
    D_DEFLECTION_1 = 0x2
  } state_dw_t;

  typedef enum {
    S_STOP = 0x0,
    S_FREE = 0x1,
    S_PENDING = 0x2,
    S_OFF_STOP = 0x3,
    S_OFF_FREE = 0x04,
    S_OFF_PENDING = 0x05
  } state_s_t;

  typedef uint8_t modulestate_t;

#define MAKEMODULESTATE(l,m) ((modulestate_t)((((m)&0x0f)<<4)+((l)&0x0f)))
#define LEDSTATE(s) ((state_led_t)((s)&0x0f))
#define MODELSTATE(s) (((s)&0xf0)>>4)
#define TURNOUTSTATE(s) ((state_t_t)(((s)&0xf0)>>4))
#define CROSSINGSTATE(s) ((state_k_t)(((s)&0xf0)>>4))
#define THREEWAYSTATE(s) ((state_dw_t)(((s)&0xf0)>>4))
#define SEMAPHORESTATE(s) ((state_s_t)(((s)&0xf0)>>4))


  extern void initModule();
  extern uint16_t processModuleType(uint8_t moduleType, operation_t op);
  extern uint16_t processModuleState(uint8_t moduleState, operation_t op);

  extern uint8_t applyStateTrack(module_t type, modulestate_t state);
  extern uint8_t applyStateTurnout(module_t type, modulestate_t state);
  extern uint8_t applyStateCrossing(module_t type, modulestate_t state);
  extern uint8_t applyStateThreeway(module_t type, modulestate_t state);
  extern uint8_t applyStateSemaphore(module_t type, modulestate_t state);

  inline uint8_t applyState(modulestate_t state) {
    module_t type = eepromFile.moduletype;
    switch (type) {
      case G1:
        return applyStateTrack(type, state);
      case D1:
        return applyStateTrack(type, state);
      case B1:
        return applyStateTrack(type, state);
      case B2:
        return applyStateTrack(type, state);
      case F1:
        return applyStateTrack(type, state);
      case F2:
        return applyStateTrack(type, state);
      case F3:
        return applyStateTrack(type, state);
      case F4:
        return applyStateTrack(type, state);
      case W1:
        return applyStateTurnout(type, state);
      case W2:
        return applyStateTurnout(type, state);
      case W3:
        return applyStateTurnout(type, state);
      case W4:
        return applyStateTurnout(type, state);
      case K1:
        return applyStateCrossing(type, state);
      case K2:
        return applyStateCrossing(type, state);
      case DW1:
        return applyStateThreeway(type, state);
      case DW2:
        return applyStateThreeway(type, state);
      case DW3:
        return applyStateThreeway(type, state);
      case SEM_E:
        return applyStateSemaphore(type, state);
      case SEM_W:
        return applyStateSemaphore(type, state);
      default:
        return -1;
    }
  }
#ifdef	__cplusplus
}
#endif

#endif	/* SYMBOLS_H */

