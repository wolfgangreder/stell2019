/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller.impl;

import at.or.reder.controller.PSController;
import at.or.reder.controller.PSControllerFactory;
import at.or.reder.controller.PSDirection;
import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerId;

final class PSControllerImpl implements PSController
{

  private final class ThresholdComp
  {

    private final Component comp;
    private final String propName;
    private final Axis axis;
    private float lastValue;
    private final BiFunction<Axis, Float, Object> valueConverter;

    public ThresholdComp(Component comp,
                         String propName,
                         Axis axis,
                         BiFunction<Axis, Float, Object> valueConverter)
    {
      this.axis = axis;
      this.valueConverter = valueConverter != null ? valueConverter : (a, f) -> f;
      lastValue = Float.NaN;
      this.comp = comp;
      this.propName = propName;
    }

    public PropertyChangeEvent check4Event()
    {
      PropertyChangeEvent evt = null;
      float pollData = comp.getPollData();
      float threshold = 1f / getRangeMax(axis);
      if (Float.isNaN(lastValue) || Math.abs(pollData - lastValue) > threshold) {
        Object oldValue = valueConverter.apply(axis,
                                               lastValue);
        lastValue = pollData;
        Object newValue = valueConverter.apply(axis,
                                               pollData);
        evt = new PropertyChangeEvent(PSControllerImpl.this,
                                      propName,
                                      oldValue,
                                      newValue);
      }
      return evt;
    }

    public float getLastValue()
    {
      return lastValue;
    }

    public boolean isSelected()
    {
      return lastValue > 0;
    }

  }
  private final Controller controller;
  private final Map<String, List<ThresholdComp>> components = new HashMap<>();
  private final Supplier<Point> leftSupplier;
  private final Supplier<Point> rightSupplier;
  private final Supplier<PSDirection> directionSupplier;
  private final ScheduledFuture<?> pollFuture;
  private final Object lock = new Object();
  private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
  private final PSControllerFactoryImpl factory;
  private final File deviceRoot;
  private final Map<Axis, Integer> rangeMax = new HashMap<>();
  private final Map<Axis, Float> deadRange = new HashMap<>();

  public PSControllerImpl(PSControllerFactoryImpl factory,
                          Controller controller,
                          ScheduledExecutorService pollExecutor)
  {
    for (Axis a : Axis.values()) {
      rangeMax.put(a,
                   1023);
      deadRange.put(a,
                    0.1f);
    }
    this.factory = factory;
    this.controller = controller;
    deviceRoot = getDeviceRoot();
    PSControllerFactoryImpl.LOGGER.log(Level.INFO,
                                       () -> "Connecting to Controller " + controller.getId());
    final ThresholdComp lx = createComp(Component.Identifier.Axis.X,
                                        PROP_LEFT,
                                        Axis.LX,
                                        (a, f) -> getLeft());
    final ThresholdComp ly = createComp(Component.Identifier.Axis.Y,
                                        PROP_LEFT,
                                        Axis.LY,
                                        (a, f) -> getLeft());
    leftSupplier = () -> computePoint(lx,
                                      ly);
    final ThresholdComp rx = createComp(Component.Identifier.Axis.RX,
                                        PROP_RIGHT,
                                        Axis.RX,
                                        (a, f) -> getRight());
    final ThresholdComp ry = createComp(Component.Identifier.Axis.RY,
                                        PROP_RIGHT,
                                        Axis.RY,
                                        (a, f) -> getRight());
    rightSupplier = () -> computePoint(rx,
                                       ry);
    createComp(Component.Identifier.Axis.Z,
               PROP_L2LEVEL,
               Axis.LZ,
               this::getZValue);
    createComp(Component.Identifier.Axis.RZ,
               PROP_R2LEVEL,
               Axis.RZ,
               this::getZValue);
    final ThresholdComp pov = createComp(Component.Identifier.Axis.POV,
                                         PROP_DIRECTION,
                                         null,
                                         (a, f) -> getDirection());
    directionSupplier = () -> computeDirection(pov);
    createComp(Component.Identifier.Button.A,
               PROP_APRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.B,
               PROP_BPRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.X,
               PROP_XPRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.Y,
               PROP_YPRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.RIGHT_THUMB,
               PROP_R1PRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.LEFT_THUMB,
               PROP_L1PRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.RIGHT_THUMB2,
               PROP_R2PRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.LEFT_THUMB2,
               PROP_L2PRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.RIGHT_THUMB3,
               PROP_RIGHTPRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.LEFT_THUMB3,
               PROP_LEFTPRESSED,
               null,
               this::floatToBoolean);

    createComp(Component.Identifier.Button.SELECT,
               PROP_SELECTPRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.START,
               PROP_STARTPRESSED,
               null,
               this::floatToBoolean);
    createComp(Component.Identifier.Button.MODE,
               PROP_MODEPRESSED,
               null,
               this::floatToBoolean);
    pollController();
    PSControllerFactoryImpl.LOGGER.log(Level.INFO,
                                       () -> "Starting polling");

    pollFuture = pollExecutor.scheduleAtFixedRate(this::pollController,
                                                  20,
                                                  20,
                                                  TimeUnit.MILLISECONDS);
  }

  @Override
  public PSControllerFactory getFactory()
  {
    return factory;
  }

  private boolean floatToBoolean(Axis a,
                                 float f)
  {
    return f != 0f;
  }

  @Override
  public String getId()
  {
    return controller.getId().toString();
  }

  private float normalizeValue(Axis axis,
                               float value)
  {
    float dr;
    synchronized (lock) {
      dr = deadRange.getOrDefault(axis,
                                  0.1f);
    }
    if (Math.abs(value) > dr) {
      if (value > 0) {
        value -= dr;
      } else {
        value += dr;
      }
      return value / (1 - dr);
    } else {
      return 0;
    }
  }

  private int getZValue(Axis axis,
                        float value)
  {
    int rm;
    synchronized (lock) {
      rm = rangeMax.get(axis);
    }
    value = normalizeValue(axis,
                           value);
    float result = ((value + 1f) / 2f) * rm;
    return (int) result;
  }

  private Point computePoint(ThresholdComp x,
                             ThresholdComp y)
  {
    int rmx;
    int rmy;
    float xpos;
    float ypos;
    synchronized (lock) {
      rmx = rangeMax.get(x.axis);
      rmy = rangeMax.get(y.axis);
      xpos = x.getLastValue();
      ypos = y.getLastValue();
    }
    xpos = normalizeValue(x.axis,
                          xpos) * rmx;
    ypos = normalizeValue(y.axis,
                          ypos) * rmy;
    return new Point((int) xpos,
                     (int) ypos);
  }

  private PSDirection computeDirection(ThresholdComp c)
  {
    int p;
    synchronized (lock) {
      p = (int) (c.getLastValue() * 8);
    }
    switch (p) {
      case 0:
        return PSDirection.UNKNOWN;
      case 1:
        return PSDirection.NORTH_WEST;
      case 2:
        return PSDirection.NORTH;
      case 3:
        return PSDirection.NORTH_EAST;
      case 4:
        return PSDirection.EAST;
      case 5:
        return PSDirection.SOUTH_EAST;
      case 6:
        return PSDirection.SOUTH;
      case 7:
        return PSDirection.SOUTH_WEST;
      case 8:
        return PSDirection.WEST;
      default:
        return PSDirection.UNKNOWN;
    }

  }

  private ThresholdComp createComp(Identifier id,
                                   String propName,
                                   Axis axis,
                                   BiFunction<Axis, Float, Object> valueConverter)
  {
    ThresholdComp result = null;
    Component comp = controller.getComponent(id);
    if (comp != null) {
      result = new ThresholdComp(comp,
                                 propName,
                                 axis,
                                 valueConverter);
      components.computeIfAbsent(propName,
                                 (p) -> new ArrayList<>()).add(result);

    } else {
      PSControllerFactoryImpl.LOGGER.log(Level.WARNING,
                                         () -> "Cannot find component with id " + id);
    }
    return result;
  }

  private void pollController()
  {
    List<PropertyChangeEvent> events = Collections.emptyList();;
    if (controller.poll()) {
      events = components.values().stream().flatMap(List::stream).map((c) -> c.check4Event()).filter((f) -> f != null).
              collect(Collectors.toList());
    } else {
      factory.pollFail(this);
    }
    events.forEach(propSupport::firePropertyChange);
  }

  @Override
  public boolean isOpened()
  {
    return !pollFuture.isDone();
  }

  @Override
  public void close() throws IOException
  {
    pollFuture.cancel(true);
  }

  @Override
  public int getRangeMax(Axis axis)
  {
    synchronized (lock) {
      return rangeMax.getOrDefault(axis,
                                   1023);
    }
  }

  @Override
  public void setRangeMax(Axis axis,
                          int rangeMax)
  {
    if (rangeMax <= 0) {
      throw new IllegalArgumentException("rangeMax is less than 1");
    }
    synchronized (lock) {
      this.rangeMax.put(axis,
                        rangeMax);
    }
  }

  @Override
  public float getDeadRange(Axis axis)
  {
    return deadRange.getOrDefault(axis,
                                  0.1f);
  }

  @Override
  public void setDeadRange(Axis axis,
                           float deadRange)
  {
    if (Math.abs(deadRange) > 1f) {
      throw new IllegalArgumentException("deadRange>1");
    }
    this.deadRange.put(axis,
                       Math.abs(deadRange));
  }

  private boolean isPressed(String prop)
  {
    List<ThresholdComp> list = components.get(prop);
    if (list != null && !list.isEmpty()) {
      return list.get(0).isSelected();
    }
    return false;
  }

  private int getZValue(Axis axis,
                        String prop)
  {
    synchronized (lock) {
      List<ThresholdComp> list = components.get(prop);
      if (list != null && !list.isEmpty()) {
        return getZValue(axis,
                         list.get(0).getLastValue());
      }
    }
    return 0;
  }

  @Override
  public boolean isL2Pressed()
  {
    return isPressed(PROP_L2PRESSED);
  }

  @Override
  public boolean isR2Pressed()
  {
    return isPressed(PROP_R2PRESSED);
  }

  @Override
  public int getL2Level()
  {
    return getZValue(Axis.LZ,
                     PROP_L2LEVEL);
  }

  @Override
  public int getR2Level()
  {
    return getZValue(Axis.RZ,
                     PROP_R2LEVEL);
  }

  @Override
  public boolean isL1Pressed()
  {
    return isPressed(PROP_L1PRESSED);
  }

  @Override
  public boolean isR1Pressed()
  {
    return isPressed(PROP_R1PRESSED);
  }

  @Override
  public PSDirection getDirection()
  {
    return directionSupplier.get();
  }

  @Override
  public boolean isXPressed()
  {
    return isPressed(PROP_XPRESSED);
  }

  @Override
  public boolean isYPressed()
  {
    return isPressed(PROP_YPRESSED);
  }

  @Override
  public boolean isAPressed()
  {
    return isPressed(PROP_APRESSED);
  }

  @Override
  public boolean isBPressed()
  {
    return isPressed(PROP_BPRESSED);
  }

  @Override
  public boolean isSelectPressed()
  {
    return isPressed(PROP_SELECTPRESSED);
  }

  @Override
  public boolean isStartPressed()
  {
    return isPressed(PROP_STARTPRESSED);
  }

  @Override
  public boolean isModePressed()
  {
    return isPressed(PROP_MODEPRESSED);
  }

  private File getDeviceRoot()
  {
    File path = new File("/sys/bus/hid/devices/");
    ControllerId id = controller.getId();
    String pattern = (id.getBusType() + ":" + id.getVendor() + ":" + id.getDevice() + ".").toLowerCase();
    File device = null;
    File[] devices = path.listFiles((File file, String string) -> string.toLowerCase().startsWith(pattern));
    if (devices != null && devices.length > 0) {
      device = devices[0];
    }
    return device != null ? device.getAbsoluteFile() : null;
  }

  private List<File> getColorFiles()
  {
    if (deviceRoot != null) {
      return Arrays.asList(new File(deviceRoot,
                                    "/leds/" + deviceRoot.getName() + ":red/brightness"),
                           new File(deviceRoot,
                                    "/leds/" + deviceRoot.getName() + ":green/brightness"),
                           new File(deviceRoot,
                                    "/leds/" + deviceRoot.getName() + ":blue/brightness"),
                           new File(deviceRoot,
                                    "/leds/" + deviceRoot.getName() + ":global/brightness"));
    }
    return Collections.emptyList();
  }

  private int readComponent(File colorFile) throws IOException
  {
    try (LineNumberReader reader = new LineNumberReader(new FileReader(colorFile))) {
      String line = reader.readLine();
      if (line != null) {
        try {
          return Integer.parseUnsignedInt(line);
        } catch (Throwable th) {
        }
      }
    }
    return 0;
  }

  private void writeComponent(File colorFile,
                              int comp) throws IOException
  {
    String str = Integer.toString(comp);
    try (Writer writer = new FileWriter(colorFile,
                                        false)) {
      writer.write(str);
    }
  }

  @Override
  public boolean isLedOn() throws IOException
  {
    List<File> compFiles = getColorFiles();
    if (compFiles.size() == 4) {
      return readComponent(compFiles.get(3)) > 0;
    }
    return false;
  }

  @Override
  public void setLedOn(boolean ledOn) throws IOException
  {
    List<File> compFiles = getColorFiles();
    if (compFiles.size() == 4) {
      writeComponent(compFiles.get(3),
                     ledOn ? 1 : 0);
    }
  }

  @Override
  public Color getColor() throws IOException
  {
    List<File> colorFiles = getColorFiles();
    if (colorFiles.size() == 4) {
      int red = readComponent(colorFiles.get(0));
      int green = readComponent(colorFiles.get(1));
      int blue = readComponent(colorFiles.get(2));
      return new Color(red,
                       green,
                       blue);
    }
    return null;
  }

  @Override
  public void setColor(Color color) throws IOException
  {
    List<File> colorFiles = getColorFiles();
    if (colorFiles.size() == 4) {
      writeComponent(colorFiles.get(0),
                     color.getRed());
      writeComponent(colorFiles.get(1),
                     color.getGreen());
      writeComponent(colorFiles.get(2),
                     color.getBlue());
    }
  }

  @Override
  public boolean isLeftPressed()
  {
    return isPressed(PROP_LEFTPRESSED);
  }

  @Override
  public Point getLeft()
  {
    return leftSupplier.get();
  }

  @Override
  public boolean isRightPressed()
  {
    return isPressed(PROP_RIGHTPRESSED);
  }

  @Override
  public Point getRight()
  {
    return rightSupplier.get();
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

}
