/*
 * File:   e2mem.h
 * Author: reder
 *
 * Created on 23. August 2019, 10:09
 */

#ifndef E2MEM_H
#define	E2MEM_H

#include <avr/io.h>
#include "types.h"

#ifdef	__cplusplus
extern "C" {
#endif

    inline uint8_t e2mem_read_uint8(uint8_t* ptr) {
        return *(ptr + MAPPED_EEPROM_START);
    }


#ifdef	__cplusplus
}
#endif

#endif	/* E2MEM_H */

