#include "hw.h"
#include "protocol.h"
#include "comm.h"
#include <string.h>
#include <avr/interrupt.h>
command_t buffer;
volatile uint8_t offset;

ISR(USART_RX_vect, __attribute__((section(".bootloader"))))
{
  uint8_t usr = UCSRA & (1 << FE | 1 << DOR);
  if (usr == 0) {
    //    buffer.buffer[buffer.bufferOffset++] = UDR;
    //    if (buffer.bufferOffset>sizeof (usartblock_t)) {
    //      state = S_PROCESS_DATA;
    //    }
  } else {
    offset = 0;
    usr = UDR; //UDR lesen damit kein interrupt mehr auftritt
  }
}

void initProtocol()
{
  memset(&buffer, 0, sizeof (buffer));
  offset = 0;
}