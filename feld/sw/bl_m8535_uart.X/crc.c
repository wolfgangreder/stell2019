
#include "crc.h"
#include <avr/boot.h>

uint8_t crc8(uint8_t* buffer, uint8_t offset, uint16_t length)
{
  uint8_t crc = 0;
  for (uint16_t i = offset; i < length + offset; ++i) {
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

uint16_t crc16Flash(const __flash uint8_t* buffer, uint8_t offset, uint16_t length)
{
  uint16_t crc = 0;
  for (uint16_t i = offset; i < length + offset; ++i) {
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

uint16_t getBootStart()
{
  uint8_t fuse = boot_lock_fuse_bits_get(GET_HIGH_FUSE_BITS);
  fuse = ~(fuse & ~(FUSE_BOOTSZ0 & FUSE_BOOTSZ1));
  switch (fuse) {
    case 0:
      return 0xc00;
      break;
    case FUSE_BOOTSZ0:
      return 0xe00;
      break;
    case FUSE_BOOTSZ1:
      return 0xf00;
      break;
    case (FUSE_BOOTSZ0 | FUSE_BOOTSZ1):
      return 0xf80;
      break;
    default:
      return FLASHEND;
  }

}

uint16_t crc16(uint8_t bootrec)
{
  const __flash uint16_t* crcLoc;
  uint8_t* location;
  uint16_t size;
  uint8_t* bootStart = (uint8_t*) getBootStart();
  if (bootrec) {
    location = bootStart;
    size = FLASHEND - (int) bootStart - 2;
    crcLoc = (uint16_t*) (FLASHEND - 1);
  } else {
    location = 0;
    size = (uint16_t) (bootStart - 2);
    crcLoc = (uint16_t*) (bootStart - 2);
  }
  return crc16Flash(location, 0, size);
}

uint8_t checkFlash(uint8_t bootrec)
{
  const __flash uint16_t* crcLoc;
  uint8_t* location;
  uint16_t size;
  uint8_t* bootStart = (uint8_t*) getBootStart();
  if (bootrec) {
    location = bootStart;
    size = FLASHEND - (int) bootStart - 2;
    crcLoc = (uint16_t*) (FLASHEND - 1);
  } else {
    location = 0;
    size = (uint16_t) (bootStart - 2);
    crcLoc = (uint16_t*) (bootStart - 2);
  }
  uint16_t calcCrc = crc16Flash(location, 0, size);
  return calcCrc == *crcLoc;
}

