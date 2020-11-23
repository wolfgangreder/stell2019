/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import at.or.reder.controller.PSController;
import at.or.reder.controller.PSControllerEvent;
import at.or.reder.controller.PSControllerEventListener;
import at.or.reder.controller.PSControllerEventType;
import at.or.reder.controller.PSDirection;
import at.or.reder.dcc.Direction;
import at.or.reder.dcc.Locomotive;
import at.or.reder.dcc.LocomotiveFuncEvent;
import at.or.reder.dcc.PowerMode;
import at.or.reder.dcc.PowerPort;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EDK750
{

  public static final Logger LOGGER = Logger.getLogger("at.or.reder.edk750");
  protected final Locomotive edk;
  private final PropertyChangeListener controllerListener = this::onControllerPropertyChange;
  private final PSControllerEventListener controllerEventListener = this::onControllerEvent;
  protected final EDKAxisController winch;
  protected final EDKAxisController beam;
  protected final EDKAxisController beamShift;

  private PSController controller;
  private String controllerId;
  private int lastWinch;
  private int lastRotate;
  private boolean axisLock = true;
  private boolean strictMode = true;
  private long emergencyMode = -1;
  private long f1On = -1;
  private final Map<String, String> config;

  public EDK750(Locomotive edk,
                PSController controller,
                Map<String, String> config)
  {
    this.edk = edk;
    edk.addLocomotiveFuncEventListener(this::onFunc);
    winch = EDKAxisController.getWinch(edk);
    beam = EDKAxisController.getBeam(edk);
    beamShift = EDKAxisController.getBeamShift(edk);
    if (config != null) {
      this.config = Collections.unmodifiableMap(new HashMap<>(config));
    } else {
      this.config = Collections.emptyMap();
    }
    setController(controller);
    setStarted(edk.isFunction(1));
  }

  public boolean isAxisLock()
  {
    return axisLock;
  }

  public void setAxisLock(boolean axisLock)
  {
    this.axisLock = axisLock;
  }

  public boolean isStrictMode()
  {
    return strictMode;
  }

  public void setStrictMode(boolean strictMode)
  {
    this.strictMode = strictMode;
  }

  public final boolean isStarted()
  {
    long f1;
    synchronized (this) {
      f1 = f1On;
    }
    boolean result = !isStrictMode() || (edk.isFunction(1) && f1 != -1 && (System.currentTimeMillis() - f1) > 2000);
    LOGGER.log(Level.FINEST,
               () -> "Engine is " + (result ? "started"
                                     : "not running"));
    return result;
  }

  public final void setStarted(boolean s)
  {
    if (isStarted() != s) {
      try {
        edk.setFunction(1,
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
      long f1;
      if (evt.getFuncValue() != 0) {
        f1 = System.currentTimeMillis();
      } else {
        f1 = -1;
      }
      synchronized (this) {
        f1On = f1;
      }
    }
  }

  public final PSController getController()
  {
    return controller;
  }

  public final String getControllerId()
  {
    return controllerId;
  }

  public final void setControllerId(String controllerId)
  {
    this.controllerId = controllerId;
  }

  public final void setController(PSController controller)
  {
    if (controller != this.controller) {
      disconnect();
      this.controller = controller;
      connect();
    }
  }

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
      setController(null);
    }
    if (controller == null && evt.getType() == PSControllerEventType.OPEN && evt.getController().getId().equals(controllerId)) {
      setController(evt.getController());
    }
  }

  private void onControllerPropertyChange(PropertyChangeEvent evt)
  {
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
    if (en) {
      emergencyMode = System.currentTimeMillis();
      if (stopEngine) {
        setStarted(false);
      }
    } else {
      emergencyMode = -1;
    }
    LOGGER.log(Level.FINER,
               () -> "Setting emergencymode to " + (en ? "on" : "off"));
  }

  private boolean isInEmergencyMode()
  {
    long delta = (System.currentTimeMillis() - emergencyMode);
    boolean result = emergencyMode != -1 && delta < 5000;
    LOGGER.log(Level.FINEST,
               () -> MessageFormat.format("Emergency mode is {0} ({1} ms))",
                                          new Object[]{result ? "on" : "off",
                                                       result ? delta : -1}));
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
  }

  protected void processRight(PropertyChangeEvent evt) throws IOException
  {
    Point p = applyAxisLock(controller.getRight());
    float fmx = controller.getRangeMax(PSController.Axis.RX);
    if (lastWinch != p.y) {
      winch.setSpeed(-p.y);
      lastWinch = p.y;
    }
    if (lastRotate != p.x) {
      edk.setFunction(2,
                      true);
      Direction dir = p.x > 0 ? Direction.FORWARD : Direction.REVERSE;
      float scale = 1024 / fmx;
      float val = p.x * scale;
      edk.control(dir,
                  (int) Math.abs(val));
      lastRotate = p.x;
    }
  }

  protected void processZAxis(PropertyChangeEvent evt) throws IOException
  {
    int levelL = controller.getL2Level();
    int levelR = controller.getR2Level();
    if (levelL != 0 && levelR != 0) {
      levelL = 0;
      levelR = 0;
    }
    Direction dir = levelL != 0 ? Direction.FORWARD : Direction.REVERSE;
    edk.setFunction(2,
                    false);
    edk.control(dir,
                Math.max(levelL,
                         levelR));
  }

  protected void processL1(PropertyChangeEvent evt) throws IOException
  {
    boolean en = (Boolean) evt.getNewValue();
    edk.setFunction(15,
                    en);
  }

  protected void processR1(PropertyChangeEvent evt) throws IOException
  {
    edk.setFunction(13,
                    controller.isR1Pressed());
  }

  protected void processDirection(PropertyChangeEvent evt) throws IOException
  {
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
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(14);
    }
  }

  protected void processB(PropertyChangeEvent evt) throws IOException
  {
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(1);
    }
  }

  protected void processX(PropertyChangeEvent evt) throws IOException
  {
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(10);
    }
  }

  protected void processY(PropertyChangeEvent evt) throws IOException
  {
    boolean en = (Boolean) evt.getNewValue();
    if (en) {
      edk.toggleFunction(0);
    }
  }

  protected void processMode(PropertyChangeEvent evt) throws IOException
  {
    edk.setFunction(12,
                    controller.isModePressed());
  }

  protected void processSelect(PropertyChangeEvent evt) throws IOException
  {
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
    edk.toggleFunction(11);
  }

  protected void processException(String context,
                                  Throwable th)
  {
    LOGGER.log(Level.SEVERE,
               th,
               () -> context + ":" + edk.toString());
  }

}
