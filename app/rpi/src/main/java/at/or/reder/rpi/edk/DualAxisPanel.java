/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.rpi.edk;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author wolfi
 */
public class DualAxisPanel extends JComponent
{

  private Point2D.Float position = new Point2D.Float();
  private BufferedImage img;
  private Point2D.Float lastPoint;
  private boolean drawTrack;

  public boolean isDrawTrack()
  {
    return drawTrack;
  }

  public void setDrawTrack(boolean drawTrack)
  {
    this.drawTrack = drawTrack;
    if (!drawTrack) {
      img = null;
    }
  }

  public Point2D.Float getPosition()
  {
    return position;
  }

  public void setPosition(Point2D.Float position)
  {
    this.position = position;
  }

  private void paintCrosshair(Graphics g)
  {
  }

  private void paintCursor(Graphics g)
  {
    Dimension d = getSize();
    if (img == null) {
      img = new BufferedImage(d.width,
                              d.height,
                              BufferedImage.TYPE_INT_RGB);
      Graphics ig = img.createGraphics();
      try {
        ig.setColor(Color.WHITE);
        ig.fillRect(0,
                    0,
                    d.width,
                    d.height);
      } finally {
        ig.dispose();
      }
    }
    Point2D.Float p = new Point2D.Float(
        (position.x / 2f * d.width) + (d.width / 2),
        (position.y / 2f * d.height) + (d.height / 2));
    if (lastPoint != null && drawTrack) {
      Graphics ig = img.createGraphics();
      try {
        ig.setColor(Color.BLACK);
        ig.drawLine((int) lastPoint.x,
                    (int) lastPoint.y,
                    (int) p.x,
                    (int) p.y);
      } finally {
        ig.dispose();
      }
    }
    g.drawImage(img,
                0,
                0,
                null);
    float l = (float) (7 / Math.sqrt(2));
    g.drawLine((int) (p.x - l),
               (int) (p.y - l),
               (int) (p.x + l),
               (int) (p.y + l));
    g.drawLine((int) (p.x - l),
               (int) (p.y + l),
               (int) (p.x + l),
               (int) (p.y - l));
    lastPoint = p;
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    paintCrosshair(g);
    paintCursor(g);
  }

}
