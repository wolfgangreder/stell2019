/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750.impl;

import at.or.reder.controller.PSController;
import at.or.reder.controller.PSControllerEvent;
import at.or.reder.controller.PSControllerEventListener;
import at.or.reder.controller.PSControllerEventType;
import at.or.reder.controller.PSDirection;
import at.or.reder.dcc.Controller;
import at.or.reder.dcc.Direction;
import at.or.reder.dcc.Locomotive;
import at.or.reder.dcc.LocomotiveFuncEvent;
import at.or.reder.dcc.LocomotiveFuncEventListener;
import at.or.reder.dcc.PowerMode;
import at.or.reder.dcc.PowerPort;
import at.or.reder.tools.edk750.EDK750;
import at.or.reder.tools.edk750.EDKAxis;
import at.or.reder.tools.edk750.EDKAxisEvent;
import at.or.reder.tools.edk750.EDKAxisListener;
import at.or.reder.tools.edk750.EDKExceptionListener;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.util.Lookup;

class EDK750Impl implements EDK750
{

  public static final Logger LOGGER = Logger.getLogger("at.or.reder.edk750");
  private final PropertyChangeListener controllerListener = this::onControllerPropertyChange;
  private final PSControllerEventListener controllerEventListener = this::onControllerEvent;
  private final LocomotiveFuncEventListener funcListener = this::onFunc;
  private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
  private final Set<EDKExceptionListener> exceptionListener = new CopyOnWriteArraySet<>();
  private final Set<EDKAxisListener> axisListener = new CopyOnWriteArraySet<>();
  protected final EDKAxisController winch;
  protected final EDKAxisController beam;
  protected final EDKAxisController beamShift;

  private PSController controller;
  protected Controller dccController;
  private int address;
  protected Locomotive _edk;
  private String controllerId;
  private int lastWinch;
  private int lastRotate;
  private boolean axisLock = true;
  private boolean strictMode = true;
  private boolean emergencyMode = false;
  private boolean f1On = false;
  private final Map<String, String> config;

  public EDK750Impl(int address,
                    Controller dcc,
                    PSController controller,
                    Map<String, String> config) throws IOException
  {
    this.address = address;
    winch = EDKAxisController.getWinch(null);
    beam = EDKAxisController.getBeam(null);
    beamShift = EDKAxisController.getBeamShift(null);
    setDCCController(dcc);
    if (config != null) {
      this.config = Collections.unmodifiableMap(new HashMap<>(config));
    } else {
      this.config = Collections.emptyMap();
    }
    setPSController(controller);
    if (_edk != null) {
    }
  }

  @Override
  public boolean isAxisLock()
  {
    return axisLock;
  }

  @Override
  public void setAxisLock(boolean axisLock)
  {
    this.axisLock = axisLock;
  }

  @Override
  public boolean isStrictMode()
  {
    return strictMode;
  }

  @Override
  public void setStrictMode(boolean strictMode)
  {
    this.strictMode = strictMode;
  }

  @Override
  public final boolean isStarted()
  {
    Locomotive edk = getLocomotive();
    boolean f1;
    synchronized (this) {
      f1 = f1On;
    }
    boolean result = edk != null && (!isStrictMode() || f1);
    LOGGER.log(Level.FINEST,
               () -> "Engine is " + (result ? "started"
                                     : "not running"));
    return result;
  }

  private Locomotive getLocomotive()
  {
    synchronized (this) {
      return _edk;
    }
  }

  @Override
  public final void setStarted(boolean s)
  {
    Locomotive e = getLocomotive();
    if (e != null && isStarted() != s) {
      try {
        e.setFunction(1,
                      s);
      } catch (IOException ex) {
        processException("setStarted",
                         ex);
      }
    }
  }

  private void onFunc(LocomotiveFuncEvent evt)
  {
    if (evt.getFuncNr() == 1) {
      boolean f1;
      if (evt.getFuncValue() != 0) {
        f1 = true;
      } else {
        f1 = false;
      }
      synchronized (this) {
        boolean wasStarted = isStarted();
        f1On = f1;
        boolean started = isStarted();
        if (wasStarted != started) {
          SwingUtilities.invokeLater(() -> propSupport.firePropertyChange(PROP_STARTED,
                                                                          wasStarted,
                                                                          started));
        }
      }
    }
  }

  @Override
  public final PSController getPSController()
  {
    return controller;
  }

  @Override
  public final String getControllerId()
  {
    return controllerId;
  }

  @Override
  public final synchronized Controller getDCCController()
  {
    return dccController;
  }

  @Override
  public final synchronized void setDCCController(Controller dcc) throws IOException
  {
    if (dccController != dcc) {
      disconnectDCC();
      this.dccController = dcc;
      connectDCC();
    }
  }

  private void disconnectDCC()
  {
    assert Thread.holdsLock(this);
    if (_edk != null) {
      _edk.removeLocomotiveFuncEventListener(funcListener);
    }
    if (winch != null) {
      winch.setLoc(null);
    }
    if (beam != null) {
      beam.setLoc(null);
    }
    if (beamShift != null) {
      beamShift.setLoc(null);
    }
    _edk = null;
    dccController = null;
  }

  private void connectDCC() throws IOException
  {
    assert Thread.holdsLock(this);
    if (dccController != null) {
      try {
        _edk = dccController.getLocomotive(address);
        _edk.takeOwnership();
        winch.setLoc(_edk);
        beam.setLoc(_edk);
        beamShift.setLoc(_edk);
        setStarted(_edk.isFunction(1));
      } catch (TimeoutException ex) {
        throw new IOException(ex);
      }
      _edk.addLocomotiveFuncEventListener(funcListener);
    }
  }

  @Override
  public int getAddress()
  {
    return address;
  }

  @Override
  public void setAddress(int address) throws IOException
  {
    if (this.address != address) {
      this.address = address;
      disconnectDCC();
      connectDCC();
    }
  }

  @Override
  public final void setControllerId(String controllerId)
  {
    this.controllerId = controllerId;
  }

  @Override
  public final void setPSController(PSController controller)
  {
    if (controller != this.controller) {
      disconnect();
      this.controller = controller;
      connect();
    }
  }

  @SuppressWarnings("UseSpecificCatch")
  private float getFloatConfig(String key,
                               float defaultValue)
  {
    String tmp = config.getOrDefault(key,
                                     null);
    float result = defaultValue;
    if (tmp != null) {
      try {
        result = Float.parseFloat(tmp);
      } catch (Throwable th) {
      }
    }
    return result;
  }

  @SuppressWarnings("UseSpecificCatch")
  private int getIntConfig(String key,
                           int defaultValue)
  {
    String tmp = config.getOrDefault(key,
                                     null);
    int result = defaultValue;
    if (tmp != null) {
      try {
        result = Integer.parseInt(tmp);
      } catch (Throwable th) {
      }
    }
    return result;
  }

  private void connect()
  {
    if (controller != null) {
      controller.addPropertyChangeListener(controllerListener);
      controllerId = controller.getId();
      controller.getFactory().addPSControllerEventListener(controllerEventListener);
      for (PSController.Axis a : PSController.Axis.values()) {
        controller.setDeadRange(a,
                                getFloatConfig("dead." + a.name(),
                                               0.1f));
        controller.setRangeMax(a,
                               getIntConfig("max." + a.name(),
                                            a != PSController.Axis.RX ? 1023 : 64));
      }
    }
  }

  private void disconnect()
  {
    try {
      this.beam.stop();
    } catch (IOException ex) {
      processException("disconnect.beam",
                       ex);
    }
    try {
      this.beamShift.stop();
    } catch (IOException ex) {
      processException("disconnect.beamShift",
                       ex);
    }
    try {
      this.winch.stop();
    } catch (IOException ex) {
      processException("disconnect.winch",
                       ex);
    }
    if (controller != null) {
      controller.getFactory().removePSControllerEventListener(controllerEventListener);
      controller.removePropertyChangeListener(controllerListener);
    }
  }

  private void onControllerEvent(PSControllerEvent evt)
  {
    if (evt.getType() == PSControllerEventType.CLOSE && evt.getController().getId().equals(controllerId)) {
      setPSController(null);
    }
    if (controller == null && evt.getType() == PSControllerEventType.OPEN && evt.getController().getId().equals(controllerId)) {
      setPSController(evt.getController());
    }
  }

  private void onControllerPropertyChange(PropertyChangeEvent evt)
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    boolean em = isInEmergencyMode();
    try {
      switch (evt.getPropertyName()) {
        case PSController.PROP_LEFT:
          if (!em && isStarted()) {
            processLeft(evt);
          }
          break;
        case PSController.PROP_RIGHT:
          if (!em && isStarted()) {
            processRight(evt);
          }
          break;
        case PSController.PROP_APRESSED:
          processA(evt);
          break;
        case PSController.PROP_BPRESSED:
          processB(evt);
          break;
        case PSController.PROP_XPRESSED:
          processX(evt);
          break;
        case PSController.PROP_YPRESSED:
          processY(evt);
          break;
        case PSController.PROP_DIRECTION:
          processDirection(evt);
          break;
        case PSController.PROP_L1PRESSED:
          processL1(evt);
          break;
        case PSController.PROP_L2LEVEL:
          if (!em && isStarted()) {
            processZAxis(evt);
          }
          break;
        case PSController.PROP_MODEPRESSED:
          processMode(evt);
          break;
        case PSController.PROP_R1PRESSED:
          processR1(evt);
          break;
        case PSController.PROP_R2LEVEL:
          if (!em && isStarted()) {
            processZAxis(evt);
          }
          break;
        case PSController.PROP_SELECTPRESSED:
          processSelect(evt);
          break;
        case PSController.PROP_STARTPRESSED:
          processStart(evt);
          break;
        case PSController.PROP_LEFTPRESSED:
        case PSController.PROP_RIGHTPRESSED:
          if (controller.isLeftPressed() && controller.isRightPressed()) {
            edk.getController().setPowerMode(PowerPort.OUT_1,
                                             PowerMode.OFF);
            edk.getController().setPowerMode(PowerPort.OUT_2,
                                             PowerMode.OFF);
            setInEmergencyMode(true,
                               true);
          } else if (controller.isLeftPressed() & !em) {
            edk.getController().setPowerMode(PowerPort.OUT_1,
                                             PowerMode.SSPEM);
            edk.getController().setPowerMode(PowerPort.OUT_2,
                                             PowerMode.SSPEM);
            setInEmergencyMode(true,
                               true);
          } else if (controller.isRightPressed() && !em) {
            edk.setFunction(3,
                            false);
            edk.setFunction(4,
                            false);
            edk.setFunction(5,
                            false);
            edk.setFunction(6,
                            false);
            edk.setFunction(7,
                            false);
            edk.setFunction(8,
                            false);
            edk.setFunction(18,
                            false);
            edk.setFunction(19,
                            false);
            edk.control(Direction.FORWARD,
                        0);
            edk.setFunction(2,
                            false);
            setInEmergencyMode(true,
                               false);
          }
          break;
        default:
      }
    } catch (IOException ex) {
      processException(evt.getPropertyName(),
                       ex);
    }
  }

  private void setInEmergencyMode(boolean en,
                                  boolean stopEngine)
  {
    boolean wasInEm = isInEmergencyMode();
    synchronized (this) {
      emergencyMode = en;
      if (en) {
        if (stopEngine) {
          setStarted(false);
        }
      }
    }
    boolean isInEm = isInEmergencyMode();
    if (wasInEm != isInEm) {
      SwingUtilities.invokeLater(() -> propSupport.firePropertyChange(PROP_EMERGENCY,
                                                                      wasInEm,
                                                                      isInEm));
    }
    LOGGER.log(Level.FINER,
               () -> "Setting emergencymode to " + (en ? "on" : "off"));
  }

  @Override
  public boolean isInEmergencyMode()
  {
    boolean result;
    synchronized (this) {
      result = emergencyMode;
    }
    LOGGER.log(Level.FINEST,
               () -> MessageFormat.format("Emergency mode is {0}",
                                          new Object[]{result ? "on" : "off"}));
    return result;
  }

  private Point applyAxisLock(Point p)
  {
    if (axisLock) {
      if (Math.abs(p.y) > Math.abs(p.x)) {
        p.x = 0;
      } else {
        p.y = 0;
      }
    }
    return p;
  }

  protected void processLeft(PropertyChangeEvent evt) throws IOException
  {
    Point p = applyAxisLock(controller.getLeft());
    beam.setSpeed(-p.y);
    beamShift.setSpeed(-p.x);
    fireAxisMovement(EDKAxis.BEAM,
                     -p.y);
    fireAxisMovement(EDKAxis.BEAMLENGTH,
                     -p.x);
  }

  protected void processRight(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    Point p = applyAxisLock(controller.getRight());
    float fmx = controller.getRangeMax(PSController.Axis.RX);
    if (lastWinch != p.y) {
      winch.setSpeed(-p.y);
      fireAxisMovement(EDKAxis.WINCH,
                       -p.y);
      lastWinch = p.y;
    }
    if (lastRotate != p.x) {
      edk.setFunction(2,
                      true);
      Direction dir = p.x > 0 ? Direction.FORWARD : Direction.REVERSE;
      float scale = 1023 / fmx;
      float val = p.x * scale;
      edk.control(dir,
                  (int) Math.abs(val));
      lastRotate = p.x;
      fireAxisMovement(EDKAxis.ROTATE,
                       val);
    }
  }

  protected void processZAxis(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    int levelL = controller.getL2Level();
    int levelR = controller.getR2Level();
    if (levelL != 0 && levelR != 0) {
      levelL = 0;
      levelR = 0;
    }
    Direction dir = levelL != 0 ? Direction.FORWARD : Direction.REVERSE;
    edk.setFunction(2,
                    false);
    int level = Math.max(levelL,
                         levelR);
    edk.control(dir,
                level);
    if (dir == Direction.REVERSE) {
      level *= -1;
    }
    fireAxisMovement(EDKAxis.DRIVE,
                     level);
  }

  protected void processL1(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    boolean en = (Boolean) evt.getNewValue();
    edk.setFunction(15,
                    en);
  }

  protected void processR1(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    edk.setFunction(13,
                    controller.isR1Pressed());
  }

  protected void processDirection(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    PSDirection dir = (PSDirection) evt.getNewValue();
    if (dir.isNorth()) {
      edk.toggleFunction(16);
    }
    if (!dir.isWest() && !dir.isEast()) {
      edk.setFunction(17,
                      dir.isSouth());
    }
    if (dir.isWest() && !isInEmergencyMode()) {
      edk.setFunction(19,
                      false);
      edk.toggleFunction(18);
    }
    if (dir.isEast() && !isInEmergencyMode()) {
      edk.setFunction(18,
                      false);
      edk.toggleFunction(19);
    }
  }

  protected void processA(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(14);
    }
  }

  protected void processB(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(1);
    }
  }

  protected void processX(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(10);
    }
  }

  protected void processY(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(0);
    }
  }

  protected void processMode(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    if (controller.isModePressed()) {
      edk.toggleFunction(12);
    }
  }

  protected void processSelect(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    if (controller.isSelectPressed()) {
      edk.getController().setPowerMode(PowerPort.OUT_1,
                                       PowerMode.ON);
      edk.getController().setPowerMode(PowerPort.OUT_2,
                                       PowerMode.ON);
      setInEmergencyMode(false,
                         true);
    }
  }

  protected void processStart(PropertyChangeEvent evt) throws IOException
  {
    Locomotive edk = getLocomotive();
    if (edk == null) {
      return;
    }
    edk.toggleFunction(11);
  }

  protected void processException(String context,
                                  Throwable th)
  {
    Locomotive edk = getLocomotive();
    LOGGER.log(Level.SEVERE,
               th,
               () -> context + ":" + edk != null ? edk.toString() : "not connected");
    exceptionListener.forEach((l) -> l.onEDKException(this,
                                                      context,
                                                      th));
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    propSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propName,
                                        PropertyChangeListener listener)
  {
    propSupport.addPropertyChangeListener(propName,
                                          listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    propSupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propName,
                                           PropertyChangeListener listener)
  {
    propSupport.removePropertyChangeListener(propName,
                                             listener);
  }

  @Override
  public void addEDKExceptionListener(EDKExceptionListener l)
  {
    if (l != null) {
      exceptionListener.add(l);
    }
  }

  @Override
  public void removeEDKExceptionListener(EDKExceptionListener l)
  {
    exceptionListener.remove(l);
  }

  @Override
  public void addEDKAxisListener(EDKAxisListener l)
  {
    if (l != null) {
      axisListener.add(l);
    }
  }

  private void fireAxisMovement(EDKAxis axis,
                                float value)
  {
    SwingUtilities.invokeLater(() -> {
      if (!axisListener.isEmpty()) {
        EDKAxisEvent evt = new EDKAxisEvent(this,
                                            axis,
                                            value);
        axisListener.forEach((l) -> l.onAxisMove(evt));
      }
    });
  }

  @Override
  public void removeEDKAxisListener(EDKAxisListener l)
  {
    axisListener.remove(l);
  }

  @Override
  public Lookup getLookup()
  {
    return Lookup.EMPTY;
  }

}
