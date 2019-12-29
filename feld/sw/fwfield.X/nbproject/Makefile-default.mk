#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Include project Makefile
ifeq "${IGNORE_LOCAL}" "TRUE"
# do not include local makefile. User is passing all local related variables already
else
include Makefile
# Include makefile containing local settings
ifeq "$(wildcard nbproject/Makefile-local-default.mk)" "nbproject/Makefile-local-default.mk"
include nbproject/Makefile-local-default.mk
endif
endif

# Environment
MKDIR=mkdir -p
RM=rm -f
MV=mv
CP=cp

# Macros
CND_CONF=default
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
IMAGE_TYPE=debug
OUTPUT_SUFFIX=elf
DEBUGGABLE_SUFFIX=elf
FINAL_IMAGE=dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${OUTPUT_SUFFIX}
else
IMAGE_TYPE=production
OUTPUT_SUFFIX=hex
DEBUGGABLE_SUFFIX=elf
FINAL_IMAGE=dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${OUTPUT_SUFFIX}
endif

ifeq ($(COMPARE_BUILD), true)
COMPARISON_BUILD=
else
COMPARISON_BUILD=
endif

ifdef SUB_IMAGE_ADDRESS

else
SUB_IMAGE_ADDRESS_COMMAND=
endif

# Object Directory
OBJECTDIR=build/${CND_CONF}/${IMAGE_TYPE}

# Distribution Directory
DISTDIR=dist/${CND_CONF}/${IMAGE_TYPE}

# Source Files Quoted if spaced
SOURCEFILES_QUOTED_IF_SPACED=source/main.c source/TWI_slave.c source/hw.c

# Object Files Quoted if spaced
OBJECTFILES_QUOTED_IF_SPACED=${OBJECTDIR}/source/main.o ${OBJECTDIR}/source/TWI_slave.o ${OBJECTDIR}/source/hw.o
POSSIBLE_DEPFILES=${OBJECTDIR}/source/main.o.d ${OBJECTDIR}/source/TWI_slave.o.d ${OBJECTDIR}/source/hw.o.d

# Object Files
OBJECTFILES=${OBJECTDIR}/source/main.o ${OBJECTDIR}/source/TWI_slave.o ${OBJECTDIR}/source/hw.o

# Source Files
SOURCEFILES=source/main.c source/TWI_slave.c source/hw.c

# Pack Options
PACK_COMPILER_OPTIONS=-I ${DFP_DIR}/include
PACK_COMMON_OPTIONS=-B ${DFP_DIR}/gcc/dev/atmega8535



CFLAGS=
ASFLAGS=
LDLIBSOPTIONS=

############# Tool locations ##########################################
# If you copy a project from one host to another, the path where the  #
# compiler is installed may be different.                             #
# If you open this project with MPLAB X in the new host, this         #
# makefile will be regenerated and the paths will be corrected.       #
#######################################################################
# fixDeps replaces a bunch of sed/cat/printf statements that slow down the build
FIXDEPS=fixDeps

.build-conf:  ${BUILD_SUBPROJECTS}
ifneq ($(INFORMATION_MESSAGE), )
	@echo $(INFORMATION_MESSAGE)
endif
	${MAKE}  -f nbproject/Makefile-default.mk dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${OUTPUT_SUFFIX}

MP_PROCESSOR_OPTION=ATmega8535
# ------------------------------------------------------------------------------------
# Rules for buildStep: assemble
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
else
endif

# ------------------------------------------------------------------------------------
# Rules for buildStep: assembleWithPreprocess
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
else
endif

# ------------------------------------------------------------------------------------
# Rules for buildStep: compile
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
${OBJECTDIR}/source/main.o: source/main.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/source"
	@${RM} ${OBJECTDIR}/source/main.o.d
	@${RM} ${OBJECTDIR}/source/main.o
	 ${MP_CC}  $(MP_EXTRA_CC_PRE) -mmcu=atmega8535 ${PACK_COMPILER_OPTIONS} ${PACK_COMMON_OPTIONS} -g -DDEBUG -D__MPLAB_DEBUGGER_SIMULATOR=1 -gdwarf-2  -x c -c -D__$(MP_PROCESSOR_OPTION)__  -I"include" -funsigned-char -funsigned-bitfields -O1 -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -MD -MP -MF "${OBJECTDIR}/source/main.o.d" -MT "${OBJECTDIR}/source/main.o.d" -MT ${OBJECTDIR}/source/main.o  -o ${OBJECTDIR}/source/main.o source/main.c  -DXPRJ_default=$(CND_CONF)  $(COMPARISON_BUILD)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 -gdwarf

${OBJECTDIR}/source/TWI_slave.o: source/TWI_slave.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/source"
	@${RM} ${OBJECTDIR}/source/TWI_slave.o.d
	@${RM} ${OBJECTDIR}/source/TWI_slave.o
	 ${MP_CC}  $(MP_EXTRA_CC_PRE) -mmcu=atmega8535 ${PACK_COMPILER_OPTIONS} ${PACK_COMMON_OPTIONS} -g -DDEBUG -D__MPLAB_DEBUGGER_SIMULATOR=1 -gdwarf-2  -x c -c -D__$(MP_PROCESSOR_OPTION)__  -I"include" -funsigned-char -funsigned-bitfields -O1 -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -MD -MP -MF "${OBJECTDIR}/source/TWI_slave.o.d" -MT "${OBJECTDIR}/source/TWI_slave.o.d" -MT ${OBJECTDIR}/source/TWI_slave.o  -o ${OBJECTDIR}/source/TWI_slave.o source/TWI_slave.c  -DXPRJ_default=$(CND_CONF)  $(COMPARISON_BUILD)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 -gdwarf

${OBJECTDIR}/source/hw.o: source/hw.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/source"
	@${RM} ${OBJECTDIR}/source/hw.o.d
	@${RM} ${OBJECTDIR}/source/hw.o
	 ${MP_CC}  $(MP_EXTRA_CC_PRE) -mmcu=atmega8535 ${PACK_COMPILER_OPTIONS} ${PACK_COMMON_OPTIONS} -g -DDEBUG -D__MPLAB_DEBUGGER_SIMULATOR=1 -gdwarf-2  -x c -c -D__$(MP_PROCESSOR_OPTION)__  -I"include" -funsigned-char -funsigned-bitfields -O1 -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -MD -MP -MF "${OBJECTDIR}/source/hw.o.d" -MT "${OBJECTDIR}/source/hw.o.d" -MT ${OBJECTDIR}/source/hw.o  -o ${OBJECTDIR}/source/hw.o source/hw.c  -DXPRJ_default=$(CND_CONF)  $(COMPARISON_BUILD)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 -gdwarf

else
${OBJECTDIR}/source/main.o: source/main.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/source"
	@${RM} ${OBJECTDIR}/source/main.o.d
	@${RM} ${OBJECTDIR}/source/main.o
	 ${MP_CC}  $(MP_EXTRA_CC_PRE) -mmcu=atmega8535 ${PACK_COMPILER_OPTIONS} ${PACK_COMMON_OPTIONS}  -x c -c -D__$(MP_PROCESSOR_OPTION)__  -I"include" -funsigned-char -funsigned-bitfields -O1 -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -MD -MP -MF "${OBJECTDIR}/source/main.o.d" -MT "${OBJECTDIR}/source/main.o.d" -MT ${OBJECTDIR}/source/main.o  -o ${OBJECTDIR}/source/main.o source/main.c  -DXPRJ_default=$(CND_CONF)  $(COMPARISON_BUILD)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 -gdwarf

${OBJECTDIR}/source/TWI_slave.o: source/TWI_slave.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/source"
	@${RM} ${OBJECTDIR}/source/TWI_slave.o.d
	@${RM} ${OBJECTDIR}/source/TWI_slave.o
	 ${MP_CC}  $(MP_EXTRA_CC_PRE) -mmcu=atmega8535 ${PACK_COMPILER_OPTIONS} ${PACK_COMMON_OPTIONS}  -x c -c -D__$(MP_PROCESSOR_OPTION)__  -I"include" -funsigned-char -funsigned-bitfields -O1 -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -MD -MP -MF "${OBJECTDIR}/source/TWI_slave.o.d" -MT "${OBJECTDIR}/source/TWI_slave.o.d" -MT ${OBJECTDIR}/source/TWI_slave.o  -o ${OBJECTDIR}/source/TWI_slave.o source/TWI_slave.c  -DXPRJ_default=$(CND_CONF)  $(COMPARISON_BUILD)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 -gdwarf

${OBJECTDIR}/source/hw.o: source/hw.c  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} "${OBJECTDIR}/source"
	@${RM} ${OBJECTDIR}/source/hw.o.d
	@${RM} ${OBJECTDIR}/source/hw.o
	 ${MP_CC}  $(MP_EXTRA_CC_PRE) -mmcu=atmega8535 ${PACK_COMPILER_OPTIONS} ${PACK_COMMON_OPTIONS}  -x c -c -D__$(MP_PROCESSOR_OPTION)__  -I"include" -funsigned-char -funsigned-bitfields -O1 -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -MD -MP -MF "${OBJECTDIR}/source/hw.o.d" -MT "${OBJECTDIR}/source/hw.o.d" -MT ${OBJECTDIR}/source/hw.o  -o ${OBJECTDIR}/source/hw.o source/hw.c  -DXPRJ_default=$(CND_CONF)  $(COMPARISON_BUILD)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 -gdwarf

endif

# ------------------------------------------------------------------------------------
# Rules for buildStep: compileCPP
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
else
endif

# ------------------------------------------------------------------------------------
# Rules for buildStep: link
ifeq ($(TYPE_IMAGE), DEBUG_RUN)
dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${OUTPUT_SUFFIX}: ${OBJECTFILES}  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} dist/${CND_CONF}/${IMAGE_TYPE}
	${MP_CC} $(MP_EXTRA_LD_PRE) -mmcu=atmega8535 ${PACK_COMMON_OPTIONS}  -D__MPLAB_DEBUGGER_SIMULATOR=1 -gdwarf-2 -D__$(MP_PROCESSOR_OPTION)__  -Wl,-Map="dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.map"    -o dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${OUTPUT_SUFFIX} ${OBJECTFILES_QUOTED_IF_SPACED}      -DXPRJ_default=$(CND_CONF)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 $(COMPARISON_BUILD)  -Wl,--defsym=__MPLAB_BUILD=1$(MP_EXTRA_LD_POST)$(MP_LINKER_FILE_OPTION),--defsym=__MPLAB_DEBUG=1,--defsym=__DEBUG=1,--defsym=__MPLAB_DEBUGGER_SIMULATOR=1 -Wl,--gc-sections -Wl,--start-group  -Wl,-lm -Wl,--end-group

	${MP_CC_DIR}/avr-objcopy -j .eeprom --set-section-flags=.eeprom=alloc,load --change-section-lma .eeprom=0 --no-change-warnings -O ihex "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.eep" || exit 0
	${MP_CC_DIR}/avr-objdump -h -S "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" > "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.lss"

	${MP_CC_DIR}/avr-objcopy -j .user_signatures --set-section-flags=.user_signatures=alloc,load --change-section-lma .user_signatures=0 --no-change-warnings -O ihex "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.usersignatures" || exit 0

else
dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${OUTPUT_SUFFIX}: ${OBJECTFILES}  nbproject/Makefile-${CND_CONF}.mk
	@${MKDIR} dist/${CND_CONF}/${IMAGE_TYPE}
	${MP_CC} $(MP_EXTRA_LD_PRE) -mmcu=atmega8535 ${PACK_COMMON_OPTIONS}  -D__$(MP_PROCESSOR_OPTION)__  -Wl,-Map="dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.map"    -o dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX} ${OBJECTFILES_QUOTED_IF_SPACED}      -DXPRJ_default=$(CND_CONF)  -DFW_MINOR=0 -DFW_MAJOR=0 -DFW_BUILD=1 -DTWI_ADDRESS=3 $(COMPARISON_BUILD)  -Wl,--defsym=__MPLAB_BUILD=1$(MP_EXTRA_LD_POST)$(MP_LINKER_FILE_OPTION) -Wl,--gc-sections -Wl,--start-group  -Wl,-lm -Wl,--end-group
	${MP_CC_DIR}/avr-objcopy -j .text -O ihex "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.hex"
	${MP_CC_DIR}/avr-objcopy -j .eeprom --set-section-flags=.eeprom=alloc,load --change-section-lma .eeprom=0 --no-change-warnings -O ihex "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.eep" || exit 0
	${MP_CC_DIR}/avr-objdump -h -S "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" > "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.lss"

	${MP_CC_DIR}/avr-objcopy -j .user_signatures --set-section-flags=.user_signatures=alloc,load --change-section-lma .user_signatures=0 --no-change-warnings -O ihex "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.${DEBUGGABLE_SUFFIX}" "dist/${CND_CONF}/${IMAGE_TYPE}/fwfield.X.${IMAGE_TYPE}.usersignatures" || exit 0

endif


# Subprojects
.build-subprojects:


# Subprojects
.clean-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r build/default
	${RM} -r dist/default

# Enable dependency checking
.dep.inc: .depcheck-impl

DEPFILES=$(shell "${PATH_TO_IDE_BIN}"mplabwildcard ${POSSIBLE_DEPFILES})
ifneq (${DEPFILES},)
include ${DEPFILES}
endif
