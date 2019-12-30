/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Wolfgang Reder
 */
public final class FieldImpl implements Field
{

  private enum State
  {
    IDLE,
    ACK_PENDING,
    ACK,
    ERROR;
  }
  private final SerialPort port;
  private final short address;
  private volatile State state;
  private volatile State expectedState;
  private volatile IOException error;
  private final Object lock = new Object();
  private final ByteBuffer returnValue = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);

  public FieldImpl(SerialPort port,
                   int address)
  {
    try {
      this.port = port;
      this.address = (short) address;
      port.addEventListener(this::onSerialEvent);
      port.notifyOnDataAvailable(true);
      state = State.IDLE;
    } catch (TooManyListenersException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private State doAckPending(int b)
  {
    if (b == 6) {
      return State.ACK;
    } else {
      return State.ERROR;
    }
  }

  private State doIdle(int b)
  {
    return state;
  }

  private State doAck(int b)
  {
    if (b == -1) {
      return state;
    }
    synchronized (lock) {
      if (returnValue.hasRemaining()) {
        returnValue.put((byte) b);
      }
      if (returnValue.hasRemaining()) {
        return State.ACK;
      } else {
        returnValue.rewind();
        return State.IDLE;
      }
    }
  }

  private void onSerialEvent(SerialPortEvent evt)
  {
    try (InputStream is = port.getInputStream()) {
      int b;
      do {
        b = is.read();
        switch (state) {
          case ACK_PENDING:
            state = doAckPending(b);
            break;
          case IDLE:
            state = doIdle(b);
            break;
          case ACK:
            state = doAck(b);
            break;
          case ERROR:
            return;
        }
      } while (b != -1 && state != expectedState && state != State.ERROR);
    } catch (IOException ex) {
      error = ex;
      state = State.ERROR;
    } finally {
      synchronized (lock) {
        lock.notifyAll();
      }
    }
  }

  @Override
  public short getAddress()
  {
    return address;
  }

  public boolean isIdle()
  {
    return state == State.IDLE;
  }

  private void initState(State expectedState)
  {
    synchronized (lock) {
      state = State.ACK_PENDING;
      returnValue.rewind();
      this.expectedState = expectedState;
    }
  }

  private int waitForState(State expectendState) throws InterruptedException, TimeoutException
  {
    int result = -1;
    synchronized (lock) {
      while (this.state != expectendState && state != State.ERROR) {
        lock.wait(1000L);
        if (this.state != expectendState && state != State.ERROR) {
          throw new TimeoutException();
        } else {
          result = returnValue.getShort() & 0xffff;
        }
      }
    }
    return result;
  }

  private void send(Register register,
                    Operation operation,
                    int a,
                    int b) throws IOException, InterruptedException, TimeoutException
  {
    checkIdle();
    initState(State.ACK);
    try {
      try (OutputStream os = port.getOutputStream()) {
        os.write(new byte[]{register.getIndex(), operation.getMagic(), (byte) a, (byte) b});
      }
      waitForState(State.ACK);
      if (error != null) {
        throw error;
      }
    } finally {
      error = null;
      state = State.IDLE;
    }
  }

  private int sendReceive(Register register,
                          Operation operation,
                          int a,
                          int b) throws IOException, InterruptedException, TimeoutException
  {
    checkIdle();
    initState(State.IDLE);
    int result = -1;
    try {
      try (OutputStream os = port.getOutputStream()) {
        os.write(new byte[]{register.getIndex(), operation.getMagic(), (byte) a, (byte) b});
      }
      result = waitForState(State.IDLE);
      if (error != null) {
        throw error;
      }
    } finally {
      error = null;
      state = State.IDLE;
    }
    return result;
  }

  private void checkIdle()
  {
    if (!isIdle()) {
      throw new IllegalStateException("Field is not idle");
    }
  }

  @Override
  public int getLeds() throws IOException, InterruptedException, TimeoutException
  {
    return sendReceive(Register.LED,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setLeds(int leds) throws IOException, InterruptedException, TimeoutException
  {
    send(Register.LED,
         Operation.WRITE,
         leds,
         0);
  }

  @Override
  public int getBlinkMask() throws IOException, TimeoutException, InterruptedException
  {
    return sendReceive(Register.BLINK_MASK,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setBlinkMask(int bm) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.BLINK_MASK,
         Operation.WRITE,
         bm,
         0);
  }

  @Override
  public int getBlinkPhase() throws IOException, TimeoutException, InterruptedException
  {
    return sendReceive(Register.BLINK_PHASE,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setBlinkPhase(int bp) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.BLINK_PHASE,
         Operation.WRITE,
         bp,
         0);
  }

  @Override
  public int getPWM() throws IOException, InterruptedException, TimeoutException
  {
    return sendReceive(Register.PWM,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setPWM(int pwm) throws InterruptedException, TimeoutException, IOException
  {
    send(Register.PWM,
         Operation.WRITE,
         pwm,
         0);
  }

  @Override
  public float getVCC() throws IOException, InterruptedException, TimeoutException
  {
    int tmp = sendReceive(Register.VCC,
                          Operation.READ,
                          0,
                          0);
    if (tmp > 0) {
      return (1.22f * 1024f) / tmp;
    }
    return Float.NaN;
  }

  @Override
  public byte getCalibration() throws IOException, TimeoutException, InterruptedException
  {
    return (byte) sendReceive(Register.VCC_CALIBRATION,
                              Operation.READ,
                              0,
                              0);
  }

  @Override
  public void setCalibration(byte cal) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.VCC_CALIBRATION,
         Operation.WRITE,
         cal,
         0);
  }

  @Override
  public void close() throws IOException
  {
    port.close();
  }

}
