/*
 * File:   crc.h
 * Author: wolfi
 *
 * Created on 30. November 2019, 13:48
 */

#ifndef CRC_H
#define	CRC_H

#include <avr/io.h>

#ifdef	__cplusplus
extern "C" {
#endif
  extern uint16_t getBootStart();

  extern uint8_t checkFlash(uint8_t bootrec);

  extern uint8_t crc8(uint8_t* buffer, uint8_t offset, uint16_t length);
  extern uint16_t crc16(uint8_t bootrec);
  extern uint16_t crc16Flash(const __flash uint8_t* buffer, uint8_t offset, uint16_t length);

#ifdef	__cplusplus
}
#endif

#endif	/* CRC_H */

