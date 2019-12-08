/*
 * File:   bootloader.h
 * Author: wolfi
 *
 * Created on 30. November 2019, 16:56
 */

#ifndef BOOTLOADER_H
#define	BOOTLOADER_H

#ifdef	__cplusplus
extern "C" {
#endif

#define CMD_PAGE_LOAD  (0x06)

#define CMD_PAGE_PGM  (0x07)

#define CMD_RESTART (0x0d)

#define RSP_ERROR (0xfe)

#define RSP_OK (0xff)

  uint8_t isUpdateRequired();

  void enterUpdateMode();

#ifdef	__cplusplus
}
#endif

#endif	/* BOOTLOADER_H */

