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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public final class SymbolPanel extends JPanel
{

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

  public SymbolPanel()
  {
    initComponents();
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

  public void setElement(TrackElement element)
  {
    this.element = element;
    if (element != null) {
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

  private void drawCorner(Graphics2D g,
                          Dimension dim,
                          int corner)
  {
    double w = (dim.getHeight() / 2 - 1) / 3;
    double phi = Math.atan2(dim.getHeight(),
                            dim.getWidth());
    Polygon p = null;
    Color savedColor = g.getColor();
    Color color = savedColor;
    switch (corner) {
      case 1:
        p = buildPolygon(0,
                         0,
                         0,
                         w * Math.sin(phi),
                         w * Math.cos(phi),
                         0);
//        color = Color.RED;
        break;
      case 3:
        p = buildPolygon(dim.getWidth(),
                         0,
                         dim.getWidth(),
                         w * Math.sin(phi),
                         dim.getWidth() - w * Math.cos(phi),
                         0);
//        color = Color.GREEN;
        break;
      case 6:
        p = buildPolygon(0,
                         dim.getHeight(),
                         0,
                         dim.getHeight() - w * Math.sin(phi),
                         w * Math.cos(phi),
                         dim.getHeight());
//        color = Color.BLUE;
        break;
      case 8:
        p = buildPolygon(dim.getWidth(),
                         dim.getHeight(),
                         dim.getWidth(),
                         dim.getHeight() - w * Math.sin(phi),
                         dim.getWidth() - w * Math.cos(phi),
                         dim.getHeight());
//        color = Color.WHITE;
        break;
    }
    try {
      if (p != null) {
        g.setColor(color);
        g.drawPolygon(p);
        g.fillPolygon(p);
      }
    } finally {
      g.setColor(savedColor);
    }
  }

  private void drawEndPoint(Graphics2D g,
                            Dimension dim,
                            int endPoint)
  {
    double cy = dim.getHeight() / 2 - 1;
    double cx = dim.getWidth() / 2;
    double w = cy / 3;
    Polygon p = null;
    Stroke savedStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) w,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND));
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
    try {
      if (p != null) {
        g.drawPolygon(p);
      }
    } finally {
      g.setStroke(savedStroke);
    }
  }

  private void buildBackground()
  {
    Dimension dim = getSize();
    background = new BufferedImage(dim.width,
                                   dim.height,
                                   BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = background.createGraphics();
    try {
      g.setColor(getForeground());
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
//      g.drawRect(0,
//                 0,
//                 dim.width - 1,
//                 dim.height - 1);
      for (Integer p : endPoints) {
        drawEndPoint(g,
                     dim,
                     p);
      }
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

  private void fireAction()
  {
    ActionEvent e = new ActionEvent(this,
                                    ActionEvent.ACTION_FIRST,
                                    "ButtonPressed");
    for (ActionListener l : actionListener) {
      l.actionPerformed(e);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
   * this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

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
