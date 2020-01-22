MODULE := stell2019
MODE := 664
GROUP := users
# If KERNELRELEASE is defined, we've been invoked from the
# kernel build system and can use its language.
ifneq ($(KERNELRELEASE),)

obj-m := $(MODULE).o
#stell2019-y := stell2019.c
# Otherwise we were called directly from the command
# line; invoke the kernel build system.
else
KERNELDIR ?= /lib/modules/$(shell uname -r)/build
PWD := $(shell pwd)
default: modules
	
modules:
	$(MAKE) -C $(KERNELDIR) M=$(PWD) modules

clean:
	$(MAKE) -C $(KERNELDIR) M=$(PWD) clean

install: 
	$(PWD)/install.sh $(MODULE) $(MODULE) $(MODE)

uninstall: 
	$(PWD)/uninstall.sh $(MODULE) $(MODULE)


endif
