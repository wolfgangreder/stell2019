/*
 * File:   configrecord.h
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 22:05
 */

#ifndef CONFIG_H
#define	CONFIG_H

#include <avr/io.h>

#ifdef	__cplusplus
extern "C" {
#endif

#ifndef _AVR_IOM8_H_
#error unsupported cpu
#endif

#ifndef UART_ENABLED
#define UART_ENABLED 1
#endif

#ifndef IND_DEBUG
#define IND_DEBUG 1
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
#define TIMER1_TOP    15625  // 8Hz
#else
#error undefined frequency
#endif


#define TWI_ADDR   1

#define BLINK_PORT PORTB
#define BLINK_PIN  PINB
#define BLINK_DIR  DDRB
#define BLINK      1

#define INT_PORT   PORTB
#define INT_PIN    PINB
#define INT_DIR    DDRB
#define INT        0

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
#ifdef	__cplusplus
}
#endif

#endif	/* CONFIGRECORD_H */

