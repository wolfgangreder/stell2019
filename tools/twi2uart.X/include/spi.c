#include <avr/io.h>
#include<avr/interrupt.h>
#include <string.h>
#include <util/atomic.h>
#include "spi.h"

TDataPacket inBuffer;
TDataPacket outBuffer;
uint8_t offset;
bool spiBusy;

void spiInit()
{
  memset(&inBuffer, 0, sizeof (inBuffer));
  memset(&outBuffer, 0, sizeof (outBuffer));
  DDRB |= _BV(PB4); // MISO output
  SPCR |= _BV(SPE) | _BV(SPIE);
  INT_DIR |= _BV(INT);
  INT_PORT |= _BV(INT);
  spiBusy = 0;
}

inline void triggerInt()
{
  spiBusy = 1;
  INT_PORT &= ~_BV(INT);
}

inline void releaseInt()
{
  spiBusy = 0;
  INT_PORT |= _BV(INT);
}

bool spiIsBusy()
{
  return spiBusy || SPSR & _BV(SPIF);
}

uint8_t spiGetDataAvailable()
{
  return offset;
}

uint8_t spiGetData(TDataPacket* dataBuffer)
{
  while (spiIsBusy());

  uint8_t result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    if (offset == sizeof (inBuffer)) {
      memcpy(dataBuffer, &inBuffer, sizeof (inBuffer));
      result = sizeof (inBuffer);
      offset = 0;
    }
  }
  return result;
}

void spiWriteData(TDataPacket* dataBuffer)
{
  while (spiIsBusy());
  memcpy(&outBuffer, dataBuffer, sizeof (outBuffer));
  SPDR = outBuffer.raw[0];
  offset = 0;
  triggerInt();
}

ISR(SPI_STC_vect)
{
  if (offset<sizeof (TDataPacket)) {
    inBuffer.raw[offset++] = SPDR;
    if (offset<sizeof (TDataPacket)) {
      SPDR = outBuffer.raw[offset];
    }
  }
  if (offset == sizeof (TDataPacket)) {
    releaseInt();
  }
}