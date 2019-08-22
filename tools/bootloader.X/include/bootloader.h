/*
 * Copyright 2019 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef BOOTLOADER_H
#define	BOOTLOADER_H

#include <avr/io.h>
#include <avr/eeprom.h>
#include "types.h"
/*
 * Provides Application access to bootloader functions.
 * Code uses EEPROM Address 0 as flag
 */
#ifdef	__cplusplus
extern "C" {
#endif

#define enterBootloader \
    eeprom_write_byte(0,true);\
    _PROTECTED_WRITE(RSTCTRL.SWRR,RSTCTRL_SWRE_bm)

#ifdef	__cplusplus
}
#endif

#endif	/* BOOTLOADER_H */

