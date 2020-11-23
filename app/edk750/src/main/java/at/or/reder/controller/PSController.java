/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller;

import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 *
 * @author Wolfgang Reder
 */
public interface PSController extends AutoCloseable
{

  public static final String PROP_L2PRESSED = "l2Pressed";
  public static final String PROP_R2PRESSED = "r2Pressed";
  public static final String PROP_L2LEVEL = "l2Level";
  public static final String PROP_R2LEVEL = "r2Level";
  public static final String PROP_L1PRESSED = "l1Pressed";
  public static final String PROP_R1PRESSED = "r1Pressed";
  public static final String PROP_DIRECTION = "direction";
  public static final String PROP_XPRESSED = "xPressed";
  public static final String PROP_YPRESSED = "yPressed";
  public static final String PROP_APRESSED = "aPressed";
  public static final String PROP_BPRESSED = "bPressed";
  public static final String PROP_SELECTPRESSED = "selectPressed";
  public static final String PROP_STARTPRESSED = "startPressed";
  public static final String PROP_MODEPRESSED = "modePressed";
  public static final String PROP_LEFTPRESSED = "leftPressed";
  public static final String PROP_RIGHTPRESSED = "rightPressed";
  public static final String PROP_RIGHT = "right";
  public static final String PROP_LEFT = "left";

  public static enum Axis
  {
    RX,
    RY,
    RZ,
    LX,
    LY,
    LZ;
  }

  public PSControllerFactory getFactory();

  public boolean isOpened();

  @Override
  public void close() throws IOException;

  public String getId();

  public int getRangeMax(Axis axis);

  public void setRangeMax(Axis axis,
                          int rangeMax);

  public float getDeadRange(Axis axis);

  public void setDeadRange(Axis axis,
                           float deadRange);

  public boolean isL2Pressed();

  public boolean isR2Pressed();

  public int getL2Level();

  public int getR2Level();

  public boolean isL1Pressed();

  public boolean isR1Pressed();

  public PSDirection getDirection();

  public boolean isXPressed();

  public boolean isYPressed();

  public boolean isAPressed();

  public boolean isBPressed();

  public boolean isSelectPressed();

  public boolean isStartPressed();

  public boolean isModePressed();

  public boolean isLedOn() throws IOException;

  public void setLedOn(boolean ledOn) throws IOException;

  public Color getColor() throws IOException;

  public void setColor(Color color) throws IOException;

  public boolean isLeftPressed();

  public Point getLeft();

  public boolean isRightPressed();

  public Point getRight();

  public void addPropertyChangeListener(PropertyChangeListener listener);

  public void addPropertyChangeListener(String propName,
                                        PropertyChangeListener listener);

  public void removePropertyChangeListener(PropertyChangeListener listener);

  public void removePropertyChangeListener(String propName,
                                           PropertyChangeListener listener);

}
