#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>
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
  currentPWM = registerFile.pwm;
  processPWM(registerFile.pwm, OP_WRITE);
  TCCR0 = _BV(WGM00) + _BV(CS02); // fosc/256
  TIMSK |= _BV(OCIE0) + _BV(TOIE0);
  MCUCR |= BLINK_MCU_OR;
  MCUCR &= BLINK_MCU_AND;
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
  if (OCR0 != 0xff) {
    PORT_LED = 0xff; // alle Leds aus
  }
}

/*
 * l = led & (!bm || bm & currentBlinkPhase ^ phaseInvert);
 */
ISR(TIMER0_COMP_vect)
{
  ledOn();
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
  uint16_t result;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    result = registerFile.vcc & 0x3ff;
  }
  return result;
}

