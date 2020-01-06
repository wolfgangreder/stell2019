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
#include "comm.h"
#include "twi.h"
#include "hw.h"
#include "symbols.h"

TRegisterFile registerFile;
EEMEM TEEPromFile ee_eepromFile = {.address = TWI_ADDRESS,
  .debounce = 5,
  .moduletype = 0,
  .softstart = 0,
  .softstop = 0,
  .vcc_calibration = 9,
  .defaultPWM = 100,
  .twibaud = BAUD_TWI,
  .twipresc = PRESC_TWI,
  .masterAddress = 1,
  .featureControl = 0};
TEEPromFile eepromFile;
const PROGMEM TFlashFile fl_flashFile = {.fw_major = FW_MAJOR, .fw_minor = FW_MINOR};
TFlashFile flashFile;

uint16_t processCommand(TDataPacket* cmd)
{
  switch (cmd->registerAddress) {
    case REG_STATE:
      return processState(cmd->data[0], cmd->registerOperation);
    case REG_MODULETYPE:
      return processModuleType(cmd->data[0], cmd->registerOperation);
    case REG_MODULESTATE:
      return processModuleState(cmd->data[0], cmd->registerOperation);
    case REG_LED:
      return processLed(cmd->data[0], cmd->registerOperation);
    case REG_BLINKMASK:
      return processBlinkMask(cmd->data[0], cmd->registerOperation);
    case REG_BLINKPHASE:
      return processBlinkPhase(cmd->data[0], cmd->registerOperation);
    case REG_BLINKDIVIDER:
      return processBlinkDivider(cmd->data[0], cmd->registerOperation);
    case REG_PWM:
      return processPWM(cmd->data[0], cmd->registerOperation);
    case REG_DEFAULT_PWM:
      return processDefaultPWM(cmd->data[0], cmd->registerOperation);
    case REG_VCC:
      return processVCC();
    case REG_VCC_CALIBRATION:
      return processVCCCalibration(cmd->data[0], cmd->registerOperation);
    case REG_DEBOUNCE:
      return processDebounce(cmd->data[0], cmd->registerOperation);
    case REG_FEATURE_CONTROL:
      IND_01;
      return processFeatureControl(cmd->data[0], cmd->registerOperation);
    case REG_VERSION:
      return processFirmwareVersion();

  }
  return -1;
}
#ifdef COMM_USART

int main(void)
{
  TCommandBuffer commandBuf;
  IND_INIT;

  initHW();
  initModule();
  initCommUsart();

  sei(); //set global interrupt enable

  for (;;) {
    if (getBytesAvailableUsart() == sizeof (commandBuf)) {
      readBytes((uint8_t*) & commandBuf, sizeof (commandBuf));
      if (commandBuf.registerOperation == OP_READ) {
        uint16_t data = processCommand(&commandBuf);
        writeBytesUsart((uint8_t*) & data, sizeof (data));
      } else {
        processCommand(&commandBuf);
        sendACK();
      }
    }
  }
}
#else

int main(void)
{
  TDataPacket commandBuf;
  IND_INIT;

  initHW();
  initModule();
  twiInit();
  sei(); //set global interrupt enable
  for (;;) {
    if (!twiIsBusy()) {
      twiStartSlave();
    }
    if (twiBytesAvailable() == sizeof (commandBuf)) {
      twiGetData(&commandBuf);
      if (commandBuf.registerOperation == OP_READ) {
        commandBuf.slaveAddress = eepromFile.masterAddress;
        commandBuf.ownAddress = eepromFile.address;
        commandBuf.wdata = processCommand(&commandBuf);
        twiStartSlaveWithData(& commandBuf);
      } else {
        processCommand(&commandBuf);
      }
    }
  }
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

