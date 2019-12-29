/*
 * File:   comm_usart.h
 * Author: wolfi
 *
 * Created on 28. November 2019, 18:48
 */

#ifndef COMM_USART_H
#define	COMM_USART_H
#include "hw.h"
#ifdef	__cplusplus
extern "C" {
#endif


  extern void initCommUsart(uint8_t ownAddress);
  extern uint8_t getBytesAvailableUsart();
  extern uint8_t readBytes(uint8_t* buffer, uint8_t bytesToRead);
  extern uint8_t isTxReady();
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


#ifdef	__cplusplus
}
#endif

#endif	/* COMM_USART_H */

