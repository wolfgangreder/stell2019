#!/bin/sh

module="stell2019"
device="stell"

rm -f /dev/${device}
/sbin/rmmod $module

