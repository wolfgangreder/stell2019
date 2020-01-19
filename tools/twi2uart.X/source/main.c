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
#include <avr/wdt.h>
#include <string.h>
#include "uart.h"
#include "twi.h"
#include "config.h"
#include "spi.h"

FUSES = {
  (FUSE_SUT1 | FUSE_SUT0 | FUSE_CKSEL3 | FUSE_CKSEL1 | FUSE_CKSEL0 | FUSE_BODEN),
  (FUSE_SPIEN | FUSE_EESAVE | FUSE_BOOTSZ0 | FUSE_BOOTSZ1)
};
TRegisterFile registerFile;
EEMEM TEEPromFile ee_eepromFile = {
  .vcc_calibration = 0,
  .twibaud = TWI_BAUD,
  .blinkCounter = TIMER1_TOP,
  .blinkPrescale = TIMER1_PRESCALE
};
TEEPromFile eepromFile;
const PROGMEM TFlashFile fl_flashFile = {.fw_major = FW_MAJOR, .fw_minor = FW_MINOR};
TFlashFile flashFile;

inline void doSendAck()
{
#if UART_ENABLED==1
  sendACK();
#endif
}

inline void doSendNack(TDataPacket* packet)
{
  memset(packet, 0xff, sizeof (TDataPacket));
  writeBytesUsart((uint8_t*) packet, sizeof (TDataPacket));
}

uint16_t processBlinkCounter(register_t reg, uint8_t data, operation_t op)
{
  switch (op) {
    case OP_READ:
      return eepromFile.blinkCounter;
    case OP_WRITE:
      switch (reg) {
        case REG_BLINK_COUNTER_H:
          eepromFile.blinkCounterH = data;
          break;
        case REG_BLINK_COUNTER_L:
          eepromFile.blinkCounterL = data;
        default:
          return -1;
      }
      break;
    case OP_INCREMENT:
      eepromFile.blinkCounter++;
      break;
    case OP_DECREMENT:
      eepromFile.blinkCounter--;
      break;
    default:
      return -1;
  }
  eeprom_write_word(&ee_eepromFile.blinkCounter, eepromFile.blinkCounter);
  OCR1A = eepromFile.blinkCounter;
  return eepromFile.blinkCounter;
}

uint16_t processBlinkPrescale(uint8_t data, operation_t op)
{
  switch (op) {
    case OP_READ:
      return eepromFile.blinkPrescale;
    case OP_WRITE:
      eepromFile.blinkPrescale = data;
      break;
    case OP_INCREMENT:
      eepromFile.blinkPrescale++;
      break;
    case OP_DECREMENT:
      eepromFile.blinkPrescale--;
      break;
    default:
      return -1;
  }
  eepromFile.blinkPrescale = eepromFile.blinkPrescale & 0x7;
  eeprom_write_byte(&ee_eepromFile.blinkPrescale, eepromFile.blinkPrescale);
  TCCR1B &= 0xf8;
  TCCR1B |= eepromFile.blinkPrescale;
  return eepromFile.blinkPrescale;
}

uint16_t processTWIBaud(uint8_t data, operation_t op)
{
  switch (op) {
    case OP_READ:
      return eepromFile.twibaud;
    case OP_WRITE:
      eepromFile.twibaud = data;
      break;
    case OP_INCREMENT:
      eepromFile.twibaud++;
      break;
    case OP_DECREMENT:
      eepromFile.twibaud--;
      break;
    default:
      return -1;
  }
  eeprom_write_byte(&ee_eepromFile.twibaud, eepromFile.twibaud);
  twiSetBaud(eepromFile.twibaud);
  return eepromFile.twibaud;
}

uint16_t processVCC()
{
  int16_t result;
  uint8_t l = ADCL;
  uint8_t h = ADCH;

  result = ((h << 8) + l) + eepromFile.vcc_calibration;

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    registerFile.vcc = result;
  }
  return result;
}

uint16_t processVCCCalib(uint8_t cal, operation_t operation)
{
  switch (operation) {
    case OP_READ:
      return eepromFile.vcc_calibration;
    case OP_WRITE:
      eepromFile.vcc_calibration = (int8_t) cal;
      break;
    case OP_INCREMENT:
      eepromFile.vcc_calibration++;
      break;
    case OP_DECREMENT:
      eepromFile.vcc_calibration--;
      break;
    default:
      return -1;
  }
  eeprom_write_byte((uint8_t*) & ee_eepromFile.vcc_calibration, eepromFile.vcc_calibration);
  return eepromFile.vcc_calibration;
}

void internalProcessCommand(TDataPacket* packet, packetsource_t source)
{
  if (source != UART || source != SPI) {
    return;
  }
  uint16_t result = -1;
  switch (packet->rxRegisterAddress) {
    case REG_STATE:
      result = registerFile.state;
      break;
    case REG_FW_VERSION:
      result = flashFile.fw_Version;
      break;
    case REG_BLINK_COUNTER_H:
    case REG_BLINK_COUNTER_L:
      result = processBlinkCounter(packet->rxRegisterAddress, packet->rxData, packet->rxRegisterOperation);
      break;
    case REG_BLINK_PRESCALE:
      result = processBlinkPrescale(packet->rxData, packet->rxRegisterOperation);
      break;
    case REG_TWI_BAUD:
      result = processTWIBaud(packet->rxData, packet->rxRegisterOperation);
      break;
    case REG_VCC:
      result = processVCC();
      break;
    case REG_VCC_CALIB:
      result = processVCCCalib(packet->rxData, packet->rxRegisterOperation);
      break;
    case REG_VCC_REF:
      result = 2560; // 2.56V
    default:
      return;
  }
  if (packet->rxRegisterOperation == OP_READ) {
    packet->txSlaveAddress = TWI_ADDR;
    packet->txOwnAddress = TWI_ADDR;
    packet->txWData = result;
    if (source == UART) {
#if UART_ENABLED==1
      writeBytesUsart((uint8_t*) packet, sizeof (TDataPacket));
#endif
    } else {
      spiWriteData(packet);
    }
  } else if (source == UART) {
    doSendAck();
  }
}

void processCommand(TDataPacket* packet, packetsource_t source)
{
  bool isRead;
  switch (source) {
    case UART:
    case SPI:
      if (packet->rxSlaveAddress == TWI_ADDR) {
        internalProcessCommand(packet, source);
      } else {
        isRead = packet->rxRegisterOperation == OP_READ;
        packet->txSlaveAddress = packet->txSlaveAddress << TWI_ADR_BITS;
        if (isRead) {
          twiSendReceiveData(packet);
        } else {
          twiSendData(packet);
        }
      }
      break;
    case TWI:
#if UART_ENABLED==1
      writeBytesUsart((uint8_t*) packet, sizeof (TDataPacket));
#endif
      spiWriteData(packet);
      break;
  }
}

void initHW()
{
  eeprom_read_block(&eepromFile, &ee_eepromFile, sizeof (TEEPromFile));
  memcpy_P(&flashFile, &fl_flashFile, sizeof (TFlashFile));
  BLINK_DIR |= _BV(BLINK); // output !
  OCR1A = eepromFile.blinkCounter;
  TCCR1A = _BV(COM1A0);
  TCCR1B = _BV(WGM12) | eepromFile.blinkPrescale;
  ADMUX = _BV(REFS0) | _BV(MUX1) | _BV(MUX2) | _BV(MUX3);
  ADCSRA = _BV(ADEN) | _BV(ADFR) | _BV(ADPS0) | _BV(ADPS1) | _BV(ADPS2) | _BV(ADSC);
}

int main()
{
  uint8_t resetSource = MCUCSR;

  TDataPacket commandBuf;
  IND_INIT;
  wdt_enable(0); // 17ms
  initHW();
  twiInit(TWI_ADDR, eepromFile.twibaud, 1);
  spiInit();
#if UART_ENABLED==1
  initCommUsart();
#endif

  registerFile.bo_error = resetSource & _BV(BORF);
  registerFile.wdt_error = resetSource & _BV(WDRF);

  sei(); //set global interrupt enable
  if (registerFile.state != 0) {
    TDataPacket msg;
    msg.txSlaveAddress = TWI_ADDR;
    msg.txOwnAddress = TWI_ADDR;
    msg.a = registerFile.state;
    msg.b = 0;
#if UART_ENABLED==1
    writeBytesUsart((uint8_t*) & msg, sizeof (msg));
#endif
    spiWriteData(&msg);
  }

  for (;;) {
    wdt_reset();
    if (!twiIsBusy()) {
      switch (twiGetAck()) {
        case ACK_ACK:
          doSendAck();
          break;
        case ACK_NACK:
          doSendNack(&commandBuf);
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
    if (spiPollData(&commandBuf)) {
      processCommand(&commandBuf, SPI);
    }
    if (twiPollData(&commandBuf)) {
      twiClearAck();
      processCommand(&commandBuf, TWI);
    }
  }
}

