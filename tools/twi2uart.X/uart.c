#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/eeprom.h>
#include <util/atomic.h>
#include "uart.h"
#include "buffer.h"
#define RS_IDLE 0
#define RS_STX_READ 1
#define DLE_READ 2


uint16_t EEMEM ubrr = 95; // 9600
volatile uint8_t* sendBuffer;
volatile uint8_t bytesToSend;
volatile uint8_t bufferOffset;
uint8_t receiverState;
uint8_t readOffset;

void initUART()
{
  UCSRB = _BV(RXCIE) | _BV(UDRIE) | _BV(RXEN) | _BV(TXEN);
  UCSRC = _BV(UCSZ1) | _BV(UCSZ0);
  uint16_t ub = eeprom_read_word(&ubrr);
  UBRRH = ub >> 8;
  UBRRL = 95;
  receiverState = RS_IDLE;
}

uint8_t isUARTReadyToSend()
{
  return sendBuffer == 0;
}

uint8_t sendUARTData(buffer_head_t* buffer)
{
  if (sendBuffer == 0) {

    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      sendBuffer = (uint8_t*) buffer;
      if (buffer->command == CMD_PROGRAM) {
        bytesToSend = sizeof (program_buffer_t);
      } else {
        bytesToSend = sizeof (command_buffer_t);
      }
      bufferOffset = 0;
      if (buffer != 0 && bytesToSend > 0) {
        while (!(UCSRA & _BV(UDRE)));
        UDR = sendBuffer[0];
        bufferOffset++;
      }
    }
  }
  return 0;
}

ISR(USART_RX_vect)
{
  uint8_t b = UDR;
  switch (receiverState) {
    case RS_IDLE:
      if (b == STX) {
        receiverState = RS_STX_READ;
        readOffset = 0;
      }
      break;
    case RS_STX_READ:
      switch (b) {
        case DLE:
          receiverState = DLE_READ;
          break;
        case EOT:
          publishCommand();
          receiverState = RS_IDLE;
          break;
        default:
          getBuffer()->rawData[readOffset++] = b;
      }
      break;
    case DLE_READ:
      getBuffer()->rawData[readOffset++] = b;
      receiverState = RS_STX_READ;
      break;
  }
}

ISR(USART_UDRE_vect)
{
  if (sendBuffer != 0 && bufferOffset < bytesToSend) {
    UDR = sendBuffer[bufferOffset++];
  } else {
    sendBuffer = 0;
  }
}