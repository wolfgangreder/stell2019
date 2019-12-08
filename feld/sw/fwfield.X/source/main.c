/*
 * File:   main.c
 * Author: wolfi
 *
 * Created on 28. November 2019, 17:45
 */
#include "hw.h"
#include "comm.h"
#include "protocol.h"
#include <avr/interrupt.h>

#if DEVICE==8535
FUSES = {
  FUSE_CKSEL0 | FUSE_CKSEL1 | FUSE_CKSEL3 | FUSE_SUT0 | FUSE_BODEN,
  FUSE_BOOTSZ0 | FUSE_BOOTSZ1 | FUSE_BOOTRST
};
#elif DEVICE==1604
FUSES = {
  .OSCCFG = FREQSEL_20MHZ_gc,
  .SYSCFG0 = CRCSRC_NOCRC_gc | RSTPINCFG_UPDI_gc,
  .SYSCFG1 = SUT_64MS_gc,
  .APPEND = 0x00,
  .BOOTEND = BOOTEND_FUSE
};
#else
#  error unknown device
#endif

/*
 *
 */
int
main()
{
  initProtocol();
  initComm();
  sei();
  return 0;
}

uint8_t crc8(uint8_t* buffer, uint8_t offset, uint8_t length)
{
  uint8_t crc = 0;
  for (uint8_t i = offset; i < length + offset; ++i) {
    uint8_t b = buffer[i];
    for (uint8_t n = 1; n <= 8; ++n) {
      uint8_t s = b & 0x01;
      if ((crc & 0x80) != 0) {
        crc = (crc * 2 + s) ^ 0x07;
      } else {
        crc = (crc * 2 + s);
      }
      b = b >> 1;
    }
  }
  return crc;
}

uint16_t crc16(uint8_t* buffer, uint8_t offset, uint8_t length)
{
  uint16_t crc = 0;
  for (uint8_t i = offset; i < length + offset; ++i) {
    uint8_t b = buffer[i];
    for (uint8_t n = 1; n <= 8; ++n) {
      uint8_t s = b & 0x01;
      if ((crc & 0x8000) != 0) {
        crc = (crc * 2 + s)^0x1021;
      } else {
        crc = (crc * 2 + s);
      }
      b = b >> 1;
    }
  }
  return crc;
}