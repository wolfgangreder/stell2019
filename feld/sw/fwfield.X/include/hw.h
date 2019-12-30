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

#ifdef	__cplusplus
extern "C" {
#endif
#define IND_1 (PORTD&=~_BV(PD7))
#define IND_0 (PORTD|=_BV(PD7))

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
#define PORT_BLINK PORTD
#define DDR_BLINK DDRD
#define PIN_BLINK PIND
#define BLINK 3
#define BLINK_INT_vect INT1_vect
#define BLINK_MCU_OR _BV(ISC11)
#define BLINK_MCU_AND (~_BV(ISC10))
#define BLINK_INT_ENABLE _BV(INT1)
#else
#error device not supported
#endif

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

  } TEEPromFile;

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
#define REG_VERSION_MAJOR (OFFSET_FLASH+offsetof(TFlashFile,fw_major))
#define REG_VERSION_MINOR (OFFSET_FLASH+offsetof(TFlashFile,fw_minor))
#define REG_BUILD_MSB (OFFSET_FLASH+offsetof(TFlashFile,fw_build))
#define REG_BUILD_LSB (OFFSET_FLASH+offsetof(TFlashFile,fw_build)+1)

  typedef struct {

    union {
      uint8_t state;

      struct {
        uint8_t key_pressed : 1; // 1 wenn taste gedrückt
        uint8_t reserved : 6;
        uint8_t key_missed : 1; // 1 wenn ein tastendruck nicht gelesen wurde
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
    uint8_t fw_major;
    uint8_t fw_minor;
    uint16_t fw_build;
  } TFlashFile;

  typedef enum {
    OP_READ = 0,
    OP_WRITE = 1,
    OP_INCREMENT = 2,
    OP_DECREMENT = 3,
    OP_COMPLEMENT = 4
  } operation_t;

  typedef struct {
    uint8_t registerAddress;
    operation_t registerOperation;

    union {
      uint8_t data[2];
      uint16_t wdata;
    };
  } TCommandBuffer;

  extern TRegisterFile registerFile;
  extern EEMEM TEEPromFile ee_eepromFile;
  extern TEEPromFile eepromFile;
  extern const PROGMEM TFlashFile fl_flashFile;
  extern TFlashFile flashFile;

  extern void initHW();
  extern uint16_t processLed(uint8_t led, operation_t operation);
  extern uint16_t processBlinkMask(uint8_t led, operation_t operation);
  extern uint16_t processBlinkPhase(uint8_t lef, operation_t operation);
  extern uint16_t processPWM(uint8_t pwm, operation_t operation);
  extern uint16_t processVCC();
  extern uint8_t processVCCCalibration(uint8_t cal, operation_t operation);

#ifdef	__cplusplus
}
#endif

#endif	/* HW_H */
