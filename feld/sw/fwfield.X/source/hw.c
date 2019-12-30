#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>
#include <math.h>
#include "hw.h"

static volatile uint8_t currentBlinkPhase;
static volatile uint8_t blinkScale;
static volatile uint8_t currentPWM;

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
  MCUCR &= BLINK_MCU_AND;
  GICR |= BLINK_INT_ENABLE;
  // ADC
  // ref=PA0->VCC
  // channel = vref
  ADMUX = _BV(REFS0) | _BV(MUX1) | _BV(MUX2) | _BV(MUX3) | _BV(MUX4);
  SFIOR &= ~(_BV(ADTS0) | _BV(ADTS1) | _BV(ADTS2));
  ADCSRA = _BV(ADEN) | _BV(ADATE) | _BV(ADPS0) | _BV(ADPS1) | _BV(ADPS2) | _BV(ADSC);
}

void ledOn()
{
  uint8_t bm;
  bm = registerFile.blinkmask;
  uint8_t tmp = registerFile.led & (~bm | (bm & (registerFile.blinkphase^currentBlinkPhase)));
  PORT_LED = ~tmp;
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

/*
 * l = led & (!bm || bm & currentBlinkPhase ^ phaseInvert);
 */
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
      return registerFile.pwm;
    case OP_DECREMENT:
      if (pwm > registerFile.pwm) {
        registerFile.pwm = 0;
      } else {
        registerFile.pwm -= pwm;
      }
      OCR0 = registerFile.pwm;
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