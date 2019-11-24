/*
 * Copyright 2019 Wolfgang Reder.
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
package at.or.reder.rpi;

import at.or.reder.rpi.model.SymbolRotation;
import at.or.reder.rpi.model.SymbolType;
import at.or.reder.rpi.model.TrackElement;
import eu.hansolo.steelseries.tools.BlinkTimer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;

public final class SymbolPanel extends JPanel
{

  static {
    try (InputStream is = SwitchPanel.class.getResourceAsStream("/font/DINPro-Regular.ttf")) {
      Font font = Font.createFont(Font.TRUETYPE_FONT,
                                  is);
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
    } catch (IOException | FontFormatException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  public static enum Neighbour
  {
    NORTH, EAST, SOUTH, WEST;
  }
  private SymbolType symbolType = SymbolType.EMPTY;
  private final Set<ActionListener> actionListener = new CopyOnWriteArraySet<>();
  private TrackElement element;
  private SymbolRotation rotation = SymbolRotation.NONE;
  private final Set<Integer> endPoints = new HashSet<>();
  private final Map<SymbolPanel, Set<Integer>> cornerPoints = new HashMap<>();
  private BufferedImage background;
  private final Map<Neighbour, SymbolPanel> neighbours = new HashMap<>();
  private final ChangeListener elementChangeListener = this::onElementChanged;

  public SymbolPanel()
  {
    initComponents();
  }

  public BlinkTimer getBlinkTimer()
  {
    return ledPanel1.getBlinkTimer();
  }

  public void setBlinkTimer(BlinkTimer t)
  {
    ledPanel1.setBlinkTimer(t);
    ledPanel2.setBlinkTimer(t);
    ledPanel3.setBlinkTimer(t);
    ledPanel4.setBlinkTimer(t);
    ledPanel5.setBlinkTimer(t);
    ledPanel6.setBlinkTimer(t);
    ledPanel7.setBlinkTimer(t);
    ledPanel8.setBlinkTimer(t);
    ledPanel9.setBlinkTimer(t);
  }

  protected void removeCorner(SymbolPanel panel)
  {
    if (panel != null) {
      cornerPoints.remove(panel);
    }
  }

  public void setNeighbour(Neighbour n,
                           SymbolPanel p)
  {
    if (p != this) {
      SymbolPanel old;
      if (p != null) {
        old = neighbours.put(n,
                             p);
      } else {
        old = neighbours.remove(n);
      }
      if (old != null) {
        old.removeCorner(this);
      }
      if (old != p) {
        fixCornerPoints();
        repaint();
      }
    }
  }

  public SymbolPanel getNorthNeighbour()
  {
    return neighbours.get(Neighbour.NORTH);
  }

  public void setNorthNeighbour(SymbolPanel p)
  {
    setNeighbour(Neighbour.NORTH,
                 p);
  }

  public SymbolPanel getEastNeighbour()
  {
    return neighbours.get(Neighbour.EAST);
  }

  public void setEastNeighbour(SymbolPanel p)
  {
    setNeighbour(Neighbour.EAST,
                 p);
  }

  public SymbolPanel getSouthNeighbour()
  {
    return neighbours.get(Neighbour.SOUTH);
  }

  public void setSouthNeighbour(SymbolPanel p)
  {
    setNeighbour(Neighbour.SOUTH,
                 p);
  }

  public SymbolPanel getWestNeighbour()
  {
    return neighbours.get(Neighbour.WEST);
  }

  public void setWestNeighbour(SymbolPanel p)
  {
    setNeighbour(Neighbour.WEST,
                 p);
  }

  public void clearCorner()
  {
    if (!cornerPoints.isEmpty()) {
      cornerPoints.clear();
      repaint();
    }
  }

  void addCorner(SymbolPanel panel,
                 int corner)
  {
    if (cornerPoints.computeIfAbsent(panel,
                                     (p) -> new HashSet<>()).add(corner)) {
      repaint();
    }
  }

  private SymbolPanel findNeighbour(Neighbour n)
  {
    Rectangle bounds = getBounds();
    Container parent = getParent();
    if (parent == null) {
      return null;
    }
    Component comp = null;
    switch (n) {
      case EAST:
        comp = parent.getComponentAt(bounds.x + bounds.width + 1,
                                     bounds.y + 1);
        break;
      case NORTH:
        comp = parent.getComponentAt(bounds.x + 1,
                                     bounds.y - 1);
        break;
      case SOUTH:
        comp = parent.getComponentAt(bounds.x + 1,
                                     bounds.y + bounds.height + 1);
        break;
      case WEST:
        comp = parent.getComponentAt(bounds.x - 1,
                                     bounds.y + 1);
        break;
    }
    if (comp instanceof SymbolPanel && comp != this) {
      return (SymbolPanel) comp;
    }
    return null;
  }

  public void fixCornerPoints()
  {
    for (Integer ep : endPoints) {
      SymbolPanel n;
      switch (ep) {
        case 1:
          n = neighbours.computeIfAbsent(Neighbour.WEST,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        3);
          }
          n = neighbours.computeIfAbsent(Neighbour.NORTH,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        6);
          }
          break;
        case 3:
          n = neighbours.computeIfAbsent(Neighbour.NORTH,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        8);
          }
          n = neighbours.computeIfAbsent(Neighbour.EAST,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        1);
          }
          break;
        case 6:
          n = neighbours.computeIfAbsent(Neighbour.SOUTH,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        1);
          }
          n = neighbours.computeIfAbsent(Neighbour.WEST,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        8);
          }
          break;
        case 8:
          n = neighbours.computeIfAbsent(Neighbour.SOUTH,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        3);
          }
          n = neighbours.computeIfAbsent(Neighbour.EAST,
                                         this::findNeighbour);
          if (n != null) {
            n.addCorner(this,
                        6);
          }
          break;
      }
    }
  }

  public void addActionListener(ActionListener l)
  {
    if (l != null) {
      actionListener.add(l);
    }
  }

  public void removeActionListener(ActionListener l)
  {
    actionListener.remove(l);
  }

  public TrackElement getElement()
  {
    return element;
  }

  private void onElementChanged(ChangeEvent evt)
  {
    if (evt.getSource() == this.element) {
      Map<Integer, LedPanel.LedState> states = this.element.getCurrentLedStates();
      for (int i = 1; i <= 9; i++) {
        getLedPanel(i).setLedState(LedPanel.LedState.OFF);
      }
      for (Map.Entry<Integer, LedPanel.LedState> e : states.entrySet()) {
        LedPanel pnl = getLedPanel(e.getKey());
        pnl.setLedState(e.getValue());
      }
    }
  }

  public void setElement(TrackElement element)
  {
    if (this.element != null) {
      this.element.removeChangeListener(elementChangeListener);
    }
    this.element = element;
    if (element != null) {
      this.element.addChangeListener(elementChangeListener);
      setSymbolType(element.getSymbolType());
    } else {
      setSymbolType(SymbolType.EMPTY);
    }
  }

  public SymbolRotation getRotation()
  {
    return rotation;
  }

  public void setRotation(SymbolRotation rotation)
  {
    if (this.rotation != rotation) {
      this.rotation = rotation;
      updateLeds();
    }
  }

  public void setLedState(LedPanel.LedState state)
  {
    ledPanel1.setLedState(state);
    ledPanel2.setLedState(state);
    ledPanel3.setLedState(state);
    ledPanel4.setLedState(state);
    ledPanel5.setLedState(state);
    ledPanel6.setLedState(state);
    ledPanel7.setLedState(state);
    ledPanel8.setLedState(state);
    ledPanel9.setLedState(state);
  }

  public SymbolType getSymbolType()
  {
    return symbolType;
  }
//  1  2  3
//  4  9  5
//  6  7  8

  private void setLedsNone()
  {
    ledPanel1.setState(symbolType.getVisibleLeds().get(1),
                       null);
    ledPanel2.setState(symbolType.getVisibleLeds().get(2),
                       null);
    ledPanel3.setState(symbolType.getVisibleLeds().get(3),
                       null);
    ledPanel4.setState(symbolType.getVisibleLeds().get(4),
                       null);
    ledPanel5.setState(symbolType.getVisibleLeds().get(5),
                       null);
    ledPanel6.setState(symbolType.getVisibleLeds().get(6),
                       null);
    ledPanel7.setState(symbolType.getVisibleLeds().get(7),
                       null);
    ledPanel8.setState(symbolType.getVisibleLeds().get(8),
                       null);
    ledPanel9.setState(symbolType.getVisibleLeds().get(9),
                       symbolType.getButtonColor());
    endPoints.clear();
    endPoints.addAll(symbolType.getLines());
  }
//  1  2  3
//  4  9  5
//  6  7  8

//  6  4  1
//  7  9  2
//  8  5  3
  private void setLedsCW90()
  {
    ledPanel1.setState(symbolType.getVisibleLeds().get(6),
                       null);
    ledPanel2.setState(symbolType.getVisibleLeds().get(4),
                       null);
    ledPanel3.setState(symbolType.getVisibleLeds().get(1),
                       null);
    ledPanel4.setState(symbolType.getVisibleLeds().get(7),
                       null);
    ledPanel5.setState(symbolType.getVisibleLeds().get(2),
                       null);
    ledPanel6.setState(symbolType.getVisibleLeds().get(8),
                       null);
    ledPanel7.setState(symbolType.getVisibleLeds().get(5),
                       null);
    ledPanel8.setState(symbolType.getVisibleLeds().get(3),
                       null);
    ledPanel9.setState(symbolType.getVisibleLeds().get(9),
                       symbolType.getButtonColor());
    endPoints.clear();
    for (Integer l : symbolType.getLines()) {
      switch (l) {
        case 6:
          endPoints.add(1);
          break;
        case 4:
          endPoints.add(2);
          break;
        case 1:
          endPoints.add(3);
          break;
        case 7:
          endPoints.add(4);
          break;
        case 2:
          endPoints.add(5);
          break;
        case 8:
          endPoints.add(6);
          break;
        case 5:
          endPoints.add(7);
          break;
        case 3:
          endPoints.add(8);
          break;
      }
    }
  }
//  1  2  3
//  4  9  5
//  6  7  8

//  8  7  6
//  5  9  4
//  3  2  1
  private void setLedsCW180()
  {
    ledPanel1.setState(symbolType.getVisibleLeds().get(8),
                       null);
    ledPanel2.setState(symbolType.getVisibleLeds().get(7),
                       null);
    ledPanel3.setState(symbolType.getVisibleLeds().get(6),
                       null);
    ledPanel4.setState(symbolType.getVisibleLeds().get(5),
                       null);
    ledPanel5.setState(symbolType.getVisibleLeds().get(4),
                       null);
    ledPanel6.setState(symbolType.getVisibleLeds().get(3),
                       null);
    ledPanel7.setState(symbolType.getVisibleLeds().get(2),
                       null);
    ledPanel8.setState(symbolType.getVisibleLeds().get(1),
                       null);
    ledPanel9.setState(symbolType.getVisibleLeds().get(9),
                       symbolType.getButtonColor());
    endPoints.clear();
    for (Integer l : symbolType.getLines()) {
      switch (l) {
        case 8:
          endPoints.add(1);
          break;
        case 7:
          endPoints.add(2);
          break;
        case 6:
          endPoints.add(3);
          break;
        case 5:
          endPoints.add(4);
          break;
        case 4:
          endPoints.add(5);
          break;
        case 3:
          endPoints.add(6);
          break;
        case 2:
          endPoints.add(7);
          break;
        case 1:
          endPoints.add(8);
          break;
      }
    }
  }

//  1  2  3
//  4  9  5
//  6  7  8
//  3  5  8
//  2  9  7
//  1  4  6
  private void setLedsCW270()
  {
    ledPanel1.setState(symbolType.getVisibleLeds().get(3),
                       null);
    ledPanel2.setState(symbolType.getVisibleLeds().get(5),
                       null);
    ledPanel3.setState(symbolType.getVisibleLeds().get(8),
                       null);
    ledPanel4.setState(symbolType.getVisibleLeds().get(2),
                       null);
    ledPanel5.setState(symbolType.getVisibleLeds().get(7),
                       null);
    ledPanel6.setState(symbolType.getVisibleLeds().get(1),
                       null);
    ledPanel7.setState(symbolType.getVisibleLeds().get(4),
                       null);
    ledPanel8.setState(symbolType.getVisibleLeds().get(6),
                       null);
    ledPanel9.setState(symbolType.getVisibleLeds().get(9),
                       symbolType.getButtonColor());
    endPoints.clear();
    for (Integer l : symbolType.getLines()) {
      switch (l) {
        case 3:
          endPoints.add(1);
          break;
        case 5:
          endPoints.add(2);
          break;
        case 8:
          endPoints.add(3);
          break;
        case 2:
          endPoints.add(4);
          break;
        case 7:
          endPoints.add(5);
          break;
        case 1:
          endPoints.add(6);
          break;
        case 4:
          endPoints.add(7);
          break;
        case 6:
          endPoints.add(8);
          break;
      }
    }
  }

  private void updateLeds()
  {
    switch (rotation) {
      case NONE:
        setLedsNone();
        break;
      case CW90:
      case CCW270:
        setLedsCW90();
        break;
      case CW180:
      case CCW180:
        setLedsCW180();
        break;
      case CW270:
      case CCW90:
        setLedsCW270();
        break;
    }
    background = null;
  }

  public void setSymbolType(SymbolType symbolType)
  {
    if (this.symbolType != symbolType && symbolType != null) {
      this.symbolType = symbolType;
      updateLeds();
    }
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    if (background == null) {
      buildBackground();
    }
    if (background != null) {
      g.drawImage(background,
                  0,
                  0,
                  null);
    }
  }

  @Override
  public void setBounds(int x,
                        int y,
                        int width,
                        int height)
  {
    super.setBounds(x,
                    y,
                    width,
                    height);
    background = null;
  }

  private Polygon buildPolygon(double... points)
  {
    int numPoints = points.length / 2;
    int[] x = new int[numPoints];
    int[] y = new int[numPoints];
    for (int i = 0; i < numPoints; ++i) {
      x[i] = (int) points[2 * i];
      y[i] = (int) points[2 * i + 1];
    }
    return new Polygon(x,
                       y,
                       numPoints);
  }

  private Stroke createStroke(double w,
                              boolean round)
  {
    if (round) {
      return new BasicStroke((float) w,
                             BasicStroke.CAP_ROUND,
                             BasicStroke.JOIN_ROUND);
    } else {
      return new BasicStroke((float) w,
                             BasicStroke.CAP_SQUARE,
                             BasicStroke.JOIN_MITER);
    }
  }

  private void drawCorner(Graphics2D g,
                          Dimension dim,
                          int corner)
  {
    Polygon p = null;
    switch (corner) {
      case 1:
        p = buildPolygon(-dim.getWidth() - 1,
                         dim.getHeight() + 1,
                         +1 + dim.getWidth(),
                         -1 - dim.getHeight());
        break;
      case 3:
        p = buildPolygon(0,
                         -dim.getHeight(),
                         2 * dim.getWidth(),
                         dim.getHeight());
        break;
      case 6:
        p = buildPolygon(-2 * dim.getWidth(),
                         -dim.getHeight(),
                         dim.getWidth(),
                         dim.getHeight() * 2);
        break;
      case 8:
        p = buildPolygon(0,
                         dim.getHeight() * 2,
                         dim.getWidth() * 2,
                         0);
        break;
    }
    if (p != null) {
      g.drawPolygon(p);
    }
  }

  private void drawEndPoint(Graphics2D g,
                            Dimension dim,
                            int endPoint)
  {
    double cy = dim.getHeight() / 2 - 1;
    double cx = dim.getWidth() / 2;
    Polygon p = null;
    switch (endPoint) {
      case 1:
        p = buildPolygon(-1,
                         -1,
                         cx,
                         cy);
        break;
      case 2:
        p = buildPolygon(cx,
                         -1,
                         cx,
                         cy);
        break;
      case 3:
        p = buildPolygon(dim.getWidth() + 1,
                         -1,
                         cx,
                         cy);
        break;
      case 4:
        p = buildPolygon(-1,
                         cy,
                         cx,
                         cy);
        break;
      case 5:
        p = buildPolygon(dim.getWidth() + 1,
                         cy,
                         cx,
                         cy);
        break;
      case 6:
        p = buildPolygon(-1,
                         dim.getHeight() + 1,
                         cx,
                         cy);
        break;
      case 7:
        p = buildPolygon(cx,
                         dim.getHeight() + 1,
                         cx,
                         cy);
        break;
      case 8:
        p = buildPolygon(dim.getWidth() + 1,
                         dim.getHeight() + 1,
                         cx,
                         cy);
        break;
    }
    if (p != null) {
      g.drawPolygon(p);
    }
  }

  private void drawSignalS2(Graphics2D g,
                            Dimension dim)
  {
    switch (rotation) {
      case NONE:
      case CCW180:
      case CW180:
        g.scale(-1,
                1);
        g.translate(-dim.getWidth(),
                    0);
        break;
      case CCW270:
      case CCW90:
      case CW270:
      case CW90:
        g.scale(1,
                -1);
        g.translate(0,
                    -dim.getHeight());
    }
    drawSignalS1(g,
                 dim);
  }

  private void drawSignalS1(Graphics2D g,
                            Dimension dim)
  {
    Rectangle redLed;
    Rectangle greenLed;
    Arc2D arc;
    Rectangle2D r1;
    Rectangle2D r2;
    Rectangle2D r3;
    double rot = 0;
    redLed = getLedRect(3);
    greenLed = getLedRect(2);
    double m = Math.min(greenLed.getWidth(),
                        greenLed.getHeight()) * 0.8;
    arc = new Arc2D.Double(redLed.getCenterX() - m / 2 + m / 6,
                           redLed.getCenterY() - m / 2,
                           m,
                           m,
                           270,
                           180,
                           Arc2D.CHORD);
    r1 = new Rectangle2D.Double(greenLed.getX(),
                                greenLed.getCenterY() - m / 2,
                                redLed.getCenterX() - greenLed.getX() + m / 6,
                                m);
    r2 = new Rectangle2D.Double(greenLed.getX() - greenLed.getWidth() / 2,
                                greenLed.getCenterY() - m / 8,
                                greenLed.getWidth() / 2,
                                m / 4);
    r3 = new Rectangle2D.Double(r2.getX(),
                                r1.getY(),
                                m / 4,
                                m);
    switch (rotation) {
      case NONE:
        rot = 0;
        break;
      case CW90:
      case CCW270:
        rot = Math.PI / 2;
        break;
      case CCW180:
      case CW180:
        rot = Math.PI;
        break;
      case CCW90:
      case CW270:
        rot = 3 * Math.PI / 2;
        break;
    }
    g.rotate(rot,
             dim.getWidth() / 2,
             dim.getHeight() / 2);
    g.fill(arc);
    g.fill(r1);
    g.fill(r2);
    g.fill(r3);
  }

  private LedPanel getLedPanel(int iLed)
  {
    LedPanel pnl = null;
    switch (iLed) {
      case 1:
        pnl = ledPanel1;
        break;
      case 2:
        pnl = ledPanel2;
        break;
      case 3:
        pnl = ledPanel3;
        break;
      case 4:
        pnl = ledPanel4;
        break;
      case 5:
        pnl = ledPanel5;
        break;
      case 6:
        pnl = ledPanel6;
        break;
      case 7:
        pnl = ledPanel7;
        break;
      case 8:
        pnl = ledPanel8;
        break;
      case 9:
        pnl = ledPanel9;
        break;
    }
    return pnl;
  }

  private Rectangle getLedRect(int iLed)
  {
    LedPanel pnl = getLedPanel(iLed);
    if (pnl != null) {
      Rectangle b = pnl.getBounds();
      return b;
    }
    return null;
  }

  private void buildBackground()
  {
    Dimension dim = getSize();
    background = new BufferedImage(dim.width,
                                   dim.height,
                                   BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = background.createGraphics();
    double w = (dim.getHeight() / 2 - 1) / 3;
    try {
      if (isEnabled()) {
        g.setColor(getForeground());
      } else {
        g.setColor(Color.GRAY);
      }
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
//      g.drawRect(0,
//                 0,
//                 dim.width - 1,
//                 dim.height - 1);
      if (!symbolType.isSpecialPainting()) {
        Graphics2D gs = null;
        try {
          if (symbolType == SymbolType.S1) {
            gs = (Graphics2D) g.create();
            drawSignalS1(gs,
                         dim);
          } else if (symbolType == SymbolType.S2) {
            gs = (Graphics2D) g.create();
            drawSignalS2(gs,
                         dim);
          }
        } finally {
          if (gs != null) {
            gs.dispose();
          }
        }
        g.setStroke(createStroke(w,
                                 true));
        for (Integer p : endPoints) {
          drawEndPoint(g,
                       dim,
                       p);
        }
      } else {
        drawSpecial(g,
                    dim,
                    symbolType.getButtonColor(),
                    symbolType.getLabel());
      }
      g.setStroke(createStroke(w,
                               false));
      List<Integer> cp = cornerPoints.values().stream().
              flatMap((s) -> s.stream()).
              collect(Collectors.toList());
      for (Integer e : cp) {
        drawCorner(g,
                   dim,
                   e);
      }
    } finally {
      g.dispose();
    }
  }

  private void drawSpecial(Graphics2D graphics,
                           Dimension dim,
                           Color color,
                           String label)
  {
    final double scale = 1d / 20d;
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setColor(color);
      g.setFont(getFont());
      g.fill(new Rectangle(dim));
      Rectangle r = getLedRect(9);
      FontMetrics metrics = g.getFontMetrics();
      final Rectangle2D textBounds = metrics.getStringBounds(label,
                                                             g);
      Rectangle2D textBox = new Rectangle.Double((r.getCenterX() - textBounds.getCenterX()) - textBounds.getWidth() * scale,
                                                 r.getMaxY(),
                                                 textBounds.getWidth() + textBounds.getWidth() * 2 * scale,
                                                 textBounds.getHeight() + textBounds.getHeight() * scale);
      g.setColor(Color.WHITE);
      g.fill(textBox);
      g.setColor(getForeground());
      g.draw(textBox);
      g.drawString(label,
                   (int) (textBox.getX() + textBounds.getWidth() * scale),
                   (int) (textBox.getMaxY() - textBounds.getHeight() - textBounds.getY()));
    } finally {
      g.dispose();
    }
  }

  private void fireAction()
  {
    ActionEvent e = new ActionEvent(this,
                                    ActionEvent.ACTION_FIRST,
                                    "ButtonPressed");
    for (ActionListener l : actionListener) {
      l.actionPerformed(e);
    }
    if (this.element != null) {
      element.action();
    }
  }

  @Override
  public void setEnabled(boolean en)
  {
    super.setEnabled(en);
    ledPanel1.setEnabled(en);
    ledPanel2.setEnabled(en);
    ledPanel3.setEnabled(en);
    ledPanel4.setEnabled(en);
    ledPanel5.setEnabled(en);
    ledPanel6.setEnabled(en);
    ledPanel7.setEnabled(en);
    ledPanel8.setEnabled(en);
    ledPanel9.setEnabled(en);
    background = null;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
   * this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    setFont(new java.awt.Font("DINPro-Regular", 0, 12)); // NOI18N
    setMinimumSize(new java.awt.Dimension(50, 50));
    setPreferredSize(new java.awt.Dimension(75, 75));
    addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        formMouseClicked(evt);
      }
    });
    setLayout(new java.awt.GridLayout(3, 3));

    ledPanel1.setLedState(at.or.reder.rpi.LedPanel.LedState.OFF);
    add(ledPanel1);
    add(ledPanel2);
    add(ledPanel3);
    add(ledPanel4);

    ledPanel9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        ledPanel9ActionPerformed(evt);
      }
    });
    add(ledPanel9);
    add(ledPanel5);
    add(ledPanel6);
    add(ledPanel7);
    add(ledPanel8);
  }// </editor-fold>//GEN-END:initComponents

  private void ledPanel9ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ledPanel9ActionPerformed
  {//GEN-HEADEREND:event_ledPanel9ActionPerformed
    fireAction();
  }//GEN-LAST:event_ledPanel9ActionPerformed

  private void formMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseClicked
  {//GEN-HEADEREND:event_formMouseClicked
    if (symbolType.getButtonColor() != null) {
      fireAction();
    }
  }//GEN-LAST:event_formMouseClicked

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final at.or.reder.rpi.LedPanel ledPanel1 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel2 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel3 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel4 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel5 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel6 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel7 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel8 = new at.or.reder.rpi.LedPanel();
  private final at.or.reder.rpi.LedPanel ledPanel9 = new at.or.reder.rpi.LedPanel();
  // End of variables declaration//GEN-END:variables
}
