#
#  There exist several targets which are by default empty and which can be
#  used for execution of your targets. These targets are usually executed
#  before and after some main targets. They are:
#
#     .build-pre:              called before 'build' target
#     .build-post:             called after 'build' target
#     .clean-pre:              called before 'clean' target
#     .clean-post:             called after 'clean' target
#     .clobber-pre:            called before 'clobber' target
#     .clobber-post:           called after 'clobber' target
#     .all-pre:                called before 'all' target
#     .all-post:               called after 'all' target
#     .help-pre:               called before 'help' target
#     .help-post:              called after 'help' target
#
#  Targets beginning with '.' are not intended to be called on their own.
#
#  Main targets can be executed directly, and they are:
#
#     build                    build a specific configuration
#     clean                    remove built files from a configuration
#     clobber                  remove all built files
#     all                      build all configurations
#     help                     print help mesage
#
#  Targets .build-impl, .clean-impl, .clobber-impl, .all-impl, and
#  .help-impl are implemented in nbproject/makefile-impl.mk.
#
#  Available make variables:
#
#     CND_BASEDIR                base directory for relative paths
#     CND_DISTDIR                default top distribution directory (build artifacts)
#     CND_BUILDDIR               default top build directory (object files, ...)
#     CONF                       name of current configuration
#     CND_ARTIFACT_DIR_${CONF}   directory of build artifact (current configuration)
#     CND_ARTIFACT_NAME_${CONF}  name of build artifact (current configuration)
#     CND_ARTIFACT_PATH_${CONF}  path to build artifact (current configuration)
#     CND_PACKAGE_DIR_${CONF}    directory of package (current configuration)
#     CND_PACKAGE_NAME_${CONF}   name of package (current configuration)
#     CND_PACKAGE_PATH_${CONF}   path to package (current configuration)
#
# NOCDDL


# Environment
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib
OBJDUMP=/home/wolfi/bin/avr8-gnu-toolchain-linux_x86_64/bin/avr-objdump
#AVRSIZE=/home/wolfi/bin/avr8-gnu-toolchain-linux_x86_64/bin/avr-size
#BUILD_CONFIG=-DFW_MINOR=6 -DFW_MAJOR=0 -DTWI_ADDRESS=3 -DCOMM_USART -gdwarf
BUILD_CONFIG=-DFW_MINOR=15 -DFW_MAJOR=0 -DTWI_ADDRESS=3 -gdwarf
# build
build: .build-post

.build-pre:
# Add your pre 'build' code here...

.build-post: .build-impl
#	${OBJDUMP} -h -S -j .fuse -j .text -j .bootloader ${CND_ARTIFACT_DIR_${CONF}}/${basename ${CND_ARTIFACT_NAME_${CONF}}}.elf > ${CND_ARTIFACT_DIR_${CONF}}/${basename ${CND_ARTIFACT_NAME_${CONF}}}.lss
#	${AVRSIZE} --mcu=atmega8535 --format avr ${CND_ARTIFACT_DIR_${CONF}}/${basename ${CND_ARTIFACT_NAME_${CONF}}}.elf  > ${CND_ARTIFACT_DIR_${CONF}}/${basename ${CND_ARTIFACT_NAME_${CONF}}}.usg
#	${AVRSIZE} --mcu=atmega8535 --format avr ${CND_ARTIFACT_DIR_${CONF}}/${basename ${CND_ARTIFACT_NAME_${CONF}}}.elf


# clean
clean: .clean-post

.clean-pre:
# Add your pre 'clean' code here...
# WARNING: the IDE does not call this target since it takes a long time to
# simply run make. Instead, the IDE removes the configuration directories
# under build and dist directly without calling make.
# This target is left here so people can do a clean when running a clean
# outside the IDE.

.clean-post: .clean-impl
# Add your post 'clean' code here...


# clobber
clobber: .clobber-post

.clobber-pre:
# Add your pre 'clobber' code here...

.clobber-post: .clobber-impl
# Add your post 'clobber' code here...


# all
all: .all-post

.all-pre:
# Add your pre 'all' code here...

.all-post: .all-impl
# Add your post 'all' code here...


# help
help: .help-post

.help-pre:
# Add your pre 'help' code here...

.help-post: .help-impl
# Add your post 'help' code here...



# include project implementation makefile
include nbproject/Makefile-impl.mk

# include project make variables
include nbproject/Makefile-variables.mk
