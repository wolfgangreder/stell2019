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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JPanel;

public final class SymbolPanel extends JPanel
{

  private SymbolType symbolType = SymbolType.EMPTY;
  private final Set<ActionListener> actionListener = new CopyOnWriteArraySet<>();
  private TrackElement element;
  private SymbolRotation rotation = SymbolRotation.NONE;
  private final List<Point> endPoints = new ArrayList<>();
  private final List<Rectangle> bars = new ArrayList<>();

  public SymbolPanel()
  {
    initComponents();
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
    for (Integer l : symbolType.getLines()) {
      switch (l) {
        case 1:
          endPoints.add(new Point(-1,
                                  -1));
          break;
        case 2:
          endPoints.add(new Point(0,
                                  -1));
          break;
        case 3:
          endPoints.add(new Point(1,
                                  -1));
          break;
        case 4:
          endPoints.add(new Point(-1,
                                  0));
          break;
        case 5:
          endPoints.add(new Point(1,
                                  0));
          break;
        case 6:
          endPoints.add(new Point(-1,
                                  1));
          break;
        case 7:
          endPoints.add(new Point(0,
                                  1));
          break;
        case 8:
          endPoints.add(new Point(1,
                                  1));
          break;
      }
    }
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
          endPoints.add(new Point(-1,
                                  -1));
          break;
        case 4:
          endPoints.add(new Point(0,
                                  -1));
          break;
        case 1:
          endPoints.add(new Point(1,
                                  -1));
          break;
        case 7:
          endPoints.add(new Point(-1,
                                  0));
          break;
        case 2:
          endPoints.add(new Point(1,
                                  0));
          break;
        case 8:
          endPoints.add(new Point(-1,
                                  1));
          break;
        case 5:
          endPoints.add(new Point(0,
                                  1));
          break;
        case 3:
          endPoints.add(new Point(1,
                                  1));
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
          endPoints.add(new Point(-1,
                                  -1));
          break;
        case 7:
          endPoints.add(new Point(0,
                                  -1));
          break;
        case 6:
          endPoints.add(new Point(1,
                                  -1));
          break;
        case 5:
          endPoints.add(new Point(-1,
                                  0));
          break;
        case 4:
          endPoints.add(new Point(1,
                                  0));
          break;
        case 3:
          endPoints.add(new Point(-1,
                                  1));
          break;
        case 2:
          endPoints.add(new Point(0,
                                  1));
          break;
        case 1:
          endPoints.add(new Point(1,
                                  1));
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
          endPoints.add(new Point(-1,
                                  -1));
          break;
        case 5:
          endPoints.add(new Point(0,
                                  -1));
          break;
        case 8:
          endPoints.add(new Point(1,
                                  -1));
          break;
        case 2:
          endPoints.add(new Point(-1,
                                  0));
          break;
        case 7:
          endPoints.add(new Point(1,
                                  0));
          break;
        case 1:
          endPoints.add(new Point(-1,
                                  1));
          break;
        case 4:
          endPoints.add(new Point(0,
                                  1));
          break;
        case 6:
          endPoints.add(new Point(1,
                                  1));
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
    buildBars();
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
    if (this.symbolType != null) {
      Dimension dim = getSize();
      dim.setSize(dim.getWidth() / 2,
                  dim.getHeight() / 2);
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setStroke(new BasicStroke((int) (dim.getHeight() / 6),
                                     BasicStroke.CAP_BUTT,
                                     BasicStroke.JOIN_BEVEL));
        g.setColor(Color.BLACK);
        for (Point p : endPoints) {
          System.err.println("dim.width=" + dim.width);
          System.err.println("dim.height=" + dim.height);
          System.err.println("end.x=" + (dim.width + (dim.width * p.x)));
          System.err.println("end.y=" + (dim.height + (dim.height * p.y)));

          g.drawLine(dim.width,
                     dim.height,
                     dim.width + (dim.width * p.x),
                     dim.height + (dim.height * p.y));
        }
      } finally {
        g2.dispose();
      }
    }
  }

  @Override
  public void setBounds(int x,
                        int y,
                        int width,
                        int height
  )
  {
//    int wh = Math.min(width,
//                      height);
    super.setBounds(x,
                    y,
                    width,
                    height);
    buildBars();
  }

  private void buildBars()
  {

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
    setLayout(new java.awt.GridLayout(3, 3));

    ledPanel1.setLedState(at.or.reder.rpi.LedPanel.LedState.ON);
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
    evt.setSource(this);
    for (ActionListener l : actionListener) {
      l.actionPerformed(evt);
    }
  }//GEN-LAST:event_ledPanel9ActionPerformed

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
