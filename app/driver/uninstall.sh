#!/bin/sh

module=$1
device=$2

rm -f /dev/${device}
/sbin/rmmod $module

