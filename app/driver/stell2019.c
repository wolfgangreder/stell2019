#include <linux/init.h>
#include <linux/module.h>
#include <linux/spi/spi.h>

#define DEVICE_NAME "stell2019"
#define STELL_ALERT KERN_ALERT"::stell2019::"

MODULE_LICENSE("Apache-2.0");
MODULE_AUTHOR("Wolfgang Reder");
MODULE_VERSION("0.1");




static struct spi_driver stell_driver = {
  .driver =
  {
    .name = DEVICE_NAME,
    .of_match_table = mcp251x_of_match,
    .pm = &mcp251x_can_pm_ops,
  },
  .id_table = mcp251x_id_table,
  .probe = mcp251x_can_probe,
  .remove = mcp251x_can_remove,
};
module_spi_driver(stell_driver);
