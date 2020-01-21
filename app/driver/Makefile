DEVICE_NAME=stell

# If KERNELRELEASE is defined, we've been invoked from the
# kernel build system and can use its language.
ifneq ($(KERNELRELEASE),)

obj-m := stell2019.o
#stell2019-y := stell2019.c
# Otherwise we were called directly from the command
# line; invoke the kernel build system.
else
KERNELDIR ?= /lib/modules/$(shell uname -r)/build
PWD := $(shell pwd)
default:
	$(MAKE) -C $(KERNELDIR) M=$(PWD) modules

clean:
	@rm -f *.o
	@rm -f *.mod*
	@rm -f *.ko
	@rm -f modules.order
	@rm -f Module.symvers
	@rm -R -f .tmp*
	@rm -f .stell*

install:
	install.sh

uninstall:
	uninstall.sh

endif
