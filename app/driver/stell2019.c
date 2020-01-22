#include <linux/init.h>
#include <linux/module.h>
#include <linux/fs.h>

#define STELL_ALERT KERN_ALERT"::stell2019::"

MODULE_LICENSE("Apache-2.0");
MODULE_AUTHOR("Wolfgang Reder");
MODULE_VERSION("0.1");

static dev_t deviceNumber;

static uint stell_poll(struct file* file, struct poll_table_struct* table)
{
	return ERR;
}

static int stell_ioctl(struct inode * node, struct file * file, unsigned int iParam, unsigned long lParam)
{
	return ERR;
}

static int stell_open(struct inode* node, struct vm_area_struct* area)
{
	return ERR;
}

static int stell_release(struct inode * node, struct file * file)
{
	return ERR;
}

static int stell_init(void)
{
	int result;

	deviceNumber = MKDEV(0, 0);
	result = alloc_chrdev_region(&deviceNumber, 0, 1, "stell2019");
	if (!result) {
		printk(STELL_ALERT "Hello, world, im am device %u:%u\n", MAJOR(deviceNumber), MINOR(deviceNumber));
		struct file_operations my_fops = {
			.owner = THIS_MODULE,
			.ioctl = stell_ioctl,
			.open = stell_open,
			.release = stell_release,
			.poll = stell_poll,
		};
		struct cdev *my_cdev = cdev_alloc();
		my_cdev->ops = &my_fops;

	} else {
		printk(STELL_ALERT"Cannot allocate a device number");
	}
	return result;
}

static void stell_exit(void)
{
	if (MAJOR(deviceNumber) > 0) {
		unregister_chrdev_region(deviceNumber, 1);
	}
	printk(STELL_ALERT "Goodbye, cruel world\n");
}


module_init(stell_init);
module_exit(stell_exit);

