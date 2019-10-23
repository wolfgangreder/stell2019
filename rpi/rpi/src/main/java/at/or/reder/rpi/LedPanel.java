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

import eu.hansolo.steelseries.tools.LedColor;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JPanel;

public final class LedPanel extends JPanel
{

  public static enum LedState
  {
    OFF,
    ON,
    BLINK;
  };
  private LedState ledState = LedState.OFF;
  private final Set<ActionListener> actionListener = new CopyOnWriteArraySet<>();

  public LedPanel()
  {
    initComponents();
  }

  public void addActionListener(ActionListener actionListener)
  {
    if (actionListener != null) {
      this.actionListener.add(actionListener);
    }
  }

  public void removeActionListener(ActionListener actionListener)
  {
    this.actionListener.remove(actionListener);
  }

  public void setLedState(LedState ledState)
  {
    if (this.ledState != ledState && ledState != null) {
      this.ledState = ledState;
      led1.setLedOn(false);
      led1.setLedBlinking(false);
      switch (ledState) {
        case ON:
          led1.setLedOn(true);
          led1.setLedBlinking(false);
          break;
        case BLINK:
          led1.setLedOn(true);
          led1.setLedBlinking(true);
          break;
      }
    }
  }

  public void setState(LedColor ledColor,
                       Color buttonColor)
  {
    CardLayout cardLayout = (CardLayout) getLayout();
    if (ledColor == null && buttonColor == null) {
      cardLayout.show(this,
                      "empty");
    } else if (buttonColor != null) {
      cardLayout.show(this,
                      "button");
      jButton1.setForeground(buttonColor);
    } else {
      cardLayout.show(this,
                      "led");
      led1.setLedColor(ledColor);
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

    jPanel1 = new javax.swing.JPanel();
    jButton1 = new javax.swing.JButton();
    led1 = new eu.hansolo.steelseries.extras.Led();

    setLayout(new java.awt.CardLayout());

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 249, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 245, Short.MAX_VALUE)
    );

    add(jPanel1, "empty");

    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton1ActionPerformed(evt);
      }
    });
    add(jButton1, "button");
    add(led1, "led");
  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
  {//GEN-HEADEREND:event_jButton1ActionPerformed
    evt.setSource(this);
    for (ActionListener al : actionListener) {
      al.actionPerformed(evt);
    }
  }//GEN-LAST:event_jButton1ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JPanel jPanel1;
  private eu.hansolo.steelseries.extras.Led led1;
  // End of variables declaration//GEN-END:variables
}