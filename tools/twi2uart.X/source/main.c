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
#include "uart.h"
#include "twi.h"
#include "config.h"
#include "spi.h"

FUSES = {
  (FUSE_SUT1 | FUSE_SUT0 | FUSE_CKSEL3 | FUSE_CKSEL1 | FUSE_CKSEL0 | FUSE_BODEN),
  (FUSE_SPIEN | FUSE_EESAVE | FUSE_BOOTSZ0 | FUSE_BOOTSZ1)
};

void processCommand(TDataPacket* packet, packetsource_t source)
{
  bool isRead;
  switch (source) {
    case UART:
    case SPI:
      isRead = packet->rxRegisterOperation == OP_READ;
      packet->txSlaveAddress = packet->txSlaveAddress << TWI_ADR_BITS;
      if (isRead) {
        twiSendReceiveData(packet);
      } else {
        twiSendData(packet);
      }
      break;
    case TWI:
#if UART_ENABLED==1
      IND_0_ON;
      writeBytesUsart((uint8_t*) packet, sizeof (TDataPacket));
#endif
      //      spiWriteData(packet);
      break;
  }
}

void doSendAck()
{
#if UART_ENABLED==1
  sendACK();
#endif
}

void sendNack(TDataPacket* packet)
{
  memset(packet, 0xff, sizeof (TDataPacket));
  writeBytesUsart((uint8_t*) packet, sizeof (TDataPacket));
}

void initHW()
{
  BLINK_DIR |= _BV(BLINK); // output !
  OCR1A = TIMER1_TOP;
  TCCR1A = _BV(COM1A0);
  TCCR1B = _BV(WGM12) | TIMER1_PRESCALE;
}

int main()
{
  TDataPacket commandBuf;
  IND_0_OFF;
  IND_1_OFF;
  IND_2_OFF;
  IND_3_OFF;
  IND_INIT;

  twiInit(TWI_ADDR, TWI_BAUD, 1);
  spiInit();
  initHW();
#if UART_ENABLED==1
  initCommUsart();
#endif

  sei(); //set global interrupt enable

  for (;;) {
    if (!twiIsBusy()) {
      switch (twiGetAck()) {
        case ACK_ACK:
          doSendAck();
          break;
        case ACK_NACK:
          sendNack(&commandBuf);
          break;
        default:
          // do nothing
          break;
      }
      twiStartSlave();
    }
#if UART_ENABLED==1
    if (getBytesAvailableUsart() == sizeof (commandBuf)) {
      readBytes((uint8_t*) & commandBuf, sizeof (commandBuf));
      processCommand(&commandBuf, UART);
    }
#endif
    //    if (spiGetDataAvailable() == sizeof (commandBuf)) {
    //      spiGetData(&commandBuf);
    //      processCommand(&commandBuf, SPI);
    //    }
    if (twiPollData(&commandBuf)) {
      twiClearAck();
      processCommand(&commandBuf, TWI);
    }
    IND_0_OFF;
    IND_1_OFF;
    IND_2_OFF;
    IND_3_OFF;
  }
}

