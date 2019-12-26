
#include <avr/io.h>
#include <util/atomic.h>
#include "buffer.h"

buffer_t buffer;
uint8_t hasCommand;
uint8_t hasEvent;
uint8_t programMode;

buffer_t* getBuffer()
{
  return &buffer;
}

void enterProgramMode()
{
  programMode = 1;
}

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

void publishCommand()
{
  hasCommand = 1;
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

void publishEvent()
{
  hasEvent = 1;
}

program_buffer_t* getProgramBuffer()
{
  return &(buffer.programBuffer);
}
