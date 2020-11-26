/*
 * Copyright 2020 Wolfgang Reder.
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
package at.or.reder.rpi.edk;

import at.or.reder.controller.PSController;
import at.or.reder.controller.PSControllerEvent;
import at.or.reder.controller.impl.PSControllerFactoryImpl;
import at.or.reder.dcc.LinkState;
import at.or.reder.rpi.DevicePanel;
import at.or.reder.tools.edk750.EDK750;
import at.or.reder.tools.edk750.EDK750Factory;
import at.or.reder.tools.edk750.EDKAxisEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Wolfgang Reder
 */
public class EDKPanel extends DevicePanel
{

  private EDK750 _edk;
  public static final Set<String> ids = Set.of("ev:0003:054c:09cc:8111",
                                               "ev:0005:054c:09cc:8001");
  private PSController controller;
  private final PSControllerFactoryImpl factory;

  /**
   * Creates new form EDKPanel
   */
  public EDKPanel()
  {
    factory = new PSControllerFactoryImpl();
    initComponents();
    factory.addPSControllerEventListener(this::onPSController);
    factory.startScanning();
  }

  private void onEDKPropertyChange(PropertyChangeEvent evt)
  {
    EDK750 edk = getEDK();
    if (edk != null) {
      switch (evt.getPropertyName()) {
        case EDK750.PROP_EMERGENCY:
        case EDK750.PROP_STARTED:
          checkEmergency();
          checkStarted();
          break;
      }
    }
  }

  private void checkStarted()
  {
    EDK750 edk = getEDK();
    ldMotor.setLedBlinking(false);
    ldMotor.setLedOn(edk != null && edk.isStarted());
  }

  private void checkEmergency()
  {
    EDK750 edk = getEDK();
    ldEmergency.setLedOn(edk == null);
    ldEmergency.setLedBlinking(edk != null && edk.isInEmergencyMode());
  }

  private void onEDKAxisMovement(EDKAxisEvent evt)
  {
    checkEmergency();
    checkStarted();
    switch (evt.getAxis()) {
      case BEAM:
        ldBeamUp.setLedOn(evt.getValue() > 0);
        ldBeamDown.setLedOn(evt.getValue() < 0);
        break;
      case BEAMLENGTH:
        ldBeamOut.setLedOn(evt.getValue() > 0);
        ldBeamIn.setLedOn(evt.getValue() < 0);
        break;
      case WINCH:
        ldWinchDown.setLedOn(evt.getValue() < 0);
        ldWinchUp.setLedOn(evt.getValue() > 0);
        break;
    }
  }

  private void onPSController(PSControllerEvent evt)
  {
    PSController ec = evt.getController();
    if (ids.contains(ec.getId())) {
      switch (evt.getType()) {
        case CLOSE:
          if (controller != null && ec.getId().equals(controller.getId())) {
            disconnect();
          }
          break;
        case OPEN:
          if (ids.contains(ec.getId())) {
            if (controller != null) {
              disconnect();
            }
            connect(ec);
          }
          break;
      }
    }
  }

  private EDK750 getEDK()
  {
    if (_edk == null) {
      try {
        EDK750Factory edkFactory = Lookup.getDefault().lookup(EDK750Factory.class);
        if (edkFactory != null) {
          _edk = edkFactory.createInstance(750,
                                           null,
                                           null,
                                           null);
          _edk.addPropertyChangeListener(this::onEDKPropertyChange);
          _edk.addEDKAxisListener(this::onEDKAxisMovement);
          checkEmergency();
          checkStarted();
        }
      } catch (IOException ex) {

      }
    }
    return _edk;
  }

  private void connect(PSController c)
  {
    factory.stopScanning();
    controller = c;
    EDK750 edk = getEDK();
    if (edk != null) {
      edk.setPSController(controller);
      edk.setControllerId(controller.getId());
    }
  }

  private void disconnect()
  {
    if (controller != null) {
      EDK750 edk = getEDK();
      if (edk != null) {
        edk.setPSController(null);
      }
    }
    controller = null;
  }

  @Override
  protected void doLinkStateChanged(LinkState linkState)
  {
    EDK750 edk = getEDK();
    if (edk != null) {
      try {
        if (linkState == LinkState.CONNECTED && device != null) {
          edk.setDCCController(device);
        } else {
          edk.setDCCController(null);
        }
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  @Override
  protected void connectListener()
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
    java.awt.GridBagConstraints gridBagConstraints;

    blinkTimer1 = new eu.hansolo.steelseries.tools.BlinkTimer();
    jPanel1 = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();

    blinkTimer1.setRunning(true);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel3.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel3.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
    jPanel1.add(jLabel3, gridBagConstraints);

    jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel4.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel4.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
    jPanel1.add(jLabel4, gridBagConstraints);

    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel5.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel5.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    jPanel1.add(jLabel5, gridBagConstraints);

    jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel6.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel6.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    jPanel1.add(jLabel6, gridBagConstraints);

    jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel7.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel7.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    jPanel1.add(jLabel7, gridBagConstraints);

    ldWinchDown.setBlinkTimer(blinkTimer1);
    ldWinchDown.setLedColor(eu.hansolo.steelseries.tools.LedColor.YELLOW_LED);
    ldWinchDown.setPreferredSize(new java.awt.Dimension(30, 30));
    ldWinchDown.setSize(new java.awt.Dimension(30, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.33;
    gridBagConstraints.weighty = 0.33;
    jPanel1.add(ldWinchDown, gridBagConstraints);

    ldWinchUp.setBlinkTimer(blinkTimer1);
    ldWinchUp.setLedColor(eu.hansolo.steelseries.tools.LedColor.YELLOW_LED);
    ldWinchUp.setPreferredSize(new java.awt.Dimension(30, 30));
    ldWinchUp.setSize(new java.awt.Dimension(30, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.33;
    gridBagConstraints.weighty = 0.33;
    jPanel1.add(ldWinchUp, gridBagConstraints);

    ldBeamIn.setBlinkTimer(blinkTimer1);
    ldBeamIn.setLedColor(eu.hansolo.steelseries.tools.LedColor.YELLOW_LED);
    ldBeamIn.setPreferredSize(new java.awt.Dimension(30, 30));
    ldBeamIn.setSize(new java.awt.Dimension(30, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.33;
    gridBagConstraints.weighty = 0.33;
    jPanel1.add(ldBeamIn, gridBagConstraints);

    ldBeamOut.setBlinkTimer(blinkTimer1);
    ldBeamOut.setLedColor(eu.hansolo.steelseries.tools.LedColor.YELLOW_LED);
    ldBeamOut.setPreferredSize(new java.awt.Dimension(30, 30));
    ldBeamOut.setSize(new java.awt.Dimension(30, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.33;
    gridBagConstraints.weighty = 0.33;
    jPanel1.add(ldBeamOut, gridBagConstraints);

    ldBeamDown.setBlinkTimer(blinkTimer1);
    ldBeamDown.setLedColor(eu.hansolo.steelseries.tools.LedColor.YELLOW_LED);
    ldBeamDown.setPreferredSize(new java.awt.Dimension(30, 30));
    ldBeamDown.setSize(new java.awt.Dimension(30, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.33;
    gridBagConstraints.weighty = 0.33;
    jPanel1.add(ldBeamDown, gridBagConstraints);

    ldBeamUp.setBlinkTimer(blinkTimer1);
    ldBeamUp.setLedColor(eu.hansolo.steelseries.tools.LedColor.YELLOW_LED);
    ldBeamUp.setPreferredSize(new java.awt.Dimension(30, 30));
    ldBeamUp.setSize(new java.awt.Dimension(30, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.33;
    gridBagConstraints.weighty = 0.33;
    jPanel1.add(ldBeamUp, gridBagConstraints);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    jLabel2.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel2.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    jPanel2.add(jLabel2, gridBagConstraints);

    ldEmergency.setBlinkTimer(blinkTimer1);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 1.0;
    jPanel2.add(ldEmergency, gridBagConstraints);

    jLabel1.setText(org.openide.util.NbBundle.getMessage(EDKPanel.class, "EDKPanel.jLabel1.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    jPanel2.add(jLabel1, gridBagConstraints);

    ldMotor.setBlinkTimer(blinkTimer1);
    ldMotor.setLedColor(eu.hansolo.steelseries.tools.LedColor.GREEN_LED);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 1.0;
    jPanel2.add(ldMotor, gridBagConstraints);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
        .addContainerGap(489, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(181, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private eu.hansolo.steelseries.tools.BlinkTimer blinkTimer1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private final eu.hansolo.steelseries.extras.Led ldBeamDown = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldBeamIn = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldBeamOut = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldBeamUp = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldEmergency = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldMotor = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldWinchDown = new eu.hansolo.steelseries.extras.Led();
  private final eu.hansolo.steelseries.extras.Led ldWinchUp = new eu.hansolo.steelseries.extras.Led();
  // End of variables declaration//GEN-END:variables
}
