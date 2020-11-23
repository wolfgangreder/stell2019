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
import at.or.reder.dcc.PowerMode;
import at.or.reder.dcc.PowerPort;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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

  public EDK750(Locomotive edk,
                PSController controller)
  {
    this.edk = edk;
    winch = EDKAxisController.getWinch(edk);
    beam = EDKAxisController.getBeam(edk);
    beamShift = EDKAxisController.getBeamShift(edk);
    setController(controller);
  }

  public boolean isAxisLock()
  {
    return axisLock;
  }

  public void setAxisLock(boolean axisLock)
  {
    this.axisLock = axisLock;
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

  private void connect()
  {
    if (controller != null) {
      controller.addPropertyChangeListener(controllerListener);
      controllerId = controller.getId();
      controller.getFactory().addPSControllerEventListener(controllerEventListener);
      controller.setRangeMax(PSController.Axis.RX,
                             64);
      for (PSController.Axis a : PSController.Axis.values()) {
        controller.setDeadRange(a,
                                0.10f);
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
    try {
      switch (evt.getPropertyName()) {
        case PSController.PROP_LEFT:
          processLeft(evt);
          break;
        case PSController.PROP_RIGHT:
          processRight(evt);
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
          processZAxis(evt);
          break;
        case PSController.PROP_MODEPRESSED:
          processMode(evt);
          break;
        case PSController.PROP_R1PRESSED:
          processR1(evt);
          break;
        case PSController.PROP_R2LEVEL:
          processZAxis(evt);
          break;
        case PSController.PROP_SELECTPRESSED:
          processSelect(evt);
          break;
        case PSController.PROP_STARTPRESSED:
          processStart(evt);
          break;
        default:
      }
    } catch (IOException ex) {
      processException(evt.getPropertyName(),
                       ex);
    }
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
    if (dir.isSouth()) {
      edk.toggleFunction(17);
    }
    if (dir.isWest()) {
      edk.setFunction(19,
                      false);
      edk.toggleFunction(18);
    }
    if (dir.isEast()) {
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
    edk.getController().setPowerMode(PowerPort.OUT_1,
                                     PowerMode.ON);
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
