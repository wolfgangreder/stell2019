/*
 * File:   spi.h
 * Author: wolfi
 *
 * Created on 11. Januar 2020, 15:55
 */

#ifndef SPI_H
#define	SPI_H

#include "config.h"


#ifdef	__cplusplus
extern "C" {
#endif

  extern void spiInit();
  extern bool spiIsBusy();
  extern uint8_t spiGetDataAvailable();
  extern uint8_t spiGetData(TDataPacket* buffer);
  extern void spiWriteData(TDataPacket* buffer);


#ifdef	__cplusplus
}
#endif

#endif	/* SPI_H */

