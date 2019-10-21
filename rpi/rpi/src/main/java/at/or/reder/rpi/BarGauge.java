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

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

/**
 *
 * @author Wolfgang Reder
 */
public final class BarGauge extends JComponent
{

  private double rangeMin;
  private double rangeMax;
  private double tickIntervall;
  private double value;
  private double maxValue;
  private double minValue;

  public BarGauge()
  {
    setForeground(new Color(53,
                            114,
                            188));
    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
  }

  @Override
  protected void paintComponent(Graphics g)
  {

  }

}
