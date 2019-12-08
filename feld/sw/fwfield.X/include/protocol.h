/*
 * File:   protocol.h
 * Author: wolfi
 *
 * Created on 28. November 2019, 21:34
 */

#ifndef PROTOCOL_H
#define	PROTOCOL_H

#ifdef	__cplusplus
extern "C" {
#endif


#define CMD_QUERY_STATE     0x01
#define CMD_HELLO           0x02
#define CMD_LAMPTEST        0x03
#define CMD_UPDATE          0x04
#define CMD_BYE             0x05
#define CMD_PAGE_LOAD       0x06
#define CMD_PAGE_PROGRAM    0x07
#define CMD_CHECK_CRC       0x08
#define CMD_LAMP            0x09
#define CMD_KEY_QUERY       0x0a
#define CMD_KEY_EVENT       0x0b
#define CMD_HEALTHCHECK     0x0c

  typedef struct {
    uint8_t address;
    uint8_t size;
    uint8_t crc8;
    uint8_t data[64];
  } command_t;

  extern void initProtocol();

  extern command_t* getCurrentCommand();

#ifdef	__cplusplus
}
#endif

#endif	/* PROTOCOL_H */

