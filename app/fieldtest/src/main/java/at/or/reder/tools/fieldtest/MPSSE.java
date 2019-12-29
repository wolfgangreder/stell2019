/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;

/**
 *
 * @author Wolfgang Reder
 */
public interface MPSSE extends Library
{

  MPSSE INSTANCE = (MPSSE) Native.load("mpsse_p",
                                       MPSSE.class);

  /**
   * Generate start condition before transmitting
   */
  public static final int I2C_TRANSFER_OPTIONS_START_BIT = 0x00000001;

  /**
   * Generate stop condition before transmitting
   */
  public static final int I2C_TRANSFER_OPTIONS_STOP_BIT = 0x00000002;

  /**
   * Continue transmitting data in bulk without caring about Ack or nAck from device if this bit is not set. If this bit is set
   * then stop transitting the data in the buffer when the device nAcks
   */
  public static final int I2C_TRANSFER_OPTIONS_BREAK_ON_NACK = 0x00000004;

  /**
   * libMPSSE-I2C generates an ACKs for every byte read. Some I2C slaves require the I2C master to generate a nACK for the last
   * data byte read. Setting this bit enables working with such I2C slaves
   */
  public static final int I2C_TRANSFER_OPTIONS_NACK_LAST_BYTE = 0x00000008;

  /**
   * no address phase, no USB interframe delays
   */
  public static final int I2C_TRANSFER_OPTIONS_FAST_TRANSFER_BYTES = 0x00000010;
  public static final int I2C_TRANSFER_OPTIONS_FAST_TRANSFER_BITS = 0x00000020;
  public static final int I2C_TRANSFER_OPTIONS_FAST_TRANSFER = 0x00000030;

  /**
   * if I2C_TRANSFER_OPTION_FAST_TRANSFER is set then setting this bit would mean that the address field should be ignored. The
   * address is either a part of the data or this is a special I2C frame that doesn't require an address
   */
  public static final int I2C_TRANSFER_OPTIONS_NO_ADDRESS = 0x00000040;

  public static final byte I2C_CMD_GETDEVICEID_RD = (byte) 0xF9;
  public static final byte I2C_CMD_GETDEVICEID_WR = (byte) 0xF8;

  public static final byte I2C_GIVE_ACK = 1;
  public static final byte I2C_GIVE_NACK = 0;

  /**
   * 3-phase clocking is enabled by default. Setting this bit in ConfigOptions will disable it
   */
  public static final short I2C_DISABLE_3PHASE_CLOCKING = 0x0001;

  /**
   * The I2C master should actually drive the SDA line only when the output is LOW. It should be tristate the SDA line when the
   * output should be high. This tristating the SDA line during output HIGH is supported only in FT232H chip. This feature is
   * called DriveOnlyZero feature and is enabled when the following bit is set in the options parameter in function I2C_Init
   */
  public static final short I2C_ENABLE_DRIVE_ONLY_ZERO = 0x0002;

  /**
   * ***************************************************************************
   */
  /*								Type defines								  */
  /**
   * ***************************************************************************
   */
  public static final int I2C_CLOCK_STANDARD_MODE = 100000;
  /* 100kb/sec */
  public static final int I2C_CLOCK_FAST_MODE = 400000;
  /* 400kb/sec */
  public static final int I2C_CLOCK_FAST_MODE_PLUS = 1000000;
  /* 1000kb/sec */
  public static final int I2C_CLOCK_HIGH_SPEED_MODE = 3400000;

  /* 3.4Mb/sec */
  /**
   * Channel configuration information
   */
  public static class ChannelConfig extends Structure
  {

    public int ClockRate;
    public byte LatencyTimer;
    public int Options;
  };

  public static class FT_Device_List_Info_Node extends Structure
  {

    public int Flags;
    public int Type;
    public int ID;
    public short LocId;
    public String SerialNumber;
    public String Description;
    public NativeLong ftHandle;
  };

  /**
   *
   * @param numChannels
   * @return
   */
  NativeLong I2C_GetNumChannels(IntByReference numChannels);

  /**
   *
   * @param index indexOfChannel
   * @param chanInfo pointer to {@link at.or.reder.tools.fieldtest.MPSSE.FT_Device_List_Info_Node}
   * @return status
   */
  NativeLong I2C_GetChannelInfo(int index,
                                Pointer chanInfo);

  NativeLong I2C_OpenChannel(int index,
                             NativeLongByReference handle);

  NativeLong I2C_InitChannel(NativeLong handle,
                             ChannelConfig config);

  NativeLong I2C_CloseChannel(NativeLong handle);

  /**
   *
   * @param handle handle
   * @param deviceAddress address
   * @param sizeToTransfer byte2transfer
   * @param buffer buffer
   * @param sizeTransfered pointer to int
   * @param options opt
   * @return status
   */
  NativeLong I2C_DeviceRead(NativeLong handle,
                            int deviceAddress,
                            int sizeToTransfer,
                            Pointer buffer,
                            IntByReference sizeTransfered,
                            int options);

  NativeLong I2C_DeviceWrite(NativeLong handle,
                             int deviceAddress,
                             int sizeToTransfer,
                             Pointer buffer,
                             IntByReference sizeTransfered,
                             int options);

  void Init_libMPSSE();

  void Cleanup_libMPSSE();

  NativeLong FT_WriteGPIO(NativeLong handle,
                          byte dir,
                          byte value);

  NativeLong FT_ReadGPIO(NativeLong handle,
                         ByteByReference value);

}
