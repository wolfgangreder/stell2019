/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;

/**
 *
 * @author Wolfgang Reder
 */
public class Main
{

  public static void main(String[] args)
  {
    System.load("/home/wolfi/projects/mpsse_p/libftd2xx.so");
    MPSSE mp = MPSSE.INSTANCE;
    mp.Init_libMPSSE();
    try {
      IntByReference numChannels = new IntByReference();
      NativeLong status = mp.I2C_GetNumChannels(numChannels);
      int nc = numChannels.getValue();
      long state = status.longValue();
      System.err.println("Status=" + state);
      System.err.println("Numchannels=" + nc);
      NativeLongByReference handleRef = new NativeLongByReference();
      status = mp.I2C_OpenChannel(0,
                                  handleRef);
      NativeLong handle = handleRef.getValue();
      System.err.println("Status=" + status);
      System.err.println("Handle=" + handle);
    } finally {
      mp.Cleanup_libMPSSE();
    }
  }

}
