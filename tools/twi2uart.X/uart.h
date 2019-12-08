/*
 * File:   uart.h
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 21:50
 */

#ifndef UART_H
#define	UART_H

#include "buffer.h"

#ifdef	__cplusplus
extern "C" {
#endif


  extern void initUART();
  extern uint8_t isUARTReadyToSend();
  extern uint8_t sendUARTData(void* buffer, uint8_t bytesToSend);


#ifdef	__cplusplus
}
#endif

#endif	/* UART_H */

