#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>
#include <math.h>
#include "config.h"
#ifdef COMM_USART
#  include "comm.h"
#else
#  include "twi_old.h"
#endif

volatile uint8_t currentBlinkPhase;
static volatile uint8_t blinkScale;
static volatile uint8_t currentPWM;
static volatile uint8_t debouncePhase;
static volatile bool switchState;
static uint8_t blinkPrescale;

inline bool isKeyPressed()
{
  return (SWITCH_PIN & _BV(SWITCH)) == 0;
}

void initHW()
{
  eeprom_read_block(&eepromFile, &ee_eepromFile, sizeof (TEEPromFile));
  memcpy_P(&flashFile, &fl_flashFile, sizeof (TFlashFile));
  registerFile.blinkdivider = 4;
  processPWM(eepromFile.defaultPWM, OP_WRITE);
  processFeatureControl(eepromFile.featureControl, OP_WRITE);
  currentPWM = registerFile.pwm;
  TCCR0 = _BV(WGM01) + _BV(WGM00) + _BV(CS02); // fast PWM; fosc/256
  TIMSK |= _BV(OCIE0) + _BV(TOIE0);
  MCUCR |= BLINK_MCU_OR;
  MCUCR &= BLINK_MCU_AND;
  GICR |= BLINK_INT_ENABLE;
  // ADC
  // ref=VCC
  // channel = vref
  ADMUX = _BV(REFS0) | _BV(MUX1) | _BV(MUX2) | _BV(MUX3) | _BV(MUX4);
  SFIOR &= ~(_BV(ADTS0) | _BV(ADTS1) | _BV(ADTS2));
  ADCSRA = _BV(ADEN) | _BV(ADATE) | _BV(ADPS0) | _BV(ADPS1) | _BV(ADPS2) | _BV(ADSC);
  blinkPrescale = BLINK_PRESCALE_INIT;
  TIMSK |= _BV(TICIE1);
  TCNT1 = 0;
  TCCR1A = 0;
  ICR1 = MS_TIMER_OCR;
  TCCR1B = _BV(WGM12) + _BV(WGM13) + MS_TIMER_PRESCALE;
  switchState = isKeyPressed();
}

inline void ledOn()
{
  uint8_t bm;
  bm = registerFile.blinkmask;
  uint8_t tmp = registerFile.led & (~bm | (bm & (registerFile.blinkphase^currentBlinkPhase)));
  PORT_LED = ~tmp;
}

void startDebounceTimer()
{
  debouncePhase = eepromFile.debounce;
}

void sendKeyEvent()
{
#ifdef COMM_USART
  uint8_t msg[3];
  msg[0] = 6;
  msg[1] = registerFile.state;
  msg[2] = registerFile.modulstate;
  writeBytesUsart(msg, sizeof (msg));
#else
  TDataPacket msg;
  msg.txSlaveAddress = MAKE_ADDRESS_W(eepromFile.masterAddress);
  msg.txOwnAddress = eepromFile.address;

  msg.txA = registerFile.state;
  msg.txB = registerFile.modulstate;
  twiSendData(&msg);
#endif
}

ISR(TIMER1_CAPT_vect)
{
  if (debouncePhase == 1) {
    --debouncePhase;
    uint8_t savedSwitchState = switchState;
    uint8_t ss = switchState;
    savedSwitchState = isKeyPressed();
    if (ss != savedSwitchState) {
      registerFile.key_pressed = savedSwitchState;
      sendKeyEvent();
    }
    switchState = savedSwitchState;
    enableSwitch();
  } else {
    --debouncePhase;
  }

  if ((--blinkPrescale) == 0) {
    blinkPrescale = BLINK_PRESCALE_INIT;
    if (eepromFile.blinkGenerator) {
      PORT_BLINK ^= _BV(BLINK);
    }
  }
}

ISR(SWITCH_INT_vect)
{
  disableSwitch();
  startDebounceTimer();
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
  uint16_t tmp;
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
      tmp = registerFile.pwm += pwm;
      if (tmp > 0xff) {
        registerFile.pwm = 0xff;
      } else {
        registerFile.pwm = tmp;
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

uint16_t processDefaultPWM(uint8_t pwm, operation_t operation)
{
  uint16_t tmp;
  switch (operation) {
    case OP_READ:
      return eepromFile.defaultPWM;
    case OP_WRITE:
      eepromFile.defaultPWM = pwm;
      eeprom_write_byte(&ee_eepromFile.defaultPWM, eepromFile.defaultPWM);
      return eepromFile.defaultPWM;
    case OP_INCREMENT:
      tmp = eepromFile.defaultPWM += pwm;
      if (tmp > 0xff) {
        eepromFile.defaultPWM = 0xff;
      } else {
        eepromFile.defaultPWM = tmp;
      }
      eeprom_write_byte(&ee_eepromFile.defaultPWM, eepromFile.defaultPWM);
      return registerFile.pwm;
    case OP_DECREMENT:
      if (pwm > eepromFile.defaultPWM) {
        eepromFile.defaultPWM = 0;
      } else {
        eepromFile.defaultPWM -= pwm;
      }
      eeprom_write_byte(&ee_eepromFile.defaultPWM, eepromFile.defaultPWM);
      return eepromFile.defaultPWM;
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

uint16_t processVCCCalibration(uint8_t cal, operation_t operation)
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
      if (registerFile.blinkdivider < 255) {
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

uint16_t processFeatureControl(uint8_t val, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return eepromFile.featureControl;
    case OP_WRITE:
      eepromFile.featureControl = val;
      eeprom_write_byte(&ee_eepromFile.featureControl, eepromFile.featureControl);
      if (eepromFile.blinkGenerator) {
        DDR_BLINK |= _BV(BLINK);
      } else {
        DDR_BLINK &= ~_BV(BLINK);
        PORT_BLINK |= _BV(BLINK); // Pullup ein
      }
      return val;
    default:
      return -1;
  }
}

uint16_t processDebounce(uint8_t val, operation_t operation)
{
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
}
