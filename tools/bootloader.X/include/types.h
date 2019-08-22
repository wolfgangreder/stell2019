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

#ifndef TYPES_H
#define	TYPES_H
#include <avr/io.h>

#ifdef	__cplusplus
extern "C" {
#endif


    typedef unsigned char bool;
    #define true  (1)
    #define false (0)

    typedef union {
        uint8_t data[67];
        struct {
          uint8_t adress;
          uint8_t dataSize;
          uint8_t crc8;
          uint8_t payload[64];
        };
    } update_rec;
    
    
#ifdef	__cplusplus
}
#endif

#endif	/* TYPES_H */

