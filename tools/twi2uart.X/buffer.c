
#include <avr/io.h>
#include "buffer.h"

buffer_t buffer;
uint8_t hasCommand;
uint8_t hasEvent;
uint8_t programMode;

uint8_t isInProgramMode()
{
  return programMode;
}

command_buffer_t* getCommandDataBuffer()
{
  if (hasCommand) {
    return &(buffer.inBuffer);
  }
  return 0;
}

uint8_t isCommandAvailable()
{
  return hasCommand;
}

command_buffer_t* getCommandEventBuffer()
{
  if (hasEvent) {
    return &(buffer.outBuffer);
  }
  return 0;
}

uint8_t isEventAvailable()
{
  return hasEvent;
}

program_buffer_t* getProgramBuffer()
{
  return &(buffer.programBuffer);
}
