/*
 * File:   main.c
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 16:52
 */
#include <avr/io.h>
#include <avr/cpufunc.h>
#include <avr/eeprom.h>
#include <util/atomic.h>
#include <string.h>
#include "buffer.h"
#include "uart.h"
#include "twi.h"
//FUSES = {
//  (FUSE_BODEN | FUSE_SUT1),
//  HFUSE_DEFAULT
//};




uint16_t EEMEM blinkScale = 473;
// addressen 0x08 bis 0x77 sind als 7bit address verfügbar
uint16_t EEMEM twiAddress = 0x08;



#define CS (_BV(CS12)|_BV(CS10)) // 14.74 MHz
#define WGM (_BV(WGM12))

void initTimer()
{
  DDRD |= (_BV(DD5)); // OC1A output
  OCR1A = eeprom_read_word(&blinkScale);
  TCCR1A = (_BV(COM1A0)); // OC1
  TCCR1B = CS | WGM;
}

/*
 *
 */
int main()
{
  initTimer();
  initUART();
  initTWI();
  sei();
  while (1) {
    if (isInProgramMode()) {
      sendTWI((buffer_head_t*) getProgramBuffer());
    } else {
      if (isCommandAvailable() && isTWIReadyToSend()) {
        sendTWI((buffer_head_t*) getCommandDataBuffer());
      }
      if (isEventAvailable()) {
        sendUARTData((buffer_head_t*) getCommandEventBuffer(), sizeof (command_buffer_t));
      }
    }
  };
  return 0;
}

