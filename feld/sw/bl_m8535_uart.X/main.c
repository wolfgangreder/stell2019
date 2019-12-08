#include <avr/io.h>
#include <avr/eeprom.h>
#include <avr/wdt.h>
#include <avr/fuse.h>
#include <avr/boot.h>
#include <string.h>
#include "crc.h"
#include "bootloader.h"
#include "protocol.h"

//FUSES = {
//  (FUSE_BODEN | FUSE_SUT1),
//  (FUSE_BOOTSZ1 & FUSE_SPIEN & FUSE_BOOTRST)
//};

uint8_t crc8Command(command_t* command)
{
  return crc8((uint8_t*)&(command->payload), 0, command->header.size);
}

void reboot()
{
  wdt_enable(WDTO_15MS);
  while (1);
}

void sendResponse(command_t* command, uint8_t rsp)
{
  command->header.address = 0;
  command->payload.command = rsp;
  command->header.size = 1;
  command->header.crc8 = crc8Command(command);
  sendCommandBuffer(command);
}

uint8_t readData(command_t* command)
{
  uint16_t* ptr = command->payload.data;
  uint8_t limit = command->header.size;
  if (limit>sizeof (command->payload.data)) {
    sendResponse(command, RSP_ERROR);
    return 0;
  }
  for (uint8_t i = 0; i < limit; ++i) {
    *(ptr++) = pollByte();
  }
  uint8_t crc = crc8Command(command);
  if (crc != command->header.crc8) {
    sendResponse(command, RSP_ERROR);
    return 0;
  } else {
    sendResponse(command, RSP_OK);
    return 1;
  }
}

uint8_t doPageLoad(command_t* command, uint16_t* pageBuffer)
{
  if (readData(command)) {
    memset(pageBuffer, 0xff, SPM_PAGESIZE);
    memcpy(pageBuffer, command->payload.data, command->header.size);
    return 1;
  } else {
    return 0;
  }
}

uint8_t doPageProgram(command_t* command, uint16_t* pageBuffer)
{

  if (readData(command)) {
    boot_page_erase(command->payload.pageAddress);
    boot_spm_busy_wait();
    uint32_t address = command->payload.pageAddress;
    for (uint8_t i = 0; i < SPM_PAGESIZE / 2; ++i) {
      boot_page_fill(address++, pageBuffer++);
    }
    boot_page_write(command->payload.pageAddress);
    boot_spm_busy_wait();
    sendResponse(command, RSP_OK);
    return 0;
  }
  return 1;
}

void doUpdate()
{
  command_t command;
  uint16_t pageBuffer[SPM_PAGESIZE / 2];
  uint8_t* ptr = (uint8_t*) & command;
  uint8_t hasPageData = 0;
  memset(&command, 0, sizeof (command));
  initUART();

  for (uint8_t i = 0; i < sizeof (command_header_t); ++i) {
    *(ptr++) = pollByte();
  }
  switch (command.payload.command) {
    case CMD_PAGE_LOAD:
      hasPageData = doPageLoad(&command, pageBuffer);
      break;
    case CMD_PAGE_PGM:
      if (hasPageData) {
        hasPageData = doPageProgram(&command, pageBuffer);
      } else {
        sendResponse(&command, RSP_ERROR);
      }
      break;
    case CMD_RESTART:
      sendResponse(&command, RSP_OK);
      reboot();
      break;
    default:
      sendResponse(&command, RSP_ERROR);
  }
}

__attribute__((section(".defaultText"))) int appMain()
{
  enterUpdateMode();
  main();
  return 0;
}

int main()
{
  if (checkFlash(0) && !isUpdateRequired()) {
    return appMain();
  } else {
    DDRB = 0xff;
    uint16_t size = crc16(0);
    PORTB = ~(((uint16_t) (size)));
    // 0x2e60
    doUpdate();
  }
  return 0;
}

uint8_t isUpdateRequired()
{
  return eeprom_read_byte((uint8_t*) E2END) != 0;
}

void enterUpdateMode()
{
  eeprom_write_byte((uint8_t*) E2END, 1);

}
