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
import java.util.EnumSet;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Wolfgang Reder
 */
public final class FieldImpl implements Field
{

  private enum ReaderState
  {
    IDLE,
    ACK_PENDING,
    ACK,
    ERROR;
  }
  private final SerialPort port;
  private final short address;
  private volatile ReaderState state;
  private volatile ReaderState expectedState;
  private volatile IOException error;
  private final Object lock = new Object();
  private final ByteBuffer returnValue = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
  private final ExecutorService exec = Executors.newSingleThreadExecutor();

  public FieldImpl(SerialPort port,
                   int address)
  {
    try {
      this.port = port;
      this.address = (short) address;
      port.addEventListener(this::onSerialEvent);
      port.notifyOnDataAvailable(true);
      state = ReaderState.IDLE;
    } catch (TooManyListenersException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private ReaderState doAckPending(int b)
  {
    if (b == 6) {
      return ReaderState.ACK;
    } else {
      return ReaderState.ERROR;
    }
  }

  private ReaderState doIdle(int b)
  {
    return state;
  }

  private ReaderState doAck(int b)
  {
    if (b == -1) {
      return state;
    }
    synchronized (lock) {
      if (returnValue.hasRemaining()) {
        returnValue.put((byte) b);
      }
      if (returnValue.hasRemaining()) {
        return ReaderState.ACK;
      } else {
        returnValue.rewind();
        return ReaderState.IDLE;
      }
    }
  }

  private void onSerialEvent(SerialPortEvent evt)
  {
    ReaderState newState = state;
    try {
      try (InputStream is = port.getInputStream()) {
        int b;
        do {
          b = is.read();
          switch (newState) {
            case ACK_PENDING:
              newState = doAckPending(b);
              break;
            case IDLE:
              newState = doIdle(b);
              break;
            case ACK:
              newState = doAck(b);
              break;
            case ERROR:
              return;
          }
        } while (b != -1 && newState != expectedState && newState != ReaderState.ERROR);
      } catch (IOException ex) {
        error = ex;
        newState = ReaderState.ERROR;
      }
    } finally {
      synchronized (lock) {
        lock.notifyAll();
      }
      setState(newState);
    }
  }

  @Override
  public short getAddress()
  {
    return address;
  }

  public boolean isIdle()
  {
    return state == ReaderState.IDLE;
  }

  private void initState(ReaderState expectedState)
  {
    state = ReaderState.ACK_PENDING;
    returnValue.rewind();
    this.expectedState = expectedState;
  }

  private int waitForState(ReaderState expectendState) throws InterruptedException, TimeoutException
  {
    int result = -1;
    synchronized (lock) {
      if (this.state != expectendState && state != ReaderState.ERROR) {
        lock.wait(1000L);
      }
      if (this.state != expectendState && state != ReaderState.ERROR) {
        throw new TimeoutException();
      } else {
        result = returnValue.getShort() & 0xffff;
      }
    }
    return result;
  }

  private void send(Register register,
                    Operation operation,
                    int a,
                    int b) throws IOException, InterruptedException, TimeoutException
  {
    Future<Void> result = exec.submit(() -> {
      initState(ReaderState.ACK);
      try {
        try (OutputStream os = port.getOutputStream()) {
          os.write(new byte[]{register.getIndex(), operation.getMagic(), (byte) a, (byte) b});
        }
        waitForState(ReaderState.ACK);
        if (error != null) {
          throw error;
        }
      } finally {
        error = null;
        setState(ReaderState.IDLE);
      }
      return null;
    });
    try {
      result.get(2,
                 TimeUnit.SECONDS);
    } catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      if (cause instanceof InterruptedException) {
        throw (InterruptedException) cause;
      }
      if (cause instanceof TimeoutException) {
        throw (TimeoutException) cause;
      }
      throw new IOException(cause);
    }
  }

  private int sendReceive(Register register,
                          Operation operation,
                          int a,
                          int b) throws IOException, InterruptedException, TimeoutException
  {
    Future<Integer> result = exec.submit(() -> {
      initState(ReaderState.IDLE);
      int r = -1;
      try {
        try (OutputStream os = port.getOutputStream()) {
          os.write(new byte[]{register.getIndex(), operation.getMagic(), (byte) a, (byte) b});
        }
        r = waitForState(ReaderState.IDLE);
        if (error != null) {
          throw error;
        }
      } finally {
        error = null;
        setState(ReaderState.IDLE);
      }
      return r;
    });
    try {
      return result.get(2,
                        TimeUnit.SECONDS);
    } catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      if (cause instanceof InterruptedException) {
        throw (InterruptedException) cause;
      }
      if (cause instanceof TimeoutException) {
        throw (TimeoutException) cause;
      }
      throw new IOException(cause);
    }
  }

  private void setState(ReaderState s)
  {
    synchronized (lock) {
      state = s;
      if (s == ReaderState.IDLE) {
        lock.notifyAll();
      }
    }
  }

  @Override
  public Set<State> getState() throws IOException, TimeoutException, InterruptedException
  {
    int tmp = sendReceive(Register.STATE,
                          Operation.READ,
                          0,
                          0);
    EnumSet<State> result = EnumSet.noneOf(State.class);
    for (State s : State.values()) {
      if ((tmp & s.getMagic()) != 0) {
        result.add(s);
      }
    }
    return result;
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
  public int getBlinkDivider() throws IOException, TimeoutException, InterruptedException
  {
    return sendReceive(Register.BLINK_DIVIDER,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setBlinkDivider(int blinkDivider) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.BLINK_DIVIDER,
         Operation.WRITE,
         blinkDivider,
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
  public int getCalibration() throws IOException, TimeoutException, InterruptedException
  {
    return sendReceive(Register.VCC_CALIBRATION,
                       Operation.READ,
                       0,
                       0) & 0xff;
  }

  @Override
  public void setCalibration(int cal) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.VCC_CALIBRATION,
         Operation.WRITE,
         cal,
         0);
  }

  @Override
  public Version getVersion() throws IOException, TimeoutException, InterruptedException
  {
    int version = sendReceive(Register.FW_VERSION,
                              Operation.READ,
                              0,
                              0) & 0xffff;
    int build = sendReceive(Register.FW_BUILD,
                            Operation.READ,
                            0,
                            0);
    return new Version(version,
                       build);
  }

  @Override
  public int getDebounce() throws IOException, TimeoutException, InterruptedException
  {
    return sendReceive(Register.DEBOUNCE,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setDebounce(int deb) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.DEBOUNCE,
         Operation.WRITE,
         deb,
         0);
  }

  @Override
  public void close() throws IOException
  {
    port.close();
  }

}
