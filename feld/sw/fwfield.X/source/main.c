/**
 * \file
 *
 * \brief Application to generate sample driver to AVRs TWI module
 *
 * Copyright (C) 2014-2015 Atmel Corporation. All rights reserved.
 *
 * \asf_license_start
 *
 * \page License
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name of Atmel may not be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * 4. This software may only be redistributed and used in connection with an
 *    Atmel micro controller product.
 *
 * THIS SOFTWARE IS PROVIDED BY ATMEL "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT ARE
 * EXPRESSLY AND SPECIFICALLY DISCLAIMED. IN NO EVENT SHALL ATMEL BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * \asf_license_stop
 *
 */
/*
 * Support and FAQ: visit <a href="http://www.atmel.com/design-support/">Atmel Support</a>
 */

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/eeprom.h>
#include <avr/pgmspace.h>
#ifdef COMM_USART
#  include "comm.h"
#else
#  include "TWI_slave.h"
#endif
#include "avr/sleep.h"
#include "hw.h"

TRegisterFile registerFile;
EEMEM TEEPromFile ee_eepromFile = {.address = TWI_ADDRESS, .debounce = 20, .moduletype = 0, .softstart = 0, .softstop = 0};
TEEPromFile eepromFile;
const PROGMEM TFlashFile fl_flashFile = {.fw_major = FW_MAJOR, .fw_minor = FW_MINOR, .fw_build = FW_BUILD};
TFlashFile flashFile;

uint16_t processCommand(TCommandBuffer* cmd)
{
  switch (cmd->registerAddress) {
    case REG_LED:
      return processLed(cmd->data[0], cmd->registerOperation);
    case REG_BLINKMASK:
      return processBlinkMask(cmd->data[0], cmd->registerOperation);
    case REG_BLINKPHASE:
      return processBlinkPhase(cmd->data[0], cmd->registerOperation);
    case REG_PWM:
      return processPWM(cmd->data[0], cmd->registerOperation);
    case REG_VCC:
      return processVCC();
  }
  return -1;
}
#ifdef COMM_USART

int main(void)
{
  TCommandBuffer commandBuf;
  DDRD |= _BV(PD7);
  PORTD |= _BV(PD7);

  initHW(&registerFile, &eepromFile);
  initCommUsart(eepromFile.address_lsb);

  sei(); //set global interrupt enable

  for (;;) {
    // Check if the last operation was a reception
    if (getBytesAvailableUsart() == sizeof (commandBuf)) {
      readBytes((uint8_t*) & commandBuf, sizeof (commandBuf));
      sendACK();
      if (commandBuf.registerOperation == OP_READ) {
        uint16_t data = processCommand(&commandBuf);
        writeBytesUsart((uint8_t*) & data, sizeof (data));
      } else {
        processCommand(&commandBuf);
      }
      IND_0;
    }
  }
}
#else
// When there has been an error, this function is run and takes care of it
unsigned char TWI_Act_On_Failure_In_Last_Transmission(unsigned char TWIerrorMsg);

int main(void)
{
  TCommandBuffer commandBuf;

  initHW(&registerFile, &eepromFile);


  // Initialize TWI module for slave operation. Include address and/or enable General Call.
  TWI_Slave_Initialise((unsigned char) ((eepromFile.address_lsb << TWI_ADR_BITS) | (TRUE << TWI_GEN_BIT)));

  sei(); //set global interrupt enable

  // Start the TWI transceiver to enable reception of the first command from the TWI Master.
  TWI_Start_Transceiver();

  // This example is made to work together with the AVR315 TWI Master application note. In addition to connecting the TWI
  // pins, also connect PORTB to the LEDS. The code reads a message as a TWI slave and acts according to if it is a
  // general call, or an address call. If it is an address call, then the first byte is considered a command byte and
  // it then responds differently according to the commands.

  // This loop runs forever. If the TWI is busy the execution will just continue doing other operations.
  for (;;) {
    asm("nop"); // Put own code here.
    // Check if the TWI Transceiver has completed an operation.
    if (!TWI_Transceiver_Busy()) {
      // Check if the last operation was successful
      if (TWI_statusReg.lastTransOK) {
        // Check if the last operation was a reception
        if (TWI_statusReg.RxDataInBuf) {
          TWI_Get_Data_From_Transceiver((uint8_t*) & commandBuf, sizeof (commandBuf));
          // Check if the last operation was a reception as General Call
          if (TWI_statusReg.genAddressCall) {
            // Put data received out to PORTB as an example.
            // PORTB = messageBuf[0];
          } else // Ends up here if the last operation was a reception as Slave Address Match
          {
            if (commandBuf.registerOperation == OP_READ) {
              uint16_t data = processCommand(&commandBuf);
              TWI_Start_Transceiver_With_Data((uint8_t*) & data, sizeof (data));
            } else {
              processCommand(&commandBuf);
            }
          }
        } else // Ends up here if the last operation was a transmission
        {
          asm("nop"); // Put own code here.
        }
        // Check if the TWI Transceiver has already been started.
        // If not then restart it to prepare it for new receptions.
        if (!TWI_Transceiver_Busy()) {
          TWI_Start_Transceiver();
        }
      } else // Ends up here if the last operation completed unsuccessfully
      {
        TWI_Act_On_Failure_In_Last_Transmission(TWI_Get_State_Info());
      }
    }
  }
}

unsigned char TWI_Act_On_Failure_In_Last_Transmission(unsigned char TWIerrorMsg)
{
  // A failure has occurred, use TWIerrorMsg to determine the nature of the failure
  // and take appropriate actions.
  // Se header file for a list of possible failures messages.

  // This very simple example puts the error code on PORTB and restarts the transceiver with
  // all the same data in the transmission buffers.
  TWI_Start_Transceiver();

  return TWIerrorMsg;
}
#endif



/*
 // A simplified example.
 // This will store data received on PORTB, and increment it before sending it back.

 TWI_Start_Transceiver( );

 for(;;)
 {
   if ( ! TWI_Transceiver_Busy() )
   {
     if ( TWI_statusReg.RxDataInBuf )
     {
       TWI_Get_Data_From_Transceiver(&temp, 1);
       PORTB = temp;
     }
     temp = PORTB + 1;
     TWI_Start_Transceiver_With_Data(&temp, 1);
   }
   asm("nop");   // Do something else while waiting
 }
}
 */

