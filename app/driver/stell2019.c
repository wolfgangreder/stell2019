#include <linux/init.h>
#include <linux/module.h>
#include <linux/spi/spi.h>
#include <linux/cdev.h>

#include "stell2019.h"

MODULE_LICENSE("Dual MIT/GPL");
MODULE_AUTHOR("Wolfgang Reder");
MODULE_VERSION("0.1");

struct stell_dev {
	struct task_struct* ownerProcess;
	unsigned char* buffer;
	spinlock_t spin;
	struct cdev cdev;
};

static int stell_probe(struct spi_device *spi)
{
	printk(STELL_ALERT"stell_probe called\n");
	return -1;
}

static int stell_remove(struct spi_device *spi)
{
	printk(STELL_ALERT"stell_remove_called\n");
	return -1;
}

static int setupSpi(struct stell_dev* dev)
{
	return -1;
}

static int stell_open(struct inode * inode, struct file * file)
{
	struct stell_dev* dev;
	int result;

	dev = container_of(inode->i_cdev, struct stell_dev, cdev);
	spin_lock(&dev->spin);
	if (dev->ownerProcess) {
		spin_unlock(&dev->spin);
		return -EBUSY;
	}
	dev->ownerProcess = current;
	spin_unlock(&dev->spin);
	result = setupSpi(dev);
	if (result) {
		return result;
	}
	printk(STELL_ALERT"stell_open called\n");
	return 0;
}

static int stell_release(struct inode * inode, struct file * file)
{
	struct stell_dev* dev;
	dev = container_of(inode->i_cdev, struct stell_dev, cdev);
	spin_lock(&dev->spin);
	dev->ownerProcess = 0;
	spin_unlock(&dev->spin);
	printk(STELL_ALERT"stell_release called\n");
	return 0;
}

static long stell_unlocked_ioctl(struct file * file, unsigned int iparam, unsigned long lparam)
{
	printk(STELL_ALERT"stell_unlocked_ioctl called\n");
	return -1;
}

static long stell_compat_ioctl(struct file * file, unsigned int iparam, unsigned long lparam)
{
	printk(STELL_ALERT"stell_compat_ioctl called\n");
	return -1;
}

static ssize_t stell_read(struct file * file, char __user * __buffer, size_t numBytes, loff_t * offset)
{
	printk(STELL_ALERT"stell_read called\n");
	if (!__buffer) {
		return -EFAULT;
	}
	return numBytes;
}

static ssize_t stell_write(struct file * file, const char __user * __buffer, size_t numBytes, loff_t * offset)
{
	printk(STELL_ALERT"stell_write called\n");
	return -1;
}


static const struct spi_device_id stell2019_id_table[] = {
	{
		.name = "stell2019",
		.driver_data = (kernel_ulong_t) 0,
	},
	{}
};
MODULE_DEVICE_TABLE(spi, stell2019_id_table);

static struct spi_driver stell_driver = {
	.driver =
	{
		.name = DEVICE_NAME,
	},
	.id_table = stell2019_id_table,
	.probe = stell_probe,
	.remove = stell_remove,
};
static dev_t dev;


struct file_operations stell_fops = {
	.open = stell_open,
	.read = stell_read,
	.write = stell_write,
	.release = stell_release,
	.unlocked_ioctl = stell_unlocked_ioctl,
	.compat_ioctl = stell_compat_ioctl,
};

struct stell_dev stell_dev;

static int stell_init(void)
{
	int result;
	printk(STELL_ALERT"stell_init called\n");
	dev = MKDEV(0, 0);
	result = alloc_chrdev_region(&dev, 0, 1, DEVICE_NAME);
	if (result) {
		printk(STELL_ALERT "cannot register character device");
		return result;
	}
	result = spi_register_driver(&stell_driver);
	if (result) {
		printk(STELL_ALERT"spi_register_driver returned %i\n", result);
		return result;
	}
	cdev_init(&stell_dev.cdev, &stell_fops);
	stell_dev.cdev.owner = THIS_MODULE;
	stell_dev.cdev.ops = &stell_fops;
	result = cdev_add(&stell_dev.cdev, dev, 1);
	if (result) {
		printk(STELL_ALERT"Error %d adding character device", result);
	}
	spin_lock_init(&stell_dev.spin);
	stell_dev.buffer = kmalloc(PACKET_SIZE, GFP_KERNEL);
	if (!stell_dev.buffer) {
		return -ENOMEM;
	}
	return result;
}

static void stell_exit(void)
{
	kfree(stell_dev.buffer);
	cdev_del(&stell_dev.cdev);
	spi_unregister_driver(&stell_driver);
	if (dev != 0) {
		unregister_chrdev_region(dev, 1);
	}
	printk(STELL_ALERT"stell_exit called\n");
}


module_init(stell_init);
module_exit(stell_exit);

//module_driver(stell_driver, stell_register_driver, spi_unregister_drvier);

