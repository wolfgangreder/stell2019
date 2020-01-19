
#include <avr/io.h>
#include "symbols.h"

volatile uint8_t ledMask;

void initModule()
{
  module_t moduletype = eepromFile.moduletype;
  switch (moduletype) {
    case G1:
      ledMask = _BV(LED_4) | _BV(LED_5) | _BV(LED_9);
      break;
    case D1:
      ledMask = _BV(LED_3) | _BV(LED_6) | _BV(LED_9);
      break;
    case B1:
      ledMask = _BV(LED_1) | _BV(LED_5) | _BV(LED_9);
      break;
    case B2:
      ledMask = _BV(LED_3) | _BV(LED_4) | _BV(LED_9);
      break;
    case F1:
      ledMask = _BV(LED_4) | _BV(LED_5);
      break;
    case F2:
      ledMask = _BV(LED_3) | _BV(LED_6);
      break;
    case F3:
      ledMask = _BV(LED_1) | _BV(LED_5);
      break;
    case F4:
      ledMask = _BV(LED_3) | _BV(LED_4);
      break;
    case W1:
      ledMask = _BV(LED_1) | _BV(LED_4) | _BV(LED_5);
      break;
    case W2:
      ledMask = _BV(LED_4) | _BV(LED_5) | _BV(LED_6);
      break;
    case W3:
      ledMask = _BV(LED_3) | _BV(LED_5) | _BV(LED_6);
      break;
    case W4:
      ledMask = _BV(LED_1) | _BV(LED_4) | _BV(LED_8);
      break;
    case K1:
      ledMask = _BV(LED_3) | _BV(LED_4) | _BV(LED_5) | _BV(LED_6);
      break;
    case K2:
      ledMask = _BV(LED_1) | _BV(LED_4) | _BV(LED_5) | _BV(LED_8);
      break;
    case DW1:
      ledMask = _BV(LED_3) | _BV(LED_4) | _BV(LED_5) | _BV(LED_8);
      break;
    case DW2:
      ledMask = _BV(LED_1) | _BV(LED_3) | _BV(LED_4) | _BV(LED_6);
      break;
    case DW3:
      ledMask = _BV(LED_1) | _BV(LED_4) | _BV(LED_6) | _BV(LED_8);
      break;
    case SEM_E:
      ledMask = _BV(LED_2) | _BV(LED_3) | _BV(LED_4) | _BV(LED_5);
      break;
    case SEM_W:
      ledMask = _BV(LED_2) | _BV(LED_1) | _BV(LED_4) | _BV(LED_5);
      break;
    default:
      ledMask = 0;
  }
  DDR_LED = ledMask; // Ledausgänge
  PORT_LED = ledMask; // alle leds aus
  if (isFunctionModule(moduletype)) {
    enableSwitch();
  } else {
    disableSwitch();
  }
}

uint16_t processModuleState(uint8_t moduleState, operation_t op)
{
  uint16_t result = -1;
  if (op == OP_WRITE) {
    uint8_t tmp = applyState((modulestate_t) moduleState);
    if (tmp != 0xff) {
      registerFile.modulstate = tmp;
      result = (eepromFile.moduletype << 8) + tmp;
    }
  } else if (op == OP_READ) {
    result = (eepromFile.moduletype << 8) + registerFile.modulstate;
  }
  return result;
}

uint16_t processModuleType(uint8_t moduleType, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return eepromFile.moduletype;
    case OP_WRITE:
      switch (moduleType) {
        case G1:
        case D1:
        case B1:
        case B2:
        case F1:
        case F2:
        case F3:
        case F4:
        case W1:
        case W2:
        case W3:
        case W4:
        case K1:
        case K2:
        case DW1:
        case DW2:
        case DW3:
        case SEM_E:
        case SEM_W:
          eepromFile.moduletype = moduleType;
          eeprom_write_byte(&ee_eepromFile.moduletype, moduleType);
          initModule();
          processModuleState(0, OP_WRITE);
          return moduleType;
          break;
        default:
          return -1;
      }
    default:
      return -1;
  }
  return -1;
}

uint8_t applyStateTrack(module_t type, modulestate_t state)
{
  state_led_t ledstate = LEDSTATE(state);

  uint8_t leds = 0;
  uint8_t blink = ledstate == LED_BLINK ? ledMask : 0;
  uint8_t phase = 0;
  if (ledstate == LED_LAMPTEST) {
    leds = ledMask;
  } else {
    if (ledstate != LED_OFF) {
      switch (type) {
        case G1:
          leds = _BV(LED_4) | _BV(LED_5) | _BV(LED_9);
          break;
        case D1:
          leds = _BV(LED_3) | _BV(LED_6) | _BV(LED_9);
          break;
        case B1:
          leds = _BV(LED_1) | _BV(LED_5) | _BV(LED_9);
          break;
        case B2:
          leds = _BV(LED_3) | _BV(LED_4) | _BV(LED_9);
          break;
        case F1:
          leds = _BV(LED_4) | _BV(LED_5);
          break;
        case F2:
          leds = _BV(LED_3) | _BV(LED_6);
          break;
        case F3:
          leds = _BV(LED_1) | _BV(LED_5);
          break;
        case F4:
          leds = _BV(LED_3) | _BV(LED_4);
          break;
        default:
          return -1;
      }
    }
  }
  processLed(leds & ledMask, OP_WRITE);
  processBlinkMask(blink & ledMask, OP_WRITE);
  processBlinkPhase(phase & ledMask, OP_WRITE);
  return state;
}

uint8_t applyStateTurnout(module_t type, modulestate_t state)
{
  state_led_t ledstate = LEDSTATE(state);
  state_t_t turnout = TURNOUTSTATE(state);
  uint8_t leds = 0;
  uint8_t blink = ledstate == LED_BLINK ? ledMask : 0;
  uint8_t phase = 0;
  if (ledstate == LED_LAMPTEST) {
    leds = ledMask;
  } else if (ledstate != LED_OFF) {
    switch (type) {
      case W1:
        switch (turnout) {
          case T_STRAIT:
            leds = _BV(LED_4) | _BV(LED_5);
            break;
          case T_DEFLECTION:
            leds = _BV(LED_1) | _BV(LED_5);
            break;
          default:
            return -1;
        }
        break;
      case W2:
        switch (turnout) {
          case T_STRAIT:
            leds = _BV(LED_4) | _BV(LED_5);
            break;
          case T_DEFLECTION:
            leds = _BV(LED_6) | _BV(LED_5);
            break;
          default:
            return -1;
        }
        break;
      case W3:
        switch (turnout) {
          case T_STRAIT:
            leds = _BV(LED_6) | _BV(LED_3);
            break;
          case T_DEFLECTION:
            leds = _BV(LED_6) | _BV(LED_5);
            break;
          default:
            return -1;
        }
        break;
      case W4:
        switch (turnout) {
          case T_STRAIT:
            leds = _BV(LED_1) | _BV(LED_8);
            break;
          case T_DEFLECTION:
            leds = _BV(LED_4) | _BV(LED_8);
            break;
          default:
            return -1;
        }
        break;
      default:
        return -1;
    }
  }
  processLed(leds & ledMask, OP_WRITE);
  processBlinkMask(blink & ledMask, OP_WRITE);
  processBlinkPhase(phase & ledMask, OP_WRITE);
  return state;
}

uint8_t applyStateCrossing(module_t type, modulestate_t state)
{
  state_led_t ledstate = LEDSTATE(state);
  state_k_t crossing = CROSSINGSTATE(state);
  uint8_t leds = 0;
  uint8_t blink = ledstate == LED_BLINK ? ledMask : 0;
  uint8_t phase = 0;
  if (ledstate == LED_LAMPTEST) {
    leds = ledMask;
  } else if (ledstate != LED_OFF) {
    switch (type) {
      case K1:
        switch (crossing) {
          case K_STRAIT_0:
            leds = _BV(LED_4) | _BV(LED_5);
            break;
          case K_DEFLECTION_0:
            leds = _BV(LED_3) | _BV(LED_4);
            break;
          case K_STRAIT_1:
            leds = _BV(LED_3) | _BV(LED_6);
            break;
          case K_DEFLECTION_1:
            leds = _BV(LED_5) | _BV(LED_6);
            break;
          default:
            return -1;
        }
        break;
      case K2:
        switch (crossing) {
          case K_STRAIT_0:
            leds = _BV(LED_4) | _BV(LED_5);
            break;
          case K_DEFLECTION_0:
            leds = _BV(LED_1) | _BV(LED_5);
            break;
          case K_STRAIT_1:
            leds = _BV(LED_1) | _BV(LED_8);
            break;
          case K_DEFLECTION_1:
            leds = _BV(LED_4) | _BV(LED_8);
            break;
          default:
            return -1;
        }
        break;
      default:
        return -1;
    }
  }
  processLed(leds & ledMask, OP_WRITE);
  processBlinkMask(blink & ledMask, OP_WRITE);
  processBlinkPhase(phase & ledMask, OP_WRITE);
  return state;
}

uint8_t applyStateThreeway(module_t type, modulestate_t state)
{
  state_led_t ledstate = LEDSTATE(state);
  state_dw_t threeway = THREEWAYSTATE(state);

  uint8_t leds = 0;
  uint8_t blink = ledstate == LED_BLINK ? ledMask : 0;
  uint8_t phase = 0;
  if (ledstate == LED_LAMPTEST) {
    leds = ledMask;
  } else if (ledstate != LED_OFF) {
    switch (type) {
      case DW1:
        switch (threeway) {
          case D_STRAIT:
            leds = _BV(LED_4) | _BV(LED_5);
            break;
          case D_DEFLECTION_0:
            leds = _BV(LED_4) | _BV(LED_3);
            break;
          case D_DEFLECTION_1:
            leds = _BV(LED_4) | _BV(LED_8);
            break;
          default:
            return -1;
        }
        break;
      case DW2:
        switch (threeway) {
          case D_STRAIT:
            leds = _BV(LED_3) | _BV(LED_4);
            break;
          case D_DEFLECTION_0:
            leds = _BV(LED_3) | _BV(LED_1);
            break;
          case D_DEFLECTION_1:
            leds = _BV(LED_3) | _BV(LED_6);
            break;
          default:
            return -1;
        }
        break;
      case DW3:
        switch (threeway) {
          case D_STRAIT:
            leds = _BV(LED_8) | _BV(LED_4);
            break;
          case D_DEFLECTION_0:
            leds = _BV(LED_8) | _BV(LED_1);
            break;
          case D_DEFLECTION_1:
            leds = _BV(LED_8) | _BV(LED_6);
            break;
          default:
            return -1;
        }
        break;
      default:
        return -1;
    }
  }
  processLed(leds & ledMask, OP_WRITE);
  processBlinkMask(blink & ledMask, OP_WRITE);
  processBlinkPhase(phase & ledMask, OP_WRITE);
  return state;
}

uint8_t applyStateSemaphore(module_t type, modulestate_t state)
{
  state_led_t ledstate = LEDSTATE(state);
  state_s_t semaphore = SEMAPHORESTATE(state);
  uint8_t leds = (ledstate != LED_OFF && semaphore != S_OFF_FREE && semaphore != S_OFF_PENDING && semaphore != S_OFF_STOP) ? _BV(LED_4) | _BV(LED_5) : 0;
  uint8_t blink = ledstate == LED_BLINK ? _BV(LED_4) | _BV(LED_5) : 0;
  uint8_t phase = 0;
  if (ledstate == LED_LAMPTEST) {
    leds = ledMask;
    blink = 0;
  } else if (ledstate != LED_OFF) {
    switch (type) {
      case SEM_E:
        switch (semaphore) {
          case S_STOP:
          case S_OFF_STOP:
            leds |= _BV(LED_3);
            if (ledstate == LED_BLINK) {
              blink |= _BV(LED_3);
            }
            break;
          case S_FREE:
          case S_OFF_FREE:
            leds |= _BV(LED_2);
            if (ledstate == LED_BLINK) {
              blink |= _BV(LED_2);
            }
            break;
          case S_PENDING:
          case S_OFF_PENDING:
            leds |= _BV(LED_3) | _BV(LED_2);
            blink |= _BV(LED_3) | _BV(LED_2);
            phase = _BV(LED_2);
            break;
          default:
            return -1;
        }
        break;
      case SEM_W:
        switch (semaphore) {
          case S_STOP:
          case S_OFF_STOP:
            leds |= _BV(LED_1);
            if (ledstate == LED_BLINK) {
              blink |= _BV(LED_1);
            }
            break;
          case S_FREE:
          case S_OFF_FREE:
            leds |= _BV(LED_2);
            if (ledstate == LED_BLINK) {
              blink |= _BV(LED_2);
            }
            break;
          case S_PENDING:
          case S_OFF_PENDING:
            leds |= _BV(LED_1) | _BV(LED_2);
            blink |= _BV(LED_1) | _BV(LED_2);
            phase = _BV(LED_2);
            break;
          default:
            return -1;
        }
        break;
      default:
        return -1;
    }
  }
  processLed(leds & ledMask, OP_WRITE);
  processBlinkMask(blink & ledMask, OP_WRITE);
  processBlinkPhase(phase & ledMask, OP_WRITE);
  return state;
}

