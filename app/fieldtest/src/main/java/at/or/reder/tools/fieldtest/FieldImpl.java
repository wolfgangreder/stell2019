/*
 * Copyright 2020 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.or.reder.tools.fieldtest;

import at.or.reder.tools.fieldtest.model.Field;
import at.or.reder.tools.fieldtest.model.ModuleState;
import at.or.reder.tools.fieldtest.model.ModuleType;
import at.or.reder.tools.fieldtest.model.Operation;
import at.or.reder.tools.fieldtest.model.Register;
import at.or.reder.tools.fieldtest.model.State;
import at.or.reder.tools.fieldtest.model.Version;
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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class FieldImpl implements Field
{

  public static final long WAIT_FOR_STATE = 1000L;
  public static final long WAIT_FOR_FUTURE = 2000L;

  private enum ReaderState
  {
    IDLE,
    ACK_PENDING,
    ACK,
    ERROR;
  }
  private final SerialPort port;
  private final short address;
  private volatile boolean dataPending;
  private volatile ReaderState state;
  private volatile ReaderState expectedState;
  private volatile IOException error;
  private final Object lock = new Object();
  private final ByteBuffer returnValue = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
  private final ExecutorService exec = Executors.newSingleThreadExecutor();
  private final ScheduledExecutorService poll = Executors.newSingleThreadScheduledExecutor();
  private final AtomicBoolean keyPressed = new AtomicBoolean();
  private final Set<ChangeListener> keyChangeListener = new CopyOnWriteArraySet<>();
  private final ChangeEvent event = new ChangeEvent(this);

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
//    poll.scheduleAtFixedRate(this::pollKey,
//                             50,
//                             50,
//                             TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean isKeyPressed()
  {
    return keyPressed.get();
  }

  @Override
  public void addChangeListener(ChangeListener l)
  {
    if (l != null) {
      keyChangeListener.add(l);
    }
  }

  @Override
  public void removeChangeListener(ChangeListener l)
  {
    keyChangeListener.remove(l);
  }

  private void pollKey()
  {
    try {
      boolean kp = getState().contains(State.KEY_PRESSED);
      if (keyPressed.compareAndSet(!kp,
                                   kp)) {
        SwingUtilities.invokeLater(this::fireChange);
      }
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(FieldImpl.class.getName()).log(Level.SEVERE,
                                                      null,
                                                      ex);
    }
  }

  private void fireChange()
  {
    for (ChangeListener l : keyChangeListener) {
      l.stateChanged(event);
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
    synchronized (lock) {
      if (!dataPending) {
        if (returnValue.position() == 0 && b == 6) {
          expectedState = ReaderState.IDLE;
          return ReaderState.ACK;
        }
      }
    }
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
      if (dataPending) {
        synchronized (lock) {
          lock.notifyAll();
        }
      } else {
        keyPressed.set(returnValue.get(0) != 0);
        returnValue.rewind();
        SwingUtilities.invokeLater(this::fireChange);
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

  private void initState(ReaderState expectedState,
                         boolean forRead)
  {
    state = forRead ? ReaderState.ACK : ReaderState.ACK_PENDING;
    dataPending = true;
    returnValue.rewind();
    this.expectedState = expectedState;
  }

  private int waitForState(ReaderState expectendState) throws InterruptedException
  {
    int result = -1;
    synchronized (lock) {
      if (this.state != expectendState && state != ReaderState.ERROR) {
        lock.wait(WAIT_FOR_STATE);
      }
      if (this.state == expectendState || state == ReaderState.ERROR) {
        result = returnValue.getShort() & 0xffff;
      }
      returnValue.rewind();
      dataPending = false;
    }
    return result;
  }

  private void send(Register register,
                    Operation operation,
                    int a,
                    int b) throws IOException, InterruptedException, TimeoutException
  {
    Future<Void> result = exec.submit(() -> {
      initState(ReaderState.ACK,
                false);
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
      result.get(WAIT_FOR_FUTURE,
                 TimeUnit.MILLISECONDS);
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
      initState(ReaderState.IDLE,
                true);
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
      return result.get(WAIT_FOR_FUTURE,
                        TimeUnit.MILLISECONDS);
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
  public int getDefaultPWM() throws IOException, TimeoutException, InterruptedException
  {
    return sendReceive(Register.DEFAULT_PWM,
                       Operation.READ,
                       0,
                       0);
  }

  @Override
  public void setDefaultPWM(int defaultPWM) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.DEFAULT_PWM,
         Operation.WRITE,
         defaultPWM,
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
    return new Version(version);
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
  public ModuleType getModuleType() throws IOException, TimeoutException, InterruptedException
  {
    int tmp = sendReceive(Register.MODULE_TYPE,
                          Operation.READ,
                          0,
                          0);
    return ModuleType.valueOfMagic(tmp);
  }

  @Override
  public void setModuleType(ModuleType type) throws IOException, TimeoutException, InterruptedException
  {
    send(Register.MODULE_TYPE,
         Operation.WRITE,
         type.getMagic(),
         0);
  }

  @Override
  public ModuleState getModuleState() throws IOException, TimeoutException, InterruptedException
  {
    int magic = sendReceive(Register.MODULE_STATE,
                            Operation.READ,
                            0,
                            0);
    if (magic != -1) {
      return ModuleState.valueOf(magic);
    }
    throw new IOException("Cannot read Module state");
  }

  @Override
  public void setModuleState(ModuleState ms) throws IOException, TimeoutException, InterruptedException
  {
    int magic = ms.getMagic();
    send(Register.MODULE_STATE,
         Operation.WRITE,
         magic,
         0);
  }

  @Override
  public void close() throws IOException
  {
    exec.shutdown();
    poll.shutdown();
    port.close();
  }

}
