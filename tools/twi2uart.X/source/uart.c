#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>
#include <string.h>
#include "uart.h"
#include "config.h"

#if UART_ENABLED==1

#  define USARTBUFSIZE sizeof(TDataPacket)

static uint8_t rxBuf[USARTBUFSIZE];
static volatile uint8_t rxAvail;
static uint8_t txBuf[USARTBUFSIZE];
static volatile uint8_t txOffset;
static volatile uint8_t txAvail;

void initCommUsart()
{
  UBRRH = UART_BAUD >> 8;
  UBRRL = UART_BAUD;
  UCSRB = _BV(RXEN) | _BV(TXEN) | _BV(RXCIE);
  UCSRC = _BV(URSEL) | _BV(UCSZ0) | _BV(UCSZ1);
  txOffset = USARTBUFSIZE; // nothing to send
  rxAvail = 0;
}

inline void enableUDRIE()
{
  UCSRB |= _BV(UDRIE);
}

inline void disableUDRIE()
{
  UCSRB &= ~_BV(UDRIE);
}

ISR(USART_RXC_vect)
{
  if (rxAvail < USARTBUFSIZE) {
    rxBuf[rxAvail++] = UDR;
  }
}

ISR(USART_UDRE_vect)
{
  if (txOffset < USARTBUFSIZE && txAvail > 0) {
    UDR = txBuf[txOffset++];
    --txAvail;
  } else {
    txOffset = USARTBUFSIZE;
    disableUDRIE();
  }
}

uint8_t getBytesAvailableUsart()
{
  return rxAvail;
}

uint8_t readBytes(uint8_t* buffer, uint8_t bytesToRead)
{
  uint8_t toCopy = bytesToRead;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    if (toCopy > rxAvail) {
      toCopy = rxAvail;
    }
    memcpy(buffer, rxBuf, toCopy);
    rxAvail = 0;
  }
  return toCopy;
}

bool isTxReady()
{
  return txAvail == 0;
}

void writeBytesUsart(uint8_t* buffer, uint8_t bytesToSend)
{
  uint8_t toCopy = bytesToSend;
  if (toCopy > USARTBUFSIZE) {
    toCopy = USARTBUFSIZE;
  }
  memcpy(txBuf, buffer, toCopy);

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    enableUDRIE();
    txAvail = toCopy;
    txOffset = 0;
  }
}

#endif