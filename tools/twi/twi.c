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
#include <avr/sleep.h>
#include <util/atomic.h>
#include <string.h>
#include "twi.h"
#include "config.h"

typedef enum {
  INIT,
  SLAVE_READ,
  SLAVE_WRITE,
  MASTER_READ,
  MASTER_WRITE
} twi_mode_t;

static volatile unsigned char TWI_state = TWI_NO_STATE; // State byte. Default set to TWI_NO_STATE.

// This is true when the TWI is in the middle of a transfer
// and set to false when all bytes have been transmitted/received
// Also used to determine how deep we can sleep.
static volatile twi_mode_t twiMode;
static volatile bool twiBusy;
union TWI_statusReg_t TWI_statusReg = {0}; // TWI_statusReg is defined in TWI_Slave.h

static uint8_t txBuffer[sizeof (TDataPacket)];
static volatile uint8_t txAvail = 0;
static volatile uint8_t txOffset = 0xff;
static uint8_t rxBuffer[sizeof (TDataPacket)];
static volatile uint8_t rxAvail = 0;

void twiInit(uint16_t ownAddress, uint8_t twiBaud, bool genCall)
{
  TWAR = (ownAddress << TWI_ADR_BITS) | (genCall ? _BV(TWI_GEN_BIT) : 0);
  TWBR = twiBaud;
  TWDR = 0xff;
  TWCR = (1 << TWEN) | // Enable TWI-interface and release TWI pins.
      (0 << TWIE) | (0 << TWINT) | // Disable TWI Interrupt.
      (0 << TWEA) | (0 << TWSTA) | (0 << TWSTO) | // Do not ACK on any requests, yet.
      (0 << TWWC);
  twiMode = INIT;
  memset(txBuffer, 0, sizeof (txBuffer));
  memset(rxBuffer, 0, sizeof (rxBuffer));
}

bool twiStartSlave()
{
  while (twiIsBusy());
  IND_1_ON;
  // Wait until TWI is ready for next transmission.
  TWI_statusReg.all = 0;
  TWI_state = TWI_NO_STATE;
  TWCR = _BV(TWEN) | // TWI Interface enabled.
      _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
      _BV(TWEA); // Prepare to ACK next time the Slave is addressed.
  twiMode = SLAVE_READ;
  twiBusy = 0;
  rxAvail = 0;
  return 1;
}

bool twiStartSlaveWithData(TDataPacket * buffer)
{
  while (twiIsBusy());
  memcpy(txBuffer, buffer, sizeof (TDataPacket));
  txAvail = sizeof (TDataPacket);
  TWI_statusReg.all = 0;
  TWI_state = TWI_NO_STATE;
  TWCR = _BV(TWEN) | // TWI Interface enabled.
      _BV(TWIE) | _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
      _BV(TWEA); // Prepare to ACK next time the Slave is addressed.
  twiMode = SLAVE_WRITE;
  return 0;
}

bool twiSendData(TDataPacket * buffer)
{
  bool wait;
  do {

    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      wait = txOffset != 0xff || twiBusy;
      if (!wait) {
        memcpy(txBuffer, buffer, sizeof (TDataPacket));
        txAvail = sizeof (TDataPacket);
        txOffset = 0;
        twiBusy = 1;
        TWCR = _BV(TWINT) | _BV(TWSTA) | _BV(TWEN) | _BV(TWIE);
        twiMode = MASTER_WRITE;
      }
    }
  } while (wait);
  return 1;
}

bool twiSendReceiveData(TDataPacket * buffer)
{
  bool wait;
  do {

    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      wait = txOffset != 0xff || twiBusy;
      if (!wait) {
        memcpy(txBuffer, buffer, sizeof (TDataPacket));
        txBuffer[0] &= ~_BV(TWI_READ_BIT);
        txAvail = sizeof (TDataPacket);
        txOffset = 0;
        twiBusy = 1;
        TWCR = _BV(TWINT) | _BV(TWSTA) | _BV(TWEN) | _BV(TWIE);
        twiMode = MASTER_READ;
      }
    }
  } while (wait);
  return 1;
}

ack_t getAck()
{
  return TWI_statusReg.ack;
}

bool twiIsBusy()
{
  bool result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    result = twiBusy || twiMode != INIT;
  }
  return result;
}

uint8_t twiBytesAvailable()
{
  return twiIsBusy() ? 0 : rxAvail;
}

bool twiIsPacketAvailable()
{
  bool result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    result = !twiBusy && TWI_statusReg.lastTransOK;
  }
  return result;
}

uint8_t twiGetData(TDataPacket * buffer)
{
  uint8_t result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    result = sizeof (TDataPacket);
    memcpy(buffer, rxBuffer, result);
    rxAvail = 0;
  }
  return result;
}

bool twiPollData(TDataPacket * buffer)
{
  bool result = 0;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    if (twiBusy && TWI_statusReg.lastTransOK) {
      memcpy(buffer, rxBuffer, sizeof (TDataPacket));
      rxAvail = 0;
      twiMode = INIT;
      result = 1;
      twiBusy = 0;
    }
  }
  return result;
}

ISR(TWI_vect)
{
  uint8_t twiState = TWSR & 0xf8;
  switch (twiMode) {
    case SLAVE_READ:
      switch (twiState) {
        case TWI_SRX_ADR_ACK://            0x60  // Own SLA+W has been received ACK has been returned
          rxBuffer[0] = 6;
          rxAvail = 1;
          TWCR = _BV(TWINT) | _BV(TWEA) | _BV(TWEN) | _BV(TWIE);
          twiBusy = 1;
          break;
        case TWI_SRX_ADR_DATA_ACK://       0x80  // Previously addressed with own SLA+W; data has been received; ACK has been returned
        case TWI_SRX_ADR_DATA_NACK://      0x88  // Previously addressed with own SLA+W; data has been received; NOT ACK has been returned
          if (rxAvail<sizeof (rxBuffer)) {
            rxBuffer[rxAvail++] = TWDR;
          }
          TWCR = _BV(TWINT) | _BV(TWEA) | _BV(TWEN) | _BV(TWIE);
          break;
        case TWI_SRX_STOP_RESTART://       0xA0  // A STOP condition or repeated START condition has been received while still addressed as Slave
          TWI_statusReg.rxDataInBuf = 1;
          TWI_statusReg.lastTransOK = rxAvail == sizeof (rxBuffer);
          TWCR = _BV(TWINT) | _BV(TWEN);
          break;
        case TWI_SRX_GEN_DATA_ACK://       0x90  // Previously addressed with general call; data has been received; ACK has been returned
        case TWI_SRX_GEN_DATA_NACK://      0x98  // Previously addressed with general call; data has been received; NOT ACK has been returned
        case TWI_SRX_GEN_ACK://            0x70  // General call address has been received; ACK has been returned
        case TWI_BUS_ERROR://              0x00  // Bus error due to an illegal START or STOP condition
        case TWI_NO_STATE:
        default:
          TWI_statusReg.all = 0;
          TWCR = _BV(TWINT) | _BV(TWSTO) | _BV(TWEN);
          twiMode = SLAVE_READ;
          twiBusy = 0;
          rxAvail = 0;
      }
      break;
    case SLAVE_WRITE:
      switch (twiState) {
        case TWI_STX_ADR_ACK://            0xA8  // Own SLA+R has been received; ACK has been returned
        case TWI_STX_DATA_ACK://           0xB8  // Data byte in TWDR has been transmitted; ACK has been received
        case TWI_STX_DATA_NACK://          0xC0  // Data byte in TWDR has been transmitted; NOT ACK has been received
          if (txOffset < txAvail) {
            TWDR = txBuffer[txOffset++];
          } else {
            txOffset = 0xff;
            TWDR = 0xff;
          }
          TWCR = _BV(TWINT) | _BV(TWEN) | _BV(TWIE) | _BV(TWEA);
          break;
        case TWI_STX_DATA_ACK_LAST_BYTE:// 0xC8  // Last data byte in TWDR has been transmitted (TWEA = 0); ACK has been received
        case TWI_BUS_ERROR://              0x00  // Bus error due to an illegal START or STOP condition
        case TWI_NO_STATE:
        default:
          TWI_statusReg.all = 0;
          TWCR = _BV(TWINT) | _BV(TWSTO) | _BV(TWEN);
          twiMode = SLAVE_READ;
          twiBusy = 0;
          rxAvail = 0;
      }
      break;
    case MASTER_READ:
      switch (twiState) {
        case TWI_START://                  0x08  // START has been transmitted
          txOffset = 0;
          TWDR = txBuffer[txOffset++];
          TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA);
          break;
        case TWI_REP_START://              0x10  // Repeated START has been transmitted
          TWDR = txBuffer[0] |= _BV(TWI_READ_BIT);
          rxBuffer[0] = TWDR;
          rxAvail = 1;
          TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA);
          twiMode = MASTER_READ;
          break;
        case TWI_ARB_LOST://               0x38  // Arbitration lost
          TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA) | _BV(TWSTA);
          break;
        case TWI_MTX_ADR_ACK://            0x18  // SLA+W has been transmitted and ACK received
        case TWI_MTX_DATA_ACK://           0x28  // Data byte has been transmitted and ACK received
          if (txOffset < txAvail) {
            TWDR = txBuffer[txOffset++];
            TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA);
          } else {
            txOffset = 0xff;
            twiBusy = 0;
            twiMode = INIT;
            rxAvail = 0;
            TWI_statusReg.all = 0;
            TWI_statusReg.ack = ACK_ACK;
            TWCR = _BV(TWINT) | _BV(TWSTO) | _BV(TWEN);
          }
          break;
        case TWI_SRX_ADR_ACK_M_ARB_LOST:// 0x68  // Arbitration lost in SLA+R/W as Master; own SLA+W has been received; ACK has been returned
        case TWI_STX_ADR_ACK_M_ARB_LOST:// 0xB0  // Arbitration lost in SLA+R/W as Master; own SLA+R has been received; ACK has been returned
          twiMode = SLAVE_READ;
          rxAvail = 1;
          rxBuffer[0] = TWDR;
          TWCR = _BV(TWINT) | _BV(TWIE) | _BV(TWEN);
          TWI_statusReg.rxDataInBuf = 1;
          break;
        case TWI_MRX_ADR_ACK://            0x40  // SLA+R has been transmitted and ACK received
          rxAvail = 0;
        case TWI_MRX_DATA_ACK://           0x50  // Data byte has been received and ACK transmitted
          rxBuffer[rxAvail++] = TWDR;
          TWCR = _BV(TWINT) | _BV(TWIE) | _BV(TWEN) | _BV(TWEA);
          break;
        case TWI_MTX_DATA_NACK://          0x30  // Data byte has been transmitted and NACK received
        case TWI_MRX_ADR_NACK://           0x48  // SLA+R has been transmitted and NACK received
        case TWI_MRX_DATA_NACK://          0x58  // Data byte has been received and NACK transmitted
        case TWI_MTX_ADR_NACK://           0x20  // SLA+W has been transmitted and NACK received
          TWI_statusReg.all = 0;
          TWI_statusReg.ack = ACK_NACK;
          TWCR = _BV(TWEN) | // TWI Interface enabled.
              _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
              _BV(TWEA) | _BV(TWSTO) | _BV(TWIE); // Prepare to ACK next time the Slave is addressed.
          twiMode = INIT;
          twiBusy = 0;
          rxAvail = 0;
          txOffset = 0xff;
          break;
        case TWI_SRX_GEN_ACK_M_ARB_LOST:// 0x78  // Arbitration lost in SLA+R/W as Master; General call address has been received; ACK has been returned
        case TWI_BUS_ERROR://              0x00  // Bus error due to an illegal START or STOP condition
        case TWI_NO_STATE:
        default:
          TWI_statusReg.all = 0;
          TWCR = _BV(TWEN) | // TWI Interface enabled.
              _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
              _BV(TWEA) | _BV(TWSTO) | _BV(TWIE); // Prepare to ACK next time the Slave is addressed.
          twiMode = INIT;
          twiBusy = 0;
          rxAvail = 0;
          txOffset = 0xff;
      }
      break;
    case MASTER_WRITE:
      switch (twiState) {
        case TWI_START://0x08  // START has been transmitted
          txOffset = 0;
          TWDR = txBuffer[txOffset++];
          TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA);
          break;
        case TWI_REP_START://              0x10  // Repeated START has been transmitted
          TWDR = txBuffer[0] |= _BV(TWI_READ_BIT);
          rxBuffer[0] = TWDR;
          rxAvail = 0;
          TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA);
          twiMode = MASTER_READ;
          break;
        case TWI_ARB_LOST://               0x38  // Arbitration lost
          TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA) | _BV(TWSTA);
          txOffset = 0;
          break;
        case TWI_MTX_ADR_ACK://            0x18  // SLA+W has been transmitted and ACK received
        case TWI_MTX_DATA_ACK://           0x28  // Data byte has been transmitted and ACK received
          if (txOffset < txAvail) {
            TWDR = txBuffer[txOffset++];
            TWCR = _BV(TWEN) | _BV(TWINT) | _BV(TWIE) | _BV(TWEA);
          } else {
            txOffset = 0xff;
            twiBusy = 0;
            twiMode = INIT;
            TWI_statusReg.all = 0;
            TWI_statusReg.ack = ACK_ACK;
            TWCR = _BV(TWINT) | _BV(TWSTO) | _BV(TWEN);
          }
          break;
        case TWI_SRX_ADR_ACK_M_ARB_LOST:// 0x68  // Arbitration lost in SLA+R/W as Master; own SLA+W has been received; ACK has been returned
        case TWI_STX_ADR_ACK_M_ARB_LOST:// 0xB0  // Arbitration lost in SLA+R/W as Master; own SLA+R has been received; ACK has been returned
          twiMode = SLAVE_READ;
          rxAvail = 1;
          rxBuffer[0] = TWDR;
          TWCR = _BV(TWINT) | _BV(TWIE) | _BV(TWEN);
          TWI_statusReg.rxDataInBuf = 1;
          break;
        case TWI_MTX_DATA_NACK://          0x30  // Data byte has been transmitted and NACK received
        case TWI_MTX_ADR_NACK://           0x20  // SLA+W has been transmitted and NACK received
          TWI_statusReg.all = 0;
          TWI_statusReg.ack = ACK_NACK;
          TWCR = _BV(TWEN) | // TWI Interface enabled.
              _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
              _BV(TWEA) | _BV(TWSTO) | _BV(TWIE); // Prepare to ACK next time the Slave is addressed.
          twiMode = INIT;
          twiBusy = 0;
          rxAvail = 0;
          txOffset = 0xff;
          break;
        case TWI_SRX_GEN_ACK_M_ARB_LOST:// 0x78  // Arbitration lost in SLA+R/W as Master; General call address has been received; ACK has been returned
        case TWI_BUS_ERROR://              0x00  // Bus error due to an illegal START or STOP condition
        case TWI_NO_STATE:
        default:
          TWI_statusReg.all = 0;
          TWCR = _BV(TWEN) | // TWI Interface enabled.
              _BV(TWINT) | // Enable TWI Interrupt and clear the flag.
              _BV(TWEA) | _BV(TWSTO) | _BV(TWIE); // Prepare to ACK next time the Slave is addressed.
          twiMode = INIT;
          twiBusy = 0;
          rxAvail = 0;
          txOffset = 0xff;
      }
      break;
    default:
      TWCR = _BV(TWINT);
  }
  IND_0_OFF;
}


