#!/bin/sh

module=$1
device=$2
mode=$3
group="users"
# invoke insmod with all arguments we got
# and use a pathname, as newer modutils don't look in . by default
/sbin/insmod ./$module.ko || exit 1
# remove stale nodes
rm -f /dev/${device}
major=$(awk '$2=="'${module}'" {print $1}' /proc/devices)
mknod /dev/${device} c $major 0

# give appropriate group/permissions, and change the group.
# Not all distributions have staff, some have "wheel" instead.
chgrp $group /dev/${device}
chmod $mode /dev/${device}

ls -la /dev/${device}
