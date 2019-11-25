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

import at.or.reder.dcc.LinkState;
import at.or.reder.dcc.PowerEvent;
import at.or.reder.dcc.PowerEventListener;
import at.or.reder.dcc.PowerMode;
import at.or.reder.dcc.PowerPort;
import at.or.reder.zcan20.packet.PowerInfo;
import eu.hansolo.steelseries.tools.Section;
import java.awt.Color;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Wolfgang Reder
 */
@Messages({"PowerPanel_PORT_OUT_1=Schiene 1",
           "PowerPanel_PORT_OUT_2=Schiene 2",
           "PowerPanel_UNKNOWN=Eingang"})
public final class PowerPanel extends DevicePanel
{

  public static final String PROP_CURRENT = "current";
  public static final String PROP_VOLTAGE = "voltage";
  public static final String PROP_POWERMODE = "powerMode";
  private double limitCurrent;
  private double limitVoltage;
  private PowerMode mode;
  private final PowerEventListener packetListener = this::onPacket;
  private boolean listenerConnected;
  private PowerPort port;

  public PowerPanel()
  {
    initComponents();
    setMaxCurrent(1);
    setLimitCurrent(0);
    setMaxVoltage(1);
    setLimitVoltage(0);
    setPowerMode(PowerMode.ON);
  }

  private void setControlState()
  {
    lnCurrent.setEnabled(device != null && device.getLinkState() == LinkState.CONNECTED);
    lnVoltage.setEnabled(device != null && device.getLinkState() == LinkState.CONNECTED);
    lbCaption.setEnabled(device != null && device.getLinkState() == LinkState.CONNECTED);
    lbState.setEnabled(device != null && device.getLinkState() == LinkState.CONNECTED);

  }

  @Override
  protected void doLinkStateChanged(LinkState linkState)
  {
    SwingUtilities.invokeLater(this::setControlState);
  }

  private void onPacket(PowerEvent event)
  {
    if (port != null) {
      PowerInfo power = event.getLookup().lookup(PowerInfo.class);
      if (power != null) {
        SwingUtilities.invokeLater(() -> assignPowerInfo(power));
      } else {
        SwingUtilities.invokeLater(() -> assignPowerState(event.getPort(),
                                                          event.getMode()));
      }
    }
  }

  private void assignPowerInfo(PowerInfo power)
  {
    if (port != PowerPort.UNKNOWN) {
      setCurrent(power.getOutputCurrent(port));
      setVoltage(power.getOutputVoltage(port));
    } else {
      setCurrent(power.getInputCurrent());
      setVoltage(power.getInputVoltage());
    }
  }

  private void assignPowerState(PowerPort port,
                                PowerMode mode)
  {
    if (port == this.port) {
      setPowerMode(mode);
    }
  }

  public PowerPort getPort()
  {
    return port;
  }

  public void setPort(PowerPort port)
  {
    if (this.port != port) {
      this.port = port;
      switch (port) {
        case OUT_1:
          lbCaption.setText(Bundle.PowerPanel_PORT_OUT_1());
          setPowerMode(PowerMode.PENDING);
          connectListener();
          break;
        case OUT_2:
          lbCaption.setText(Bundle.PowerPanel_PORT_OUT_2());
          setPowerMode(PowerMode.PENDING);
          connectListener();
          break;
        case UNKNOWN:
          lbCaption.setText(Bundle.PowerPanel_UNKNOWN());
          setPowerMode(PowerMode.ON);
          connectListener();
          break;
        default:
          this.port = null;
          lbCaption.setText(null);
          setPowerMode(PowerMode.PENDING);
          disconnectListener();
      }
    }
  }

  @Override
  protected void disconnectListener()
  {
    if (this.device != null) {
      this.device.removePowerEventListener(packetListener);
      listenerConnected = false;
    }
    setControlState();
  }

  @Override
  protected void connectListener()
  {
    if (this.device != null && !listenerConnected) {
      this.device.addPowerEventListener(packetListener);
      listenerConnected = true;
    }
    setControlState();
  }

  public PowerMode getPowerMode()
  {
    return mode;
  }

  public void setPowerMode(PowerMode mode)
  {
    if (mode == null) {
      mode = PowerMode.PENDING;
    }
    if (this.mode != mode) {
      PowerMode oldMode = this.mode;
      this.mode = mode;
      lbState.setText(mode.getLabel());
      if (mode == PowerMode.OVERCURRENT) {
        lnCurrent.setGlowVisible(true);
        lnCurrent.setGlowing(true);
        lnCurrent.setGlowPulsating(true);
      } else if ((mode != PowerMode.OVERCURRENT) && (mode != PowerMode.UNDERVOLTAGE)) {
        lnCurrent.setGlowVisible(false);
        lnVoltage.setGlowVisible(false);
      } else if (mode == PowerMode.UNDERVOLTAGE) {
        lnVoltage.setGlowVisible(true);
        lnVoltage.setGlowing(true);
        lnVoltage.setGlowPulsating(true);
      }
      firePropertyChange(PROP_POWERMODE,
                         oldMode,
                         this.mode);
    }
    setControlState();
  }

  public String getCaption()
  {
    return lbCaption.getText();
  }

  public void setCaption(String caption)
  {
    lbCaption.setText(caption);
  }

  public double getCurrent()
  {
    return lnCurrent.getValue();
  }

  public void setCurrent(double value)
  {
    if (value != lnCurrent.getValue()) {
      double oldValue = lnCurrent.getValue();
      lnCurrent.setValue(value);
      double newValue = lnCurrent.getValue();
      if (value != newValue) {
        firePropertyChange(PROP_CURRENT,
                           oldValue,
                           newValue);
      }
    }
  }

  public double getVoltage()
  {
    return lnVoltage.getValue();
  }

  public void setVoltage(double voltage)
  {
    if (voltage != lnVoltage.getValue()) {
      double oldValue = lnVoltage.getValue();
      lnVoltage.setValue(voltage);
      double newValue = lnVoltage.getValue();
      if (oldValue != newValue) {
        firePropertyChange(PROP_VOLTAGE,
                           oldValue,
                           newValue);
      }
    }
  }

  public double getMaxCurrent()
  {
    return lnCurrent.getMaxValue();
  }

  public void setMaxCurrent(double maxCurrent)
  {
    lnCurrent.setMaxValue(maxCurrent);
  }

  public double getMaxVoltage()
  {
    return lnVoltage.getMaxValue();
  }

  public void setMaxVoltage(double maxVoltage)
  {
    lnVoltage.setMaxValue(maxVoltage);
  }

  public double getMinCurrent()
  {
    return lnCurrent.getMinValue();
  }

  public void setMinCurrent(double minCurrent)
  {
    lnCurrent.setMinValue(minCurrent);
  }

  public double getMinVoltage()
  {
    return lnVoltage.getMinValue();
  }

  public void setMinVoltage(double minVoltage)
  {
    lnVoltage.setMinValue(minVoltage);
  }

  public double getLimitCurrent()
  {
    return limitCurrent;
  }

  public void setLimitCurrent(double limitCurrent)
  {
    this.limitCurrent = limitCurrent;
    if (limitCurrent != Double.NaN) {
      lnCurrent.setSections(new Section(lnCurrent.getMinValue(),
                                        limitCurrent,
                                        Color.GREEN),
                            new Section(limitCurrent,
                                        lnCurrent.getMaxValue(),
                                        Color.RED));
    } else {
      lnCurrent.setSections();
    }
  }

  public double getLimitVoltage()
  {
    return limitVoltage;
  }

  public void setLimitVoltage(double limitVoltage)
  {
    this.limitVoltage = limitVoltage;
    if (limitVoltage != Double.NaN) {
      lnVoltage.setSections(new Section(lnVoltage.getMinValue(),
                                        limitVoltage,
                                        Color.GREEN),
                            new Section(limitVoltage,
                                        lnVoltage.getMaxValue(),
                                        Color.RED));
    } else {
      lnVoltage.setSections();
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

    lnCurrent.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
    lnCurrent.setFrameVisible(false);
    lnCurrent.setGlowColor(new java.awt.Color(204, 0, 0));
    lnCurrent.setGlowing(true);
    lnCurrent.setLabelNumberFormat(eu.hansolo.steelseries.tools.NumberFormat.FRACTIONAL);
    lnCurrent.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.STANDARD_LCD);
    lnCurrent.setLcdDecimals(3);
    lnCurrent.setLcdUnitString(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lnCurrent.lcdUnitString")); // NOI18N
    lnCurrent.setLcdUnitStringVisible(true);
    lnCurrent.setLedVisible(false);
    lnCurrent.setMaxValue(6.0);
    lnCurrent.setTitle(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lnCurrent.title")); // NOI18N
    lnCurrent.setUnitString(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lnCurrent.unitString")); // NOI18N
    lnCurrent.setValue(2.16);

    lbCaption.setFont(new java.awt.Font("Arial Black", 0, 12)); // NOI18N
    lbCaption.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lbCaption.setText(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lbCaption.text")); // NOI18N

    lnVoltage.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
    lnVoltage.setFrameVisible(false);
    lnVoltage.setGlowColor(new java.awt.Color(204, 0, 0));
    lnVoltage.setLabelNumberFormat(eu.hansolo.steelseries.tools.NumberFormat.FRACTIONAL);
    lnVoltage.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.STANDARD_LCD);
    lnVoltage.setLcdDecimals(2);
    lnVoltage.setLcdUnitString(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lnVoltage.lcdUnitString")); // NOI18N
    lnVoltage.setLcdUnitStringVisible(true);
    lnVoltage.setLedVisible(false);
    lnVoltage.setMaxValue(20.0);
    lnVoltage.setTitle(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lnVoltage.title")); // NOI18N
    lnVoltage.setUnitString(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lnVoltage.unitString")); // NOI18N
    lnVoltage.setValue(13.0);
    lnVoltage.setValueColor(eu.hansolo.steelseries.tools.ColorDef.BLUE);

    lbState.setFont(new java.awt.Font("Arial Black", 0, 12)); // NOI18N
    lbState.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lbState.setText(org.openide.util.NbBundle.getMessage(PowerPanel.class, "PowerPanel.lbState.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lbState, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lbCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(lnCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lnVoltage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addComponent(lbCaption)
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lnCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lnVoltage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 0, 0)
        .addComponent(lbState)
        .addGap(0, 0, 0))
    );
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final javax.swing.JLabel lbCaption = new javax.swing.JLabel();
  private final javax.swing.JLabel lbState = new javax.swing.JLabel();
  private final eu.hansolo.steelseries.gauges.Linear lnCurrent = new eu.hansolo.steelseries.gauges.Linear();
  private final eu.hansolo.steelseries.gauges.Linear lnVoltage = new eu.hansolo.steelseries.gauges.Linear();
  // End of variables declaration//GEN-END:variables
}
