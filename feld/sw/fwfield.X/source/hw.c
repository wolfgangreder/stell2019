#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>
#include <math.h>
#include "hw.h"

#ifdef KEY_DEBOUNCE

typedef enum {
  IDLE,
  RELEASED,
  TRIGGERED_PRESSED,
  PRESSED,
  TRIGGERED_RELEASED
} debounce_state_t;
#endif
static volatile uint8_t currentBlinkPhase;
static volatile uint8_t blinkScale;
static volatile uint8_t currentPWM;
#ifdef KEY_DEBOUNCE
static volatile debounce_state_t keyState;
static volatile uint8_t debouncePhase;
#endif

inline uint8_t isKeyPressed()
{
  return (SWITCH_PIN & _BV(SWITCH)) == 0;
}

void initHW()
{
  eeprom_read_block(&eepromFile, &ee_eepromFile, sizeof (TEEPromFile));
  memcpy_P(&flashFile, &fl_flashFile, sizeof (TFlashFile));
  DDR_LED = 0xff; // Ledausgänge
  PORT_LED = 0xff; // alle leds aus
  registerFile.blinkdivider = 4;
  processPWM(0x7f, OP_WRITE);
  currentPWM = registerFile.pwm;
  TCCR0 = _BV(WGM01) + _BV(WGM00) + _BV(CS02); // fast PWM; fosc/256
  TIMSK |= _BV(OCIE0) + _BV(TOIE0);
  MCUCR |= BLINK_MCU_OR;
  MCUCR |= SWITCH_MCU_OR;
  MCUCR &= BLINK_MCU_AND;
  MCUCR &= SWITCH_MCU_AND;
  GICR |= BLINK_INT_ENABLE | SWITCH_INT_ENABLE;
  // ADC
  // ref=PA0->VCC
  // channel = vref
  ADMUX = _BV(REFS0) | _BV(MUX1) | _BV(MUX2) | _BV(MUX3) | _BV(MUX4);
  SFIOR &= ~(_BV(ADTS0) | _BV(ADTS1) | _BV(ADTS2));
  ADCSRA = _BV(ADEN) | _BV(ADATE) | _BV(ADPS0) | _BV(ADPS1) | _BV(ADPS2) | _BV(ADSC);
  SWITCH_DIR &= ~_BV(SWITCH);
  SWITCH_PORT |= _BV(SWITCH);
#ifdef KEY_DEBOUNCE
  TIMSK |= _BV(TOIE1);
  keyState = IDLE;
#endif
}

void ledOn()
{
  uint8_t bm;
  bm = registerFile.blinkmask;
  uint8_t tmp = registerFile.led & (~bm | (bm & (registerFile.blinkphase^currentBlinkPhase)));
  PORT_LED = ~tmp;
}
#ifdef KEY_DEBOUNCE

void startDebounceTimer()
{
  TCNT1 = -231;
  TCCR1B = _BV(CS11) | _BV(CS10);
  debouncePhase = eepromFile.debounce;
}

ISR(TIMER1_OVF_vect)
{
  TCNT1 = -231;
  if ((--debouncePhase) == 0) {
    TCCR1B = 0; // stop timer
    uint8_t sp = isKeyPressed();
    switch (keyState) {
      case TRIGGERED_PRESSED:
        if (sp) {
          IND_01;
          registerFile.key_pressed = 1;
        }
        break;
      case TRIGGERED_RELEASED:
        if (!sp) {
          IND_00;
          registerFile.key_pressed = 0;
        }

        break;
      default:
        registerFile.key_error = 1;
        break;
        // do nothing by intention
    }
    keyState = IDLE;
  }
}
#endif

ISR(SWITCH_INT_vect)
{
  registerFile.key_pressed = isKeyPressed();
}

ISR(BLINK_INT_vect)
{
  uint8_t bd;
  bd = registerFile.blinkdivider;
  blinkScale = (blinkScale + 1) % bd;
  if (blinkScale == 0) {
    currentBlinkPhase = ~currentBlinkPhase;
  }
}

ISR(TIMER0_OVF_vect)
{
  ledOn();
}

ISR(TIMER0_COMP_vect)
{
  PORT_LED = 0xff;
}

uint16_t processLed(uint8_t led, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return registerFile.led;
    case OP_WRITE:
      registerFile.led = led;
      return led;
    case OP_COMPLEMENT:
      registerFile.led = ~registerFile.led;
      return registerFile.led;
    default:
      return -1;
  }
  return -1;
}

uint16_t processBlinkMask(uint8_t led, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return registerFile.blinkmask;
    case OP_WRITE:
      return registerFile.blinkmask = led;
    case OP_COMPLEMENT:
      registerFile.blinkmask = ~registerFile.blinkmask;
      return registerFile.blinkmask;
    default:
      return -1;
  }
}

uint16_t processBlinkPhase(uint8_t led, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return registerFile.blinkphase;
    case OP_WRITE:
      return registerFile.blinkphase = led;
    case OP_COMPLEMENT:
      registerFile.blinkphase = ~registerFile.blinkphase;
      return registerFile.blinkphase;
    default:
      return -1;
  }
}

uint16_t processPWM(uint8_t pwm, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return registerFile.pwm;
    case OP_WRITE:
      OCR0 = pwm;
      registerFile.pwm = OCR0;
      if (OCR0 != 0xff) {
        TIMSK |= _BV(OCIE0);
      } else {
        TIMSK &= ~_BV(OCIE0);
      }
      return registerFile.pwm;
    case OP_INCREMENT:
    {
      uint16_t tmp = registerFile.pwm += pwm;
      if (tmp > 0xff) {
        registerFile.pwm = 0xff;
      } else {
        registerFile.pwm = tmp;
      }
    }
      OCR0 = registerFile.pwm;
      if (OCR0 != 0xff) {
        TIMSK |= _BV(OCIE0);
      } else {
        TIMSK &= ~_BV(OCIE0);
      }
      return registerFile.pwm;
    case OP_DECREMENT:
      if (pwm > registerFile.pwm) {
        registerFile.pwm = 0;
      } else {
        registerFile.pwm -= pwm;
      }
      OCR0 = registerFile.pwm;
      if (OCR0 != 0xff) {
        TIMSK |= _BV(OCIE0);
      } else {
        TIMSK &= ~_BV(OCIE0);
      }
      return registerFile.pwm;
    default:
      return -1;
  }
}

uint16_t processVCC()
{
  int16_t result;
  uint8_t l = ADCL;
  uint8_t h = ADCH;

  result = ((h << 8) + l) + eepromFile.vcc_calibration;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    registerFile.vcc = result;
  }
  return result;
}

uint8_t processVCCCalibration(uint8_t cal, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return eepromFile.vcc_calibration;
    case OP_WRITE:
      eepromFile.vcc_calibration = (int8_t) cal;
      eeprom_write_byte((uint8_t*) & ee_eepromFile.vcc_calibration, eepromFile.vcc_calibration);
      return cal;
    case OP_INCREMENT:
      eepromFile.vcc_calibration++;
      eeprom_write_byte((uint8_t*) & ee_eepromFile.vcc_calibration, eepromFile.vcc_calibration);
      return eepromFile.vcc_calibration;
    case OP_DECREMENT:
      eepromFile.vcc_calibration--;
      eeprom_write_byte((uint8_t*) & ee_eepromFile.vcc_calibration, eepromFile.vcc_calibration);
      return eepromFile.vcc_calibration;
    default:
      return -1;
  }
}

uint16_t processFirmwareVersion()
{
  return flashFile.fw_Version;
}

uint16_t processFirmwareBuild()
{
  return flashFile.fw_build;
}

uint16_t processBlinkDivider(uint8_t val, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return registerFile.blinkdivider;
    case OP_WRITE:
      if (val != 0) {
        registerFile.blinkdivider = val;
      }
      return registerFile.blinkdivider;
    case OP_DECREMENT:
      if (registerFile.blinkdivider > 1) {
        registerFile.blinkdivider--;
      }
      return registerFile.blinkdivider;
    case OP_INCREMENT:
      if (registerFile.blinkdivider < 254) {
        registerFile.blinkdivider++;
      }
      return registerFile.blinkdivider;
    default:
      return -1;
  }
}

uint16_t processState()
{
  return registerFile.state & 0xff;
}

uint16_t processDebounce(uint8_t val, operation_t operation)
{
#ifdef KEY_DEBOUNCE

  switch (operation) {
    case OP_READ:
      return eepromFile.debounce;
    case OP_WRITE:
      eepromFile.debounce = val;
      eeprom_write_byte(&ee_eepromFile.debounce, eepromFile.debounce);
      return val;
    case OP_INCREMENT:
      eepromFile.debounce++;
      eeprom_write_byte(&ee_eepromFile.debounce, eepromFile.debounce);
      return eepromFile.debounce;
    case OP_DECREMENT:
      eepromFile.debounce++;
      eeprom_write_byte(&ee_eepromFile.debounce, eepromFile.debounce);
      return eepromFile.debounce;
    default:
      return -1;
  }
#else
  return -1;
#endif
}
