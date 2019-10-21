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

import at.or.reder.zcan20.LinkState;
import at.or.reder.zcan20.ZCAN;

/**
 *
 * @author Wolfgang Reder
 */
public class DashboardPanel extends DevicePanel
{

  /**
   * Creates new form DashboardPanel
   */
  public DashboardPanel()
  {
    initComponents();
  }

  @Override
  protected void doLinkStateChanged(LinkState linkState)
  {
    ledConnection.setLedOn(linkState == LinkState.OPEN);
  }

  @Override
  public void setDevice(ZCAN device)
  {
    super.setDevice(device);
    ledConnection.setLedOn(device != null && device.getLinkState() == LinkState.OPEN);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
   * this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

    powerControlPanel1.setPort(at.or.reder.zcan20.PowerPort.OUT_1);

    jLabel2.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel2.setText(org.openide.util.NbBundle.getMessage(DashboardPanel.class, "DashboardPanel.jLabel2.text")); // NOI18N

    powerControlPanel2.setPort(at.or.reder.zcan20.PowerPort.OUT_2);

    jLabel3.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel3.setText(org.openide.util.NbBundle.getMessage(DashboardPanel.class, "DashboardPanel.jLabel3.text")); // NOI18N

    ledConnection.setLedColor(eu.hansolo.steelseries.tools.LedColor.GREEN_LED);

    jLabel1.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    jLabel1.setText(org.openide.util.NbBundle.getMessage(DashboardPanel.class, "DashboardPanel.jLabel1.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(powerControlPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(powerControlPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 246, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel1)
          .addComponent(ledConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, 0))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(jLabel3)
          .addComponent(jLabel1))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(powerControlPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
              .addComponent(powerControlPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ledConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
    );
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final eu.hansolo.steelseries.extras.Led ledConnection = new eu.hansolo.steelseries.extras.Led();
  private final at.or.reder.rpi.PowerControlPanel powerControlPanel1 = new at.or.reder.rpi.PowerControlPanel();
  private final at.or.reder.rpi.PowerControlPanel powerControlPanel2 = new at.or.reder.rpi.PowerControlPanel();
  // End of variables declaration//GEN-END:variables
}
