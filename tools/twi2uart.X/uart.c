#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/eeprom.h>
#include <util/atomic.h>
#include "uart.h"

uint16_t EEMEM ubrr = 95; // 9600
volatile void* sendBuffer;
volatile uint8_t bytesToSend;
volatile uint8_t bufferOffset;

void initUART()
{
  UCSRB = _BV(RXCIE) | _BV(UDRIE) | _BV(RXEN) | _BV(TXEN);
  UCSRC = _BV(UCSZ1) | _BV(UCSZ0);
  uint16_t ub = eeprom_read_word(&ubrr);
  UBRRH = ub >> 8;
  UBRRL = 95;
}

uint8_t isUARTReadyToSend()
{
  return sendBuffer == 0;
}

uint8_t sendUARTData(void* buffer, uint8_t bts)
{
  if (sendBuffer == 0) {

    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      sendBuffer = buffer;
      bytesToSend = bts;
      bufferOffset = 0;
    }
  }
  return 0;
}

ISR(USART_RX_vect)
{
  // rx
}

ISR(USART_UDRE_vect)
{
  // tx
}