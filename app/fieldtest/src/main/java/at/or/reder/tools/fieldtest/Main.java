/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wolfgang Reder
 */
public class Main implements AutoCloseable
{

  private boolean isRPI;
  private final ScheduledExecutorService exe = Executors.newSingleThreadScheduledExecutor();
  private RXTXPort port;
  private final ConcurrentLinkedQueue<Command> commandQueue = new ConcurrentLinkedQueue<>();
  private boolean exit;

  private Main() throws IOException, InterruptedException, PortInUseException, UnsupportedCommOperationException,
                        TooManyListenersException
  {
//    Native.
    String arch = System.getProperty("os.arch");
    isRPI = "arm".equals(arch);
    port = new RXTXPort("/dev/ttyS0");
    port.setSerialPortParams(57600,
                             SerialPort.DATABITS_8,
                             SerialPort.STOPBITS_1,
                             SerialPort.PARITY_NONE);
    port.addEventListener(this::onSerialEvent);
    port.notifyOnDataAvailable(true);
    port.notifyOnFramingError(true);
    port.notifyOnParityError(true);
  }

  private void run() throws IOException, InterruptedException
  {
    Command cmd = new Command(new byte[]{1, 1, (byte) 0x55, 0});
    commandQueue.offer(new Command(new byte[]{4, 1, (byte) 0x2, 0}));
    commandQueue.offer(new Command(new byte[]{4, 0, 0, 0}));
    commandQueue.offer(new Command(new byte[]{4, 1, (byte) 0xfe, 0}));
    port.getOutputStream().write(cmd.getBuffer());
    port.getOutputStream().flush();
    while (!exit) {
      synchronized (this) {
        wait(10000);
      }
    }
  }

  @Override
  public void close() throws IOException
  {
    port.close();
  }

  private void onSerialEvent(SerialPortEvent event)
  {
    if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
      try {
        int b = port.getInputStream().read();
        if (b == 6) {
          Command cmd = commandQueue.poll();
          if (cmd != null) {
            try (OutputStream os = port.getOutputStream()) {
              os.write(cmd.getBuffer());
            }
          } else {
            synchronized (this) {
              exit = true;
              notifyAll();
            }
          }
        } else {
          System.err.println("Received byte " + b);
        }
      } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                   null,
                                                   ex);
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException, PortInUseException,
                                                UnsupportedCommOperationException, TooManyListenersException
  {
    try (Main main = new Main()) {
      main.run();
    }
  }

}
