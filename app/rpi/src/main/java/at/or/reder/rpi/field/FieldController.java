/*
 * Copyright 2020 Wolfgang Reder
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
package at.or.reder.rpi.field;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author wolfi
 */
public class FieldController implements AutoCloseable {

  private final class FieldImpl implements Field {

    private final int address;
    private final EnumSet<State> moduleState = EnumSet.noneOf(State.class);
    private final Set<ChangeListener> keyChangeListener = new CopyOnWriteArraySet<>();
    private final AtomicReference<ModuleState> lastSymbolState = new AtomicReference<>();
    private final AtomicReference<ModuleType> lastSymbolType = new AtomicReference<>();
    private final ChangeEvent event = new ChangeEvent(this);

    public FieldImpl(int address) {
      this.address = address;
    }

    @Override
    public boolean isKeyPressed() {
      synchronized (moduleState) {
        return moduleState.contains(State.KEY_PRESSED);
      }
    }

    @Override
    public Set<State> getLastState() {
      synchronized (moduleState) {
        if (moduleState.isEmpty()) {
          return Collections.emptySet();
        } else {
          return EnumSet.copyOf(moduleState);
        }
      }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
      if (l != null) {
        keyChangeListener.add(l);
      }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
      keyChangeListener.remove(l);
    }

    private void fireChange() {
      keyChangeListener.forEach((l) -> l.stateChanged(event));
    }

    @Override
    public short getAddress() {
      return (short) address;
    }

    void processStateEvent(ByteBuffer buffer) {

      Set<State> states = State.bitfieldToSet(buffer.get(2));
      ModuleState ms = null;
      try {
        ms = ModuleState.valueOf(lastSymbolType.get(),
                                 buffer.get(3));
      } catch (Throwable th) {
      }
      synchronized (moduleState) {
        moduleState.clear();
        moduleState.addAll(states);
        lastSymbolState.set(ms);
      }
      SwingUtilities.invokeLater(this::fireChange);
    }

    @Override
    public Set<State> getState() throws IOException, TimeoutException, InterruptedException {
      int tmp = receive(this,
                        Register.STATE);
      EnumSet<State> result = State.bitfieldToSet(tmp);
      synchronized (moduleState) {
        moduleState.clear();
        moduleState.addAll(result);
      }
      return result;
    }

    @Override
    public int getLeds() throws IOException, InterruptedException, TimeoutException {
      return receive(this,
                     Register.LED);
    }

    @Override
    public void setLeds(int leds) throws IOException, InterruptedException, TimeoutException {
      send(this,
           Register.LED,
           Operation.WRITE,
           leds);
    }

    @Override
    public int getBlinkMask() throws IOException, TimeoutException, InterruptedException {
      return receive(this,
                     Register.BLINK_MASK);
    }

    @Override
    public void setBlinkMask(int bm) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.BLINK_MASK,
           Operation.WRITE,
           bm);
    }

    @Override
    public int getBlinkPhase() throws IOException, TimeoutException, InterruptedException {
      return receive(this,
                     Register.BLINK_PHASE);
    }

    @Override
    public void setBlinkPhase(int bp) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.BLINK_PHASE,
           Operation.WRITE,
           bp);
    }

    @Override
    public int getBlinkDivider() throws IOException, TimeoutException, InterruptedException {
      return receive(this,
                     Register.BLINK_DIVIDER);
    }

    @Override
    public void setBlinkDivider(int blinkDivider) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.BLINK_DIVIDER,
           Operation.WRITE,
           blinkDivider);
    }

    @Override
    public int getPWM() throws IOException, InterruptedException, TimeoutException {
      return receive(this,
                     Register.PWM);
    }

    @Override
    public void setPWM(int pwm) throws InterruptedException, TimeoutException, IOException {
      send(this,
           Register.PWM,
           Operation.WRITE,
           pwm);
    }

    @Override
    public int getDefaultPWM() throws IOException, TimeoutException, InterruptedException {
      return receive(this,
                     Register.DEFAULT_PWM);
    }

    @Override
    public void setDefaultPWM(int defaultPWM) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.DEFAULT_PWM,
           Operation.WRITE,
           defaultPWM);
    }

    @Override
    public float getVCC() throws IOException, InterruptedException, TimeoutException {
      int tmp = receive(this,
                        Register.VCC);
      if (tmp > 0) {
        return (1.22f * 1024f) / tmp;
      }
      return Float.NaN;
    }

    @Override
    public int getCalibration() throws IOException, TimeoutException, InterruptedException {
      return receive(this,
                     Register.VCC_CALIBRATION) & 0xff;
    }

    @Override
    public void setCalibration(int cal) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.VCC_CALIBRATION,
           Operation.WRITE,
           cal);
    }

    @Override
    public Version getVersion() throws IOException, TimeoutException, InterruptedException {
      int version = receive(this,
                            Register.FW_VERSION) & 0xffff;
      return new Version(version);
    }

    @Override
    public int getDebounce() throws IOException, TimeoutException, InterruptedException {
      return receive(this,
                     Register.DEBOUNCE);
    }

    @Override
    public void setDebounce(int deb) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.DEBOUNCE,
           Operation.WRITE,
           deb);
    }

    @Override
    public ModuleType getLastModuleType() {
      return lastSymbolType.get();
    }

    @Override
    public ModuleType getModuleType() throws IOException, TimeoutException, InterruptedException {
      int tmp = receive(this,
                        Register.MODULE_TYPE);
      ModuleType type = ModuleType.valueOfMagic(tmp);
      lastSymbolType.set(type);
      return type;
    }

    @Override
    public void setModuleType(ModuleType type) throws IOException, TimeoutException, InterruptedException {
      send(this,
           Register.MODULE_TYPE,
           Operation.WRITE,
           type.getMagic());
      lastSymbolType.set(type);
    }

    @Override
    public ModuleState getLastModuleState() {
      return lastSymbolState.get();
    }

    @Override
    public ModuleState getModuleState() throws IOException, TimeoutException, InterruptedException {
      int magic = receive(this,
                          Register.MODULE_STATE);
      if (magic != -1) {
        ModuleState result = ModuleState.valueOf(magic);
        lastSymbolState.set(result);
        return result;
      }
      throw new IOException("Cannot read Module state");
    }

    @Override
    public void setModuleState(ModuleState ms) throws IOException, TimeoutException, InterruptedException {
      int magic = ms.getMagic();
      send(this,
           Register.MODULE_STATE,
           Operation.WRITE,
           magic);
      lastSymbolState.set(ms);
    }

  }
  public static final long WAIT_FOR_STATE = 1000L;
  public static final long WAIT_FOR_FUTURE = 2000L;
  public static final int MAX_FIELD_ADDRESS = 127;

  private enum ReaderState {
    IDLE,
    ACK_PENDING,
    ACK,
    ERROR;
  }
  private final SerialPort port;
  private volatile boolean dataPending;
  private volatile ReaderState state;
  private volatile ReaderState expectedState;
  private volatile IOException error;
  private final Object lock = new Object();
  private final ByteBuffer returnValue = ByteBuffer.allocate(4).
          order(ByteOrder.LITTLE_ENDIAN);
  private final ExecutorService exec = Executors.newSingleThreadExecutor();
  private final Map<Integer, FieldImpl> fieldMap = new HashMap<>();

  public FieldController(SerialPort port) {
    try {
      this.port = port;
      port.addEventListener(this::onSerialEvent);
      port.notifyOnDataAvailable(true);
      state = ReaderState.IDLE;
    } catch (TooManyListenersException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public int scanFields() {
    fieldMap.clear();
    for (int i = 1; i <= MAX_FIELD_ADDRESS; ++i) {
      try {
        FieldImpl field = new FieldImpl(1);
        Version version = field.getVersion();
        if (version != null) {
          fieldMap.put(i,
                       field);
        }
      } catch (IOException | TimeoutException | InterruptedException ex) {
      }
    }
    return fieldMap.size();
  }

  private ReaderState doAckPending(int b) {
    if (b == 6) {
      return ReaderState.ACK;
    } else {
      return ReaderState.ERROR;
    }
  }

  private ReaderState doIdle(int b) {
    synchronized (lock) {
      if (!dataPending) {
        if (returnValue.position() == 0 && b == 6) {
          returnValue.put((byte) b);
          expectedState = ReaderState.IDLE;
          return ReaderState.ACK;
        }
      }
    }
    return state;
  }

  private ReaderState doAck(int b) {
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

  private void onSerialEvent(SerialPortEvent evt) {
    ReaderState newState = state;
    try {
      try ( InputStream is = port.getInputStream()) {
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
        } while (b != -1 && newState != expectedState && newState !=
                                                         ReaderState.ERROR);
      } catch (IOException ex) {
        error = ex;
        newState = ReaderState.ERROR;
      }
    } finally {
      boolean wasPending = dataPending;
      setState(newState,
               wasPending);
      if (!wasPending && error == null) {
        FieldImpl field = fieldMap.get(returnValue.get(1) & 0xff);
        if (field != null) {
          field.processStateEvent(returnValue);
        }
        returnValue.rewind();
      }
    }
  }

  public boolean isIdle() {
    return state == ReaderState.IDLE;
  }

  private void initState(ReaderState expectedState,
                         boolean forRead) {
    state = forRead ? ReaderState.ACK : ReaderState.ACK_PENDING;
    dataPending = true;
    returnValue.rewind();
    this.expectedState = expectedState;
  }

  private int waitForState(ReaderState expectendState) throws InterruptedException {
    int result;
    synchronized (lock) {
      if (this.state != expectendState && state != ReaderState.ERROR) {
        lock.wait(WAIT_FOR_STATE);
      }
      if (this.state == expectendState) {
        result = returnValue.getShort(1) & 0xffff;
      } else {
        result = -1;
      }
      returnValue.rewind();
      dataPending = false;
    }
    return result;
  }

  private void send(Field field,
                    Register register,
                    Operation operation,
                    int a) throws IOException, InterruptedException, TimeoutException {
    Future<Void> result = exec.submit(() -> {
      initState(ReaderState.ACK,
                false);
      try {
        try ( OutputStream os = port.getOutputStream()) {
          os.write(
                  new byte[]{(byte) (field.getAddress() & 0x7f),
                             register.getIndex(),
                             operation.getMagic(),
                             (byte) a});
        }
        waitForState(ReaderState.ACK);
        if (error != null) {
          throw error;
        }
      } finally {
        error = null;
        setState(ReaderState.IDLE,
                 false);
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

  private int receive(Field field,
                      Register register) throws IOException, InterruptedException, TimeoutException {
    Future<Integer> result = exec.submit(() -> {
      initState(ReaderState.IDLE,
                true);
      int r = -1;
      try {
        try ( OutputStream os = port.getOutputStream()) {
          os.write(
                  new byte[]{(byte) (field.getAddress() & 0x7f),
                             register.getIndex(),
                             Operation.READ.getMagic(),
                             (byte) 0});
        }
        r = waitForState(ReaderState.IDLE);
        if (error != null) {
          throw error;
        }
      } finally {
        error = null;
        setState(ReaderState.IDLE,
                 false);
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

  private void setState(ReaderState s,
                        boolean forceNotify) {
    synchronized (lock) {
      state = s;
      if (forceNotify || s == ReaderState.IDLE) {
        lock.notifyAll();
      }
    }
  }

  @Override
  public void close() throws IOException {
    exec.shutdown();
    port.close();
  }

}
