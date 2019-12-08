/*
 * File:   twi.h
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 22:32
 */

#ifndef TWI_H
#define	TWI_H

#include "buffer.h"

#ifdef	__cplusplus
extern "C" {
#endif

  // addressen 0x08 bis 0x77 sind als 7bit address verfügbar
#define IS_SMALLADDRESS(a) ((a)>=0x08 && (a)<=0x77)
#define TOSLA(a,w) ((IS_SMALLADDRESS(a) ? (a) : (0xf0+(((a)&0x300)>>7)))+((w)&0x01))
#define TOADDR(a) (IS_SMALLADDRESS(a) ? 0 : ((a) & 0xff))
  extern void initTWI();
  extern uint8_t isTWIReadyToSend();
  extern uint8_t sendTWI(buffer_head_t* buffer);


#ifdef	__cplusplus
}
#endif

#endif	/* TWI_H */

