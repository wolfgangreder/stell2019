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
#if COMM==COMM_USART
#define initComm  initCommUsart
#define readByte readByteUsart
#define writeByte writeByteUsart
#elif COMM==COMM_TWI
#include "comm_twi.h"
#define initComm  initCommTwi
#define readByte readByteTwi
#define writeByte readByteUsart
#else
#error comminication not supported
#endif

  extern void initCommTwi();
  extern uint8_t readByteTwi();
  extern void writeByteTwi(uint8_t byte);
  extern __attribute__((section(".bootloader"))) void initCommUsart();
  extern uint8_t readByteUsart();
  extern void writeByteUsart(uint8_t byte);


#ifdef	__cplusplus
}
#endif

#endif	/* COMM_USART_H */

