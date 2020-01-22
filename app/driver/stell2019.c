#include <linux/init.h>
#include <linux/module.h>
#include <linux/fs.h>

#define STELL_ALERT KERN_ALERT"::stell2019::"

MODULE_LICENSE("Apache-2.0");
MODULE_AUTHOR("Wolfgang Reder");
MODULE_VERSION("0.1");

static dev_t deviceNumber;

static int stell_init(void)
{
	int result;

	deviceNumber = MKDEV(0, 0);
	result = alloc_chrdev_region(&deviceNumber, 0, 1, "/dev/stell");
	printk(STELL_ALERT "Hello, world, im am device %u:%u\n", MAJOR(deviceNumber), MINOR(deviceNumber));
	return result;
}

static void stell_exit(void)
{
	if (MAJOR(deviceNumber)>0) {
	  unregister_chrdev_region(deviceNumber, 1);
	}
	printk(STELL_ALERT "Goodbye, cruel world\n");
}
module_init(stell_init);
module_exit(stell_exit);

