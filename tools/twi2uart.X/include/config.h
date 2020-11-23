/*
 * File:   configrecord.h
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 22:05
 */

#ifndef CONFIG_H
#define CONFIG_H

#include <avr/io.h>
#include <avr/eeprom.h>
#include <avr/pgmspace.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifndef _AVR_IOM8_H_
#error unsupported cpu
#endif

#ifndef UART_ENABLED
#define UART_ENABLED 1
#endif

#ifndef SPI_ENABLED
#define SPI_ENABLED 0
#endif

#ifndef IND_DEBUG
#define IND_DEBUG 0
#endif

#ifndef FW_MAJOR
#define FW_MAJOR 0
#endif

#ifndef FW_MINOR
#define FW_MINOR 1
#endif

#if IND_DEBUG==1
#define IND_0_OFF (PORTD&=~_BV(PD2))
#define IND_0_ON (PORTD|=_BV(PD2))
#define IND_1_OFF (PORTD&=~_BV(PD3))
#define IND_1_ON (PORTD|=_BV(PD3))
#define IND_2_OFF (PORTD&=~_BV(PD4))
#define IND_2_ON (PORTD|=_BV(PD4))
#define IND_3_OFF (PORTD&=~_BV(PB5))
#define IND_3_ON (PORTD|=_BV(PB5))
#define IND_INIT   DDRD |= (_BV(PD2)|_BV(PD3)|_BV(PD4)|_BV(PD5));
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


  typedef unsigned char bool;

#define FREQ 8000000

#if FREQ==1000000
#if UART_ENABLED==1
#define UART_BAUD    12 // 9600
#define UART_U2X     1
#endif
#define TWI_BAUD     2 // 50k
#define TWI_PRESCALE 0
#define TIMER1_PRESCALE _BV(CS11) // 8
#define TIMER1_TOP   15625  // 8Hz
#elif FREQ==8000000
#if UART_ENABLED==1
#define UART_BAUD    12 // 38400
#define UART_U2X     0
#endif
#define TWI_BAUD     32 // 100k
#define TWI_PRESCALE 0
#define TIMER1_PRESCALE (_BV(CS11)|_BV(CS10)) // 64
#define TIMER1_TOP    2500  // 48Hz
#define TIMER1_BLINK_POSTSCALE 6
#else
#error undefined frequency
#endif


#define TWI_ADDR   1

#define BLINK_PORT PORTB
#define BLINK_PIN  PINB
#define BLINK_DIR  DDRB
#define BLINK      PB1
#define PWM        PB2

#define INT_PORT   PORTB
#define INT_PIN    PINB
#define INT_DIR    DDRB
#define INT        PB0

#if SPI_ENABLED==1
#define SPI_PORT   PORTB
#define SPI_DIR    DDRB
#define SPI_PIN    PINB
#define SPI_MOSI   PB3
#define SPI_MISO   PB4
#define SPI_SS     PB2
#define SPI_SCK    PB5
#define SPI_SS_INT_OR (_BV(ISC00))
#define SPI_SS_INT_EN _BV(INT0)
#define SPI_SS_INT_vec INT0_vect
#endif

  typedef enum {
    REG_STATE = 0,
    REG_VCC = 6,
    REG_VCC_CALIB = 12,
    REG_VCC_REF = 15,
    REG_TWI_BAUD = 16,
    REG_BLINK_COUNTER_H = 17,
    REG_BLINK_COUNTER_L = 18,
    REG_BLINK_PRESCALE = 19,
    REG_FW_VERSION = 14,
    REG_PWM_OUT = 20
  } register_t;

  typedef struct {
    int8_t vcc_calibration;
    uint8_t twibaud;

    union {
      uint16_t blinkCounter;

      struct {
        uint8_t blinkCounterH;
        uint8_t blinkCounterL;
      };
    };
    uint8_t blinkPrescale;
  } TEEPromFile;

  typedef struct {

    union {
      uint8_t state;

      struct {
        uint8_t reserved : 6;
        bool bo_error : 1; // 1 wenn brown out reset
        bool wdt_error : 1; // 1 wenn watchdog reset
      };
    };
    uint16_t vcc; // vcc mV;
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
          uint8_t a;
          uint8_t b;
        };
      };
    };

  } TDataPacket;

  typedef enum {
    TWI,
    UART,
    SPI
  } packetsource_t;

  extern TRegisterFile registerFile;
  extern EEMEM TEEPromFile ee_eepromFile;
  extern TEEPromFile eepromFile;
  extern const PROGMEM TFlashFile fl_flashFile;
  extern TFlashFile flashFile;

#ifdef __cplusplus
}
#endif

#endif /* CONFIGRECORD_H */

