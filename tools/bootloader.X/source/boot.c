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
#define F_CPU_RESET                 (20E6/6)

#include <avr/io.h>
#include <string.h>
#include "types.h"
#include "bootloader.h"
#include "e2mem.h"
/* Memory configuration
 * BOOTEND_FUSE * 256 must be above Bootloader Program Memory Usage,
 * this is 490 bytes at optimization level -O3, so BOOTEND_FUSE = 0x02
 */
#define BOOTEND_FUSE                (0x01)
#define BOOT_SIZE                   (BOOTEND_FUSE * 0x100)
#define MAPPED_APPLICATION_START    (MAPPED_PROGMEM_START + BOOT_SIZE)
#define MAPPED_APPLICATION_SIZE     (MAPPED_PROGMEM_SIZE - BOOT_SIZE)

/* Fuse configuration
 * BOOTEND sets the size (end) of the boot section in blocks of 256 bytes.
 * APPEND = 0x00 defines the section from BOOTEND*256 to end of Flash as application code.
 * Remaining fuses have default configuration.
 */
FUSES = {
	.OSCCFG = FREQSEL_20MHZ_gc,
	.SYSCFG0 = CRCSRC_NOCRC_gc | RSTPINCFG_UPDI_gc,
	.SYSCFG1 = SUT_64MS_gc,
	.APPEND = 0x00,
	.BOOTEND = BOOTEND_FUSE
};


bool bootloaderRequired EEMEM = true;

/* Define application pointer type */
typedef void (*const app_t) (void);

/* Interface function prototypes */
static bool is_bootloader_requested(void);
static void init_twi(void);
static void updateflash();
static bool page_load(update_rec*);
static void page_write(update_rec*);

/*
 * Main boot function
 * Put in the constructors section (.ctors) to save Flash.
 * Naked attribute used since function prologue and epilogue is unused
 */
__attribute__((naked)) __attribute__((section(".ctors"))) void
boot(void)
{
	/* Initialize system for AVR GCC support, expects r1 = 0 */
	asm volatile("clr r1");
	// Init Stack !
	SP = RAMEND;

	//	if (!is_bootloader_requested()) {
	//		app_t app = (app_t) (BOOT_SIZE / sizeof(app_t));
	//		app();
	//	} else {
	init_twi();
	updateflash();
	//	}
	//	/* Issue system reset */
	_PROTECTED_WRITE(RSTCTRL.SWRR, RSTCTRL_SWRE_bm);
}

static update_rec page_buffer;

static void updateflash()
{
	memset(&page_buffer, 0, sizeof(page_buffer));
	while (1) {
		while (page_load(&page_buffer)) {
			page_write(&page_buffer);
		}
	}
}

/*
 * Boot access request function
 */
static bool is_bootloader_requested(void)
{
	return true;
	//if (*(&bootloaderRequired + MAPPED_EEPROM_START) == 0) {
	//	if (e2mem_read_uint8(&bootloaderRequired) == 0) { // no forced update requested, check crc
	//		CRCSCAN.CTRLB = 0; //Priority on Flash; Scan entire Flash
	//
	//		CRCSCAN.CTRLA = 1 << CRCSCAN_ENABLE_bp /* Enable CRC scan: enabled */
	//			| 0 << CRCSCAN_NMIEN_bp;
	//		return !(CRCSCAN_STATUS & CRCSCAN_OK_bm);
	//	}
	//	return true;
}

/*
 * Communication interface functions
 */
static void
init_twi(void)
{
	/* Switch pins to alternate TWI pin location */
	//	PORTMUX.CTRLB |= PORTMUX_TWI0_ALTERNATE_gc;

	/* Enable TWI driver */
	//	i2c_slave_init();
}

static bool
page_load(update_rec* page_buffer)
{
	if (page_buffer->adress == 0xff) {
		return false;
	}
	page_buffer->adress = 0x1f;
	page_buffer->crc8 = 0x2f;
	page_buffer->dataSize = 64;
	return true;
}

static void
page_write(update_rec* page_buffer)
{
	page_buffer->adress = 0xff;
	//	uint8_t *app_ptr = (uint8_t *)MAPPED_APPLICATION_START;
	//	while(app_ptr - MAPPED_PROGMEM_START <= (uint8_t *)PROGMEM_END) {
	//		/* Receive and echo data before loading to memory */
	//		uint8_t rx_data = twi_receive();
	//		twi_send(rx_data);
	//
	//		/* Incremental load to page buffer before writing to Flash */
	//		*app_ptr = rx_data;
	//		app_ptr++;
	//		if(!((uint16_t)app_ptr % MAPPED_PROGMEM_PAGE_SIZE)) {
	//			/* Page boundary reached, Commit page to Flash */
	//			_PROTECTED_WRITE_SPM(NVMCTRL.CTRLA, NVMCTRL_CMD_PAGEERASEWRITE_gc);
	//			while(NVMCTRL.STATUS & NVMCTRL_FBUSY_bm);
	//
	//			toggle_status_led();
	//		}
	//	}
	//
}