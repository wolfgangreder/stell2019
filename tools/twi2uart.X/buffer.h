/*
 * File:   buffer.h
 * Author: wolfi
 *
 * Created on 8. Dezember 2019, 21:50
 */

#ifndef BUFFER_H
#define	BUFFER_H

#ifdef	__cplusplus
extern "C" {
#endif
#define PAGE_SIZE 64
#define MAX_COMMAND_DATA 8
#define STX 2
#define EOT 4
#define DLE 10 // STX und EOT müssen in den daten mit DLE markiert werden
#define ACK 6

  typedef struct {
    uint16_t receiver;
    uint8_t crc8;
    uint8_t command;
  } buffer_head_t;

  typedef struct {
    buffer_head_t head;
    uint32_t pageAddress;
    uint8_t pageData[PAGE_SIZE];
  } program_buffer_t;

  typedef struct {
    buffer_head_t head;
    uint8_t pageData[MAX_COMMAND_DATA];
  } command_buffer_t;

  typedef struct {

    union {
      program_buffer_t programBuffer;

      struct {
        command_buffer_t inBuffer;
        command_buffer_t outBuffer;
      };
    };
  } buffer_t;

  extern uint8_t isInProgramMode();
  extern command_buffer_t* getCommandDataBuffer();
  extern uint8_t isCommandAvailable();
  extern command_buffer_t* getCommandEventBuffer();
  extern uint8_t isEventAvailable();
  extern program_buffer_t* getProgramBuffer();
#ifdef	__cplusplus
}
#endif

#endif	/* BUFFER_H */

