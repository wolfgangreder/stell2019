/*
 * File:   hw.h
 * Author: wolfi
 *
 * Created on 28. November 2019, 18:01
 */

#ifndef HW_H
#define	HW_H
#include <avr/io.h>
#ifdef	__cplusplus
extern "C" {
#endif
#define COMM_USART 0
#define COMM_TWI 1

#define COMM COMM_USART


#ifdef _AVR_IOM8535_H_
#define FREQ 14745600
#define DEVICE 8535
#define PORT_LED PORTC
#define PIN_LED_1 PC0
#define PIN_LED_2 PC1
#define PIN_LED_3 PC2
#define PIN_LED_4 PC3
#define PIN_LED_5 PC4
#define PIN_LED_6 PC5
#define PIN_LED_8 PC6
#define PIN_LED_9 PC7
#define SWITCH_PORT PORTD
#define SWITCH_PIN PD2
#else
#error device not supported
#endif

  typedef enum {
    CHECK_FLASH,
    IDLE,
    LAMPTEST,
    RUNNING,
    UPDATE
  } device_state;


  extern uint8_t crc8(uint8_t* buffer, uint8_t offset, uint8_t length);
  extern uint16_t crc16(uint8_t* buffer, uint8_t offset, uint8_t length);
#ifdef	__cplusplus
}
#endif

#endif	/* HW_H */

