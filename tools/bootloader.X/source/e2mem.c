
#include <avr/io.h>
#include "types.h"
#include "e2mem.h"

uint8_t e2mem_read(uint8_t* ptr)
{
	return *(ptr + MAPPED_EEPROM_START);
}