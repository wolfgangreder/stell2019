#include <avr/io.h>
#include<avr/interrupt.h>
#include <string.h>
#include <util/atomic.h>
#include "spi.h"

TDataPacket inBuffer;
TDataPacket outBuffer;
uint8_t volatile offset;
bool volatile spiBusy;
bool volatile hasData;

void spiInit()
{
  memset(&inBuffer, 0, sizeof (inBuffer));
  memset(&outBuffer, 0, sizeof (outBuffer));
  spiBusy = 0;
  hasData = 0;
  offset = 0;
#if SPI_ENABLED==1
  INT_PORT |= _BV(INT);
  INT_DIR |= _BV(INT);
  SPI_DIR |= SPI_MISO; // MISO output
  SPCR = _BV(SPE) | _BV(SPIE);
  __attribute__((unused))uint8_t dummy = SPSR;
  dummy = SPDR;
  //  INT_DIR |= _BV(INT);
  //  INT_PORT |= _BV(INT);
  //  MCUCR |= SPI_SS_INT_OR;
  //  GICR |= SPI_SS_INT_EN;
#endif
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
  return spiBusy;
}

bool spiPollData(TDataPacket* dataBuffer)
{
  bool result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    if (hasData) {
      memcpy(dataBuffer, &inBuffer, sizeof (inBuffer));
      result = 1;
      hasData = 0;
      offset = 0;
    }
  }
  return result;
}

void spiWriteData(TDataPacket* dataBuffer)
{
#if SPI_ENABLED==1

  while (spiIsBusy());
  memcpy(&outBuffer, dataBuffer, sizeof (outBuffer));
  offset = 0;
  triggerInt();
#endif
}
#if SPI_ENABLED==1

ISR(SPI_SS_INT_vec)
{
  if (SPI_PIN & _BV(SPI_SS)) {
    spiBusy = 1;
    hasData = 0;
  } else {
    releaseInt();
    hasData = 1;
  }
}
#endif

ISR(SPI_STC_vect)
{
  if (!spiBusy) {
    spiBusy = 1;
    hasData = 0;
    SPDR = outBuffer.raw[offset];
  } else {
    if (offset<sizeof (TDataPacket)) {
      inBuffer.raw[offset++] = SPDR;
      if (offset<sizeof (TDataPacket)) {
        SPDR = outBuffer.raw[offset];
      }
    }
    if (offset == sizeof (TDataPacket)) {
      spiBusy = 0;
      hasData = 1;
      releaseInt();
    }
  }
}