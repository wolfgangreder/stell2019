/*
 * File:   protocol.h
 * Author: wolfi
 *
 * Created on 30. November 2019, 13:50
 */

#ifndef PROTOCOL_H
#define	PROTOCOL_H

#ifdef	__cplusplus
extern "C" {
#endif

  typedef struct {
    uint8_t address;
    uint8_t size;
    uint8_t crc8;

  } command_header_t;

  typedef struct {
    command_header_t header;

    struct {
      uint8_t command;

      union {
        uint16_t data[SPM_PAGESIZE / 2];

        struct {
          uint32_t pageAddress;
        };
      };
    } payload;
  } command_t;

  extern void initUART();

  extern uint8_t pollByte();

  extern void sendBuffer(uint8_t* buffer, uint8_t size);

  extern void sendCommandBuffer(command_t* cmd);

#ifdef	__cplusplus
}
#endif

#endif	/* PROTOCOL_H */

