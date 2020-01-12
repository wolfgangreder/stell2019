/*
 * File:   uart.h
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 21:50
 */

#ifndef UART_H
#define	UART_H

#include "config.h"

#ifdef	__cplusplus
extern "C" {
#endif

#if UART_ENABLED==1

  extern void initCommUsart();
  extern uint8_t getBytesAvailableUsart();
  extern uint8_t readBytes(uint8_t* buffer, uint8_t bytesToRead);
  extern bool isTxReady();
  extern void writeBytesUsart(uint8_t* buffer, uint8_t bytesToSend);

  inline void writeByteUsart(uint8_t byte) {
    writeBytesUsart(&byte, 1);
  }

  inline void sendACK() {
    writeByteUsart(6);
  }

  inline void sendNACK() {
    writeByteUsart(25);
  }


#endif

#ifdef	__cplusplus
}
#endif

#endif	/* UART_H */

