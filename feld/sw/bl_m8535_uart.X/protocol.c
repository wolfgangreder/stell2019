#include <avr/io.h>
#include "protocol.h"
#include "crc.h"

void initUART()
{
  UBRRL = 23;
  UBRRH = 0;
  UCSRB = (1 << RXEN | 1 << TXEN);
  //  UCSRC = (1 << URSEL | 1 << UPM1 | 1 << UCSZ0 | 1 << UCSZ1); // even parity; 8 databits
  UCSRC = (1 << URSEL | 1 << UCSZ0 | 1 << UCSZ1); // no parity; 8 databits
}

uint8_t pollByte()
{
  while (!(UCSRA & (1 << RXC)));
  return UDR;
}

void sendBuffer(uint8_t* buffer, uint8_t toSend)
{
  uint8_t* ptr = buffer;
  for (int i = 0; i < toSend; ++i) {
    while (!(UCSRA & (1 << UDRE)));
    UDR = ptr[i];
  }

}

void sendCommandBuffer(command_t* cmd)
{
  sendBuffer((uint8_t*) cmd, sizeof (command_header_t) + cmd->header.size);
}
