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

#ifndef COMM_USART
#  include <avr/io.h>
#  include <avr/interrupt.h>
#  include <avr/sleep.h>
#  include <util/atomic.h>
#  include <string.h>
#  include "twi_old.h"

static unsigned char TWI_state = TWI_NO_STATE; // State byte. Default set to TWI_NO_STATE.

// This is true when the TWI is in the middle of a transfer
// and set to false when all bytes have been transmitted/received
// Also used to determine how deep we can sleep.
static bool twiBusy = 0;

union TWI_statusReg_t TWI_statusReg = {0}; // TWI_statusReg is defined in TWI_Slave.h

static uint8_t txBuffer[sizeof (TDataPacket)];
static volatile uint8_t txOffset = 0xff;
static uint8_t rxBuffer[sizeof (TDataPacket)];
static volatile uint8_t rxAvail = 0;

void twiInit()
{
  TWAR = eepromFile.address_lsb << TWI_ADR_BITS | _BV(TWI_GEN_BIT);
  TWBR = eepromFile.twibaud;
  TWDR = 0xff;
  TWCR = _BV(TWEN); // init slaveMode
  twiBusy = 0;
}

bool twiStartSlave()
{
  while (twiIsBusy());
  // Wait until TWI is ready for next transmission.
  TWI_statusReg.all = 0;
  TWI_state = TWI_NO_STATE;
  TWCR = _BV(TWEN) | // TWI Interface enabled.
      _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
      _BV(TWEA); // Prepare to ACK next time the Slave is addressed.
  twiBusy = 0;
  return 1;
}

bool twiStartSlaveWithData(TDataPacket* buffer)
{
  while (twiIsBusy());
  memcpy(txBuffer, buffer, sizeof (txBuffer));
  TWI_statusReg.all = 0;
  TWI_state = TWI_NO_STATE;
  TWCR = _BV(TWEN) | // TWI Interface enabled.
      _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
      _BV(TWEA); // Prepare to ACK next time the Slave is addressed.
  twiBusy = 1;
  return 0;
}

bool twiSendData(TDataPacket* buffer)
{
  while (txOffset != 0xff); // warten bis alles gesendet wurde

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    memcpy(txBuffer, buffer, sizeof (txBuffer));
    txOffset = 0;
  }
  if (!twiBusy) {
    TWCR = _BV(TWINT) | _BV(TWSTA) | _BV(TWEN) | _BV(TWIE);
    TWI_statusReg.masterMode = 1;
  }
  return 1;
}

bool twiIsBusy()
{
  return twiBusy || ((TWCR & _BV(TWIE)) != 0);
}

uint8_t twiBytesAvailable()
{
  return twiIsBusy() ? rxAvail : 0;
}

uint8_t twiGetData(TDataPacket* buffer)
{
  while (twiIsBusy());
  uint8_t result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    result = sizeof (TDataPacket);
    memcpy(rxBuffer, buffer, result);
    rxAvail = 0;
  }
  return result;
}

ISR(TWI_vect)
{
  switch (TWSR) {
      // General TWI Master status codes
    case TWI_START: // START has been transmitted
    case TWI_REP_START: // Repeated START has been transmitted
      txOffset = 0;
    case TWI_MTX_ADR_ACK: // SLA+W has been transmitted and ACK received
    case TWI_MTX_ADR_NACK: // SLA+W has been transmitted and NACK received
    case TWI_MTX_DATA_ACK: // Data byte has been transmitted and ACK received
    case TWI_MTX_DATA_NACK: // Data byte has been transmitted and NACK received
      if (txOffset<sizeof (txBuffer)) {
        TWDR = txBuffer[txOffset++];
        TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE);
        twiBusy = 1;
      } else {
        txOffset = 0xff;
        TWDR = 0xff;
        TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWSTO); // send stopbit
        twiBusy = 0;
        TWI_statusReg.masterMode = 0;
      }
      break;
    case TWI_ARB_LOST: // Arbitration lost and not general call or own address
      txOffset = 0;
      TWCR = (1 << TWEN) | // TWI Interface enabled
          (1 << TWIE) | (1 << TWINT) | // Enable TWI Interrupt and clear the flag
          (1 << TWSTA); // Initiate a (RE)START condition.
      twiBusy = 1;
      break;



      // TWI Slave Transmitter status codes

    case TWI_STX_DATA_NACK: // Data byte in TWDR has been transmitted; NACK has been received.
      // I.e. this could be the end of the transmission.
      if (txOffset == 0xff) { // Have we transceiver all expected data?
        TWI_statusReg.lastTransOK = 1; // Set status bits to completed successfully.
      } else {// Master has sent a NACK before all data where sent.
        TWI_state = TWSR; // Store TWI State as error message.
      }
      TWCR = _BV(TWEN) | // Enable TWI-interface and release TWI pins
          _BV(TWIE) | _BV(TWINT) | // Keep interrupt enabled and clear the flag
          _BV(TWEA); // Answer on next address match
      twiBusy = 0; // Transmit is finished, we are not busy anymore
      break;

    case TWI_STX_ADR_ACK: // Own SLA+R has been received; ACK has been returned
      txOffset = 0; // Set buffer pointer to first data location
    case TWI_STX_DATA_ACK: // Data byte in TWDR has been transmitted; ACK has been received
      TWDR = txBuffer[txOffset++];
      TWCR = _BV(TWEN) | // TWI Interface enabled
          _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag to send byte
          _BV(TWEA);
      twiBusy = 1;
      break;

      // TWI Slave Receiver status codes
    case TWI_SRX_ADR_ACK_M_ARB_LOST: // Arbitration lost in SLA+R/W as Master; own SLA+W has been received; ACK has been returned
      TWI_statusReg.genAddressCall = 0;
      break;



    case TWI_SRX_GEN_ACK: // General call address has been received; ACK has been returned
      TWI_statusReg.genAddressCall = 1;
    case TWI_SRX_ADR_ACK: // Own SLA+W has been received ACK has been returned
      // don't need to clear TWI_S_statusRegister.generalAddressCall due to that it is the default state.
      IND_10;
      TWI_statusReg.rxDataInBuf = 1;
      txOffset = 0; // Set buffer pointer to first data location

      // Reset the TWI Interrupt to wait for a new event.
      TWCR = _BV(TWEN) | // TWI Interface enabled
          _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag to send byte
          _BV(TWEA); // Expect ACK on this transmission
      twiBusy = 1;
      break;

    case TWI_SRX_ADR_DATA_ACK: // Previously addressed with own SLA+W; data has been received; ACK has been returned
    case TWI_SRX_GEN_DATA_ACK: // Previously addressed with general call; data has been received; ACK has been returned
      IND_11;
      if (rxAvail<sizeof (rxBuffer)) {
        rxBuffer[rxAvail++] = TWDR;
        TWCR = _BV(TWEN) | // TWI Interface enabled
            _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag to send byte
            _BV(TWEA); // Send ACK after next reception
      } else {
        TWCR = _BV(TWIE) | _BV(TWINT); // Enable TWI Interrupt and clear the flag to send byte
      }
      twiBusy = 1;
      TWI_statusReg.lastTransOK = 1; // Set flag transmission successfully.
      break;
    case TWI_SRX_STOP_RESTART: // A STOP condition or repeated START condition has been received while still addressed as Slave
      // Enter not addressed mode and listen to address match
      IND_21;
      TWCR = _BV(TWEN) | // Enable TWI-interface and release TWI pins
          _BV(TWIE) | _BV(TWINT) | // Enable interrupt and clear the flag
          _BV(TWEA); // Wait for new address match
      twiBusy = 0; // We are waiting for a new address match, so we are not busy
      break;


    case TWI_SRX_GEN_ACK_M_ARB_LOST: // Arbitration lost in SLA+R/W as Master; General call address has been received; ACK has been returned

      // TWI Miscellaneous status codes
    case TWI_NO_STATE: // No relevant state information available; TWINT = 0
    case TWI_SRX_ADR_DATA_NACK: // Previously addressed with own SLA+W; data has been received; NOT ACK has been returned
    case TWI_SRX_GEN_DATA_NACK: // Previously addressed with general call; data has been received; NOT ACK has been returned
    case TWI_STX_DATA_ACK_LAST_BYTE: // Last data byte in TWDR has been transmitted (TWEA = �0�); ACK has been received
    case TWI_BUS_ERROR: // Bus error due to an illegal START or STOP condition
      IND_31;
      TWI_state = TWSR; //Store TWI State as error message, operation also clears noErrors bit
      TWCR = (1 << TWSTO) | (1 << TWINT); //Recover from TWI_BUS_ERROR, this will release the SDA and SCL pins thus enabling other devices to use the bus
      break;
    case TWI_MRX_ADR_ACK: // SLA+R has been transmitted and ACK received
    case TWI_MRX_ADR_NACK: // SLA+R has been transmitted and NACK received
    case TWI_MRX_DATA_ACK: // Data byte has been received and ACK transmitted
    case TWI_MRX_DATA_NACK: // Data byte has been received and NACK transmitted
    default:
      IND_31;
      TWI_state = TWSR; // Store TWI State as error message, operation also clears the Success bit.
      TWCR = (1 << TWEN) | // Enable TWI-interface and release TWI pins
          (1 << TWIE) | (1 << TWINT) | // Keep interrupt enabled and clear the flag
          (1 << TWEA) | (0 << TWSTA) | (0 << TWSTO) | // Acknowledge on any new requests.
          (0 << TWWC); //

      twiBusy = 0; // Unknown status, so we wait for a new address match that might be something we can handle
      // TWI Master Receiver status codes
      break;
  }
  IND_00;
  IND_10;
  IND_20;
  IND_30;
}


#endif