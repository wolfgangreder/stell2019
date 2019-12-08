#include <avr/io.h>
#include <util/atomic.h>

#include "twi.h"
#include "buffer.h"

buffer_head_t* currentBuffer;
uint8_t bufOffset;

#define TWCR_INIT (_BV(TWEA) | _BV(TWEN) | _BV(TWIE))
#define TWCR_START (_BV(TWEA) | _BV(TWEN) | _BV(TWIE)|_BV(TWSTA))
#define TWCR_STOP (_BV(TWEA) | _BV(TWEN) | _BV(TWIE)|_BV(TWSTO))

void initTWI()
{
  PORTC = _BV(PINC1) | _BV(PINC0); // enable Pull-Up
  // TWBR= 14 (fSCL=115200)
  TWBR = 14;
  // prescale = 1
  TWSR = 0;
  TWCR = _BV(TWEA) | _BV(TWEN) | _BV(TWIE);
}

uint8_t isTWIReadyToSend()
{
  return currentBuffer == 0;
}

uint8_t sendTWI(buffer_head_t* buffer)
{
  if (isTWIReadyToSend()) {

    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      currentBuffer = buffer;
      bufOffset = 0;
    }
    TWCR = TWCR_START;
    return 1;
  } else {
    return 0;
  }
}

ISR(TWI_vect)
{
  buffer_head_t* b;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    b = currentBuffer;
  }
  if (b != 0) {
    switch (TWSR & ~0x03) {
      case 0x08:
        TWDR = TOSLA(b->receiver, 1);
        // Load SLA+W
        break;
      case 0x10:
        // Load SLA+W or SLA+R
      case 0x18:
        // Load ADDRESSHI
        break;
    }
  }
}