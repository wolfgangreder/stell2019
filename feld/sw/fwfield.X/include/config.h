/*
 * File:   hw.h
 * Author: wolfi
 *
 * Created on 28. November 2019, 18:01
 */

#ifndef HW_H
#define	HW_H
#include <avr/io.h>
#include <avr/eeprom.h>
#include <avr/pgmspace.h>
#include <avr/interrupt.h>
#include "types.h"

#ifdef	__cplusplus
extern "C" {
#endif



#ifndef TWI_ADDRESS
#define TWI_ADDRESS 3
#endif

#ifndef FW_MAJOR
#define FW_MAJOR 0
#endif

#ifndef FW_MINOR
#define FW_MINOR 0
#endif


#ifndef IND_DEBUG
#define IND_DEBUG 1
#endif

#if IND_DEBUG==1
#define IND_PORT PORTA
#define IND_DIR DDRA
#define IND_0_OFF (IND_PORT&=~_BV(PA0))
#define IND_0_ON (IND_PORT|=_BV(PA0))
#define IND_1_OFF (IND_PORT&=~_BV(PA1))
#define IND_1_ON (IND_PORT|=_BV(PA1))
#define IND_2_OFF (IND_PORT&=~_BV(PA2))
#define IND_2_ON (IND_PORT|=_BV(PA2))
#define IND_3_OFF (IND_PORT&=~_BV(PA3))
#define IND_3_ON (IND_PORT|=_BV(PA3))
#define IND_INIT   IND_DIR |= (_BV(PA0)|_BV(PA1)|_BV(PA2)|_BV(PA3));
#elif IND_DEBUG==2
#undef IND_0_OFF
#undef IND_0_ON
#undef IND_1_OFF
#undef IND_1_ON
#undef IND_2_OFF
#undef IND_2_ON
#undef IND_3_OFF
#undef IND_3_ON
#undef IND_INIT
#else
#define IND_0_OFF
#define IND_0_ON
#define IND_1_OFF
#define IND_1_ON
#define IND_2_OFF
#define IND_2_ON
#define IND_3_OFF
#define IND_3_ON
#define IND_INIT
#endif

#define TIMER1_PRESCALE_MASK (_BV(CS10)|_BV(CS11)|_BV(CS12))
#define TIMER1_PRESCALE_1 _BV(CS10)
#define TIMER1_PRESCALE_8 _BV(CS11)
#define TIMER1_PRESCALE_64 (_BV(CS10)|_BV(CS11))
#define TIMER1_PRESCALE_256 _BV(CS12)
#define TIMER1_PRESCALE_1024 (_BV(CS12)|_BV(CS10))

#ifdef _AVR_IOM8535_H_
#define FREQ 14745600
#define DEVICE 8535
#define PORT_LED PORTB
#define DDR_LED DDRB
#define PIN_LED PINB
#define LED_1 0
#define LED_2 1
#define LED_3 2
#define LED_4 3
#define LED_5 4
#define LED_6 5
#define LED_8 6
#define LED_9 7
#define SWITCH_PORT PORTD
#define SWITCH_DIR DDRD
#define SWITCH_PIN PIND
#define SWITCH 2
#define SWITCH_INT_vect INT0_vect
#define SWITCH_INT_ENABLE _BV(INT0)
#define SWITCH_MCU_OR _BV(ISC00)
#define SWITCH_MCU_AND (~_BV(ISC01))
#define PORT_BLINK PORTD
#define DDR_BLINK DDRD
#define PIN_BLINK PIND
#define BLINK 3
#define BLINK_INT_vect INT1_vect
#define BLINK_MCU_OR _BV(ISC11)
#define BLINK_MCU_AND (~_BV(ISC10))
#define BLINK_INT_ENABLE _BV(INT1)
#define MS_TIMER_PRESCALE TIMER1_PRESCALE_64
#define MS_TIMER_OCR 231
#define BLINK_PRESCALE_INIT 62
#define BAUD_TWI 66 // 100k
#define PRESC_TWI 0
#define PORT_TWI PORTC
#define DDR_TWI DDRC
#define PIN_TWI PINC
#define SDA PC1
#define SCL PC0
#else
#error device not supported
#endif



#define OFFSET_RAM (0)
#define OFFSET_EEPROM (96)
#define OFFSET_FLASH (120)

#define OFFSET(flag,file) (((void*)&(flag))-((void*)&(file)))

#define REG_STATE offsetof(TRegisterFile,state)
#define REG_LED offsetof(TRegisterFile,led)
#define REG_BLINKMASK offsetof(TRegisterFile,blinkmask)
#define REG_BLINKPHASE offsetof(TRegisterFile,blinkphase)
#define REG_PWM offsetof(TRegisterFile,pwm)
#define REG_MODULESTATE offsetof(TRegisterFile,modulstate)
#define REG_VCC offsetof(TRegisterFile,vcc)
#define REG_BLINKDIVIDER offsetof(TRegisterFile,blinkdivider)
#define REG_ADDRESS_MSB (OFFSET_EEPROM+offsetof(TEEPromFile,address_msb))
#define REG_ADDRESS_LSB (OFFSET_EEPROM+offsetof(TEEPromFile,address_lsb))
#define REG_MODULETYPE (OFFSET_EEPROM+offsetof(TEEPromFile,moduletype))
#define REG_DEBOUNCE (OFFSET_EEPROM+offsetof(TEEPromFile,debounce))
#define REG_SOFTSTART (OFFSET_EEPROM+offsetof(TEEPromFile,softstart))
#define REG_SOFTSTOP (OFFSET_EEPROM+offsetof(TEEPromFile,softstop))
#define REG_VCC_CALIBRATION (OFFSET_EEPROM+offsetof(TEEPromFile,vcc_calibration))
#define REG_DEFAULT_PWM (OFFSET_EEPROM+offsetof(TEEPromFile,defaultPWM))
#define REG_FEATURE_CONTROL (OFFSET_EEPROM+offsetof(TEEPromFile,featureControl))
#define REG_VERSION (OFFSET_FLASH+offsetof(TFlashFile,fw_major))

  typedef struct {

    union {
      uint16_t address;

      struct {
        uint8_t address_msb;
        uint8_t address_lsb;
      };
    };
    uint8_t moduletype;
    uint8_t debounce;
    uint8_t softstart;
    uint8_t softstop;
    int8_t vcc_calibration;
    uint8_t defaultPWM;
    uint8_t twibaud;
    uint8_t twipresc;
    uint16_t masterAddress;

    union {
      uint8_t featureControl;
    };
  } TEEPromFile;

  typedef struct {

    union {
      uint8_t state;

      struct {
        bool key_pressed : 1; // 1 wenn taste gedrückt
        uint8_t reserved : 6;
        bool key_error : 1; // 1 wenn ein tastendruck nicht gelesen wurde
      };
    };
    uint8_t led; // 1-> led an
    uint8_t blinkmask; // 1-> led blinkt
    uint8_t blinkphase; // 1-> led blinkt invers
    uint8_t pwm; // helligkeit
    uint8_t modulstate; // weiche gerade/ablenkung etc
    uint16_t vcc; // vcc mV;
    uint8_t blinkdivider;

  } TRegisterFile;

  typedef struct {

    union {
      uint16_t fw_Version;

      struct {
        uint8_t fw_major;
        uint8_t fw_minor;
      };
    };
  } TFlashFile;

  typedef enum {
    OP_READ = 0,
    OP_WRITE = 1,
    OP_INCREMENT = 2,
    OP_DECREMENT = 3,
    OP_COMPLEMENT = 4
  } operation_t;

  typedef union {
    uint8_t raw[4];

    struct {
      uint8_t rxSlaveAddress;
      uint8_t rxRegisterAddress;
      operation_t rxRegisterOperation;
      uint8_t rxData;
    };

    struct {
      uint8_t txSlaveAddress;
      uint8_t txOwnAddress;

      union {
        uint16_t txWData;

        struct {
          uint8_t txA;
          uint8_t txB;
        };
      };
    };

  } TDataPacket;

  extern TRegisterFile registerFile;
  extern EEMEM TEEPromFile ee_eepromFile;
  extern TEEPromFile eepromFile;
  extern const PROGMEM TFlashFile fl_flashFile;
  extern TFlashFile flashFile;

  inline void enableSwitch() {
    MCUCR |= SWITCH_MCU_OR;
    MCUCR &= SWITCH_MCU_AND;
    GICR |= SWITCH_INT_ENABLE;
    SWITCH_DIR &= ~_BV(SWITCH);
    SWITCH_PORT |= _BV(SWITCH);
  }

  inline void disableSwitch() {
    MCUCR &= SWITCH_MCU_AND & ~SWITCH_MCU_OR;
    GICR &= ~SWITCH_INT_ENABLE;
    SWITCH_DIR &= ~_BV(SWITCH);
    SWITCH_PORT &= ~_BV(SWITCH);
  }


  extern void initHW();
  extern uint16_t processState();
  extern uint16_t processLed(uint8_t led, operation_t operation);
  extern uint16_t processBlinkMask(uint8_t led, operation_t operation);
  extern uint16_t processBlinkPhase(uint8_t lef, operation_t operation);
  extern uint16_t processBlinkDivider(uint8_t val, operation_t operation);
  extern uint16_t processPWM(uint8_t pwm, operation_t operation);
  extern uint16_t processDefaultPWM(uint8_t pwm, operation_t operation);
  extern uint16_t processVCC();
  extern uint16_t processVCCCalibration(uint8_t cal, operation_t operation);
  extern uint16_t processDebounce(uint8_t val, operation_t operation);
  extern uint16_t processFeatureControl(uint8_t val, operation_t operation);
  extern uint16_t processFirmwareVersion();
  extern uint16_t processModuleType(uint8_t moduleType, operation_t operation);

#define TWI_ADDR 3
#define TWI_BAUD 66

#ifdef	__cplusplus
}
#endif

#endif	/* HW_H */

