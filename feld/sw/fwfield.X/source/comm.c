#include "comm.h"

void initCommTwi()
{

}

uint8_t readByteTwi()
{
  return 0;
}

void writeByteTwi(uint8_t byte)
{
}
//
//ISR(USART0_RX_vect)
//{
//  uint8_t usr = UCSRA & (1 << FE | 1 << DOR);
//  if (state!=S_PROCESS_DATA && usr == 0) {
//    buffer.buffer[buffer.bufferOffset++] = UDR;
//    if (buffer.bufferOffset>sizeof(usartblock_t)) {
//      state = S_PROCESS_DATA;
//    }
//  } else {
//    state = S_ERROR;
//    usr = UDR; //UDR lesen damit kein interrupt mehr auftritt
//  }
//}

__attribute__((section(".bootloader"))) void initCommUsart()
{
#if FREQ==14745600
  UBRRL = 191;
  UBRRH = 0;
#elif FREQ==8000000
  UBRRL = 51; // 9600@8Mhz
  UBRRH = 0;
#elif FREQ==3686400
  UBRRL = 47;
  UBRRH = 0;
#else
#  error unknown frequency FREQ
#endif
  UCSRB = (1 << RXEN | 1 << TXEN | 1 << RXCIE);
  UCSRC = (1 << UPM1 | 1 << UCSZ0 | 1 << UCSZ1); // even parity; 8 databits
}

extern uint8_t readByteUsart()
{
  return 0;
}

extern void writeByteUsart(uint8_t byte)
{
}
