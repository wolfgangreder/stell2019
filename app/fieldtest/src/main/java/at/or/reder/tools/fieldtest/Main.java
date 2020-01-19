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
package at.or.reder.tools.fieldtest;

import at.or.reder.tools.fieldtest.model.CrossingState;
import at.or.reder.tools.fieldtest.model.Feature;
import at.or.reder.tools.fieldtest.model.Field;
import at.or.reder.tools.fieldtest.model.LedState;
import at.or.reder.tools.fieldtest.model.ModuleState;
import at.or.reder.tools.fieldtest.model.ModuleType;
import at.or.reder.tools.fieldtest.model.SemaphoreState;
import at.or.reder.tools.fieldtest.model.State;
import at.or.reder.tools.fieldtest.model.ThreewayState;
import at.or.reder.tools.fieldtest.model.TurnoutState;
import at.or.reder.tools.fieldtest.model.Version;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;

public class Main extends javax.swing.JFrame
{

  private final Field field;
  private final AbstractComboBoxModel<ModuleType> typeModel = AbstractComboBoxModel.instanceOf(ModuleType.class);
  private final AbstractComboBoxModel<TurnoutState> turnoutModel = AbstractComboBoxModel.instanceOf(TurnoutState.class);
  private final AbstractComboBoxModel<ThreewayState> threewayModel = AbstractComboBoxModel.instanceOf(ThreewayState.class);
  private final AbstractComboBoxModel<CrossingState> crossingModel = AbstractComboBoxModel.instanceOf(CrossingState.class);
  private final AbstractComboBoxModel<SemaphoreState> semModel = AbstractComboBoxModel.instanceOf(SemaphoreState.class);
  private boolean ignoreChangeEvent;
  private final Set<Feature> features = EnumSet.noneOf(Feature.class);

  public Main() throws PortInUseException, UnsupportedCommOperationException
  {
    SerialPort port = new RXTXPort("/dev/ttyS0");
    port.setSerialPortParams(38400,//57600,
                             SerialPort.DATABITS_8,
                             SerialPort.STOPBITS_1,
                             SerialPort.PARITY_NONE);
    field = new FieldImpl(port,
                          3);
    initComponents();
    readAll();
    field.addChangeListener(this::onKeyChanged);
  }

  private void onKeyChanged(ChangeEvent evt)
  {
    ckKeyPressed.setSelected(field.isKeyPressed());
  }

  private void readAll()
  {
    try {
      Version version = field.getVersion();
      int leds = field.getLeds();
      int bMask = field.getBlinkMask();
      int bPhase = field.getBlinkPhase();
      int cal = field.getCalibration();
      int pwm = field.getPWM();
      float vcc = field.getVCC();
      int div = field.getBlinkDivider();
      int debounce = field.getDebounce();
      ModuleType type = field.getModuleType();
      features.clear();
      lbVersion.setText(version.toString());
      ledPanel.setValue(leds);
      blinkMask.setValue(bMask);
      blinkPhase.setValue(bPhase);
      spCalibration.setValue(cal);
      spPWM.setValue(pwm);
      edVCC.setValue(vcc);
      spDivider.setValue(div);
      typeModel.setSelectedItem(type);
      spDebounce.setValue(debounce);
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }

  private void writeAll()
  {
    try {
      field.setLeds(ledPanel.getValue());
      field.setBlinkMask(blinkMask.getValue());
      field.setBlinkPhase(blinkPhase.getValue());
      field.setBlinkDivider(((Number) spDivider.getValue()).intValue());
      field.setCalibration(((Number) spCalibration.getValue()).intValue());
      field.setPWM(((Number) spPWM.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }

  private void writeVolatile()
  {
    try {
      field.setLeds(ledPanel.getValue());
      field.setBlinkMask(blinkMask.getValue());
      field.setBlinkPhase(blinkPhase.getValue());
      field.setBlinkDivider(((Number) spDivider.getValue()).intValue());
      field.setPWM(((Number) spPWM.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
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

    buttonGroup1 = new javax.swing.ButtonGroup();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    jPanel1 = new javax.swing.JPanel();
    jButton8 = new javax.swing.JButton();
    jButton9 = new javax.swing.JButton();
    jLabel6 = new javax.swing.JLabel();
    jButton10 = new javax.swing.JButton();
    javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
    jButton4 = new javax.swing.JButton();
    jButton12 = new javax.swing.JButton();
    javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
    jButton6 = new javax.swing.JButton();
    javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
    jButton5 = new javax.swing.JButton();
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    jButton7 = new javax.swing.JButton();
    jLabel8 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    jButton13 = new javax.swing.JButton();
    jButton11 = new javax.swing.JButton();
    jButton14 = new javax.swing.JButton();
    jButton15 = new javax.swing.JButton();
    jLabel15 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel10 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel13 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
    javax.swing.JPanel jPanel3 = new javax.swing.JPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    spDivider.setModel(new javax.swing.SpinnerNumberModel());
    spDivider.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        spDividerStateChanged(evt);
      }
    });

    jButton8.setText("Inc");
    jButton8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton8ActionPerformed(evt);
      }
    });

    blinkPhase.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        blinkPhaseStateChanged(evt);
      }
    });

    jButton9.setText("Dec");
    jButton9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton9ActionPerformed(evt);
      }
    });

    jLabel6.setText("Cal");

    jButton10.setText("Read All");
    jButton10.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton10ActionPerformed(evt);
      }
    });

    ckKeyError.setText("Tastenfehler");

    spPWM.setModel(new javax.swing.SpinnerNumberModel(1, 1, 255, 1));
    spPWM.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        spPWMStateChanged(evt);
      }
    });

    jLabel5.setText("VCC");

    jButton4.setText("Read");
    jButton4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton4ActionPerformed(evt);
      }
    });

    jButton12.setText("Write volatile");
    jButton12.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton12ActionPerformed(evt);
      }
    });

    slPWM.setMajorTickSpacing(10);
    slPWM.setMaximum(255);
    slPWM.setMinimum(1);
    slPWM.setPaintTicks(true);
    slPWM.setValue(0);
    slPWM.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        slPWMStateChanged(evt);
      }
    });

    jLabel7.setText("Version:");

    jButton1.setText("Read");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton1ActionPerformed(evt);
      }
    });

    ledPanel.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        ledPanelStateChanged(evt);
      }
    });

    lbVersion.setText("jLabel8");

    jButton6.setText("Read");
    jButton6.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton6ActionPerformed(evt);
      }
    });

    jLabel3.setText("Phase");

    jLabel4.setText("PWM");

    jButton5.setText("Write");
    jButton5.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton5ActionPerformed(evt);
      }
    });

    ckKeyPressed.setText("Taste gedrückt");

    edVCC.setEditable(false);
    edVCC.setColumns(5);
    edVCC.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

    jLabel1.setText("LED");

    jButton7.setText("Write");
    jButton7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton7ActionPerformed(evt);
      }
    });

    blinkMask.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        blinkMaskStateChanged(evt);
      }
    });

    jLabel8.setText("Blinkteiler");

    jLabel2.setText("Blinken");

    jButton13.setText("Read");
    jButton13.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton13ActionPerformed(evt);
      }
    });

    jButton11.setText("Write All");
    jButton11.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton11ActionPerformed(evt);
      }
    });

    spCalibration.setModel(new javax.swing.SpinnerNumberModel((byte)0, null, null, (byte)1));

    jButton14.setText("Write default");
    jButton14.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton14ActionPerformed(evt);
      }
    });

    jButton15.setText("Read default");
    jButton15.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton15ActionPerformed(evt);
      }
    });

    jLabel15.setText("Debounce");

    spDebounce.setModel(new javax.swing.SpinnerNumberModel(1, 1, 20, 1));
    spDebounce.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        spDebounceStateChanged(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel5)
              .addComponent(jLabel6))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(edVCC)
              .addComponent(spCalibration, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jButton1)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5))))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slPWM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(ledPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(blinkMask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(jLabel3)
                  .addComponent(blinkPhase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spDivider, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(jButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(jButton9)
                  .addComponent(jButton7)))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(spPWM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton15))))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jButton10)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton11)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton12))
          .addComponent(ckKeyError)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(ckKeyPressed)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton13)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel15)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(spDebounce, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );

    jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton7, jButton9});

    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(jLabel2)
          .addComponent(jLabel3)
          .addComponent(jLabel7)
          .addComponent(lbVersion))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(ledPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(blinkMask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(blinkPhase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel8)
              .addComponent(spDivider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jButton6)
              .addComponent(jButton7))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jButton8)
              .addComponent(jButton9))))
        .addGap(18, 18, 18)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(slPWM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(spPWM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jButton14)
            .addComponent(jButton15))
          .addComponent(jLabel4))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(edVCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButton1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(spCalibration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButton4)
          .addComponent(jButton5))
        .addGap(26, 26, 26)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(ckKeyPressed)
          .addComponent(jButton13)
          .addComponent(jLabel15)
          .addComponent(spDebounce, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(ckKeyError)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton10)
          .addComponent(jButton11)
          .addComponent(jButton12))
        .addContainerGap())
    );

    jScrollPane1.setViewportView(jPanel1);

    jTabbedPane1.addTab("Low Level", jScrollPane1);

    jLabel9.setText("Modultyp");

    cbModuleType.setModel(typeModel);
    cbModuleType.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbModuleTypeActionPerformed(evt);
      }
    });

    btReadModuleState.setText("Read State");
    btReadModuleState.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btReadModuleStateActionPerformed(evt);
      }
    });

    jLabel10.setText("LedState");

    jLabel11.setText("Weiche");

    jLabel12.setText("3WW");

    jLabel13.setText("DKW");

    jLabel14.setText("Signal");

    cbTurnout.setModel(turnoutModel);
    cbTurnout.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbTurnoutActionPerformed(evt);
      }
    });

    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 0);
    flowLayout1.setAlignOnBaseline(true);
    jPanel3.setLayout(flowLayout1);

    buttonGroup1.add(rdLedOn);
    rdLedOn.setText("Ein");
    rdLedOn.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rdLedOnActionPerformed(evt);
      }
    });
    jPanel3.add(rdLedOn);

    buttonGroup1.add(rdBlink);
    rdBlink.setText("Blink");
    rdBlink.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rdBlinkActionPerformed(evt);
      }
    });
    jPanel3.add(rdBlink);

    buttonGroup1.add(rdLampTest);
    rdLampTest.setText("Lamp-Test");
    rdLampTest.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rdLampTestActionPerformed(evt);
      }
    });
    jPanel3.add(rdLampTest);

    buttonGroup1.add(rdLedOff);
    rdLedOff.setText("Aus");
    rdLedOff.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rdLedOffActionPerformed(evt);
      }
    });
    jPanel3.add(rdLedOff);

    cbThreeway.setModel(threewayModel);
    cbThreeway.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbThreewayActionPerformed(evt);
      }
    });

    cbCrossing.setModel(crossingModel);
    cbCrossing.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbCrossingActionPerformed(evt);
      }
    });

    cbSem.setModel(semModel);
    cbSem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbSemActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(cbModuleType, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(cbTurnout, 0, 210, Short.MAX_VALUE)
                .addComponent(cbThreeway, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbCrossing, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbSem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGap(0, 0, Short.MAX_VALUE))))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        .addContainerGap(530, Short.MAX_VALUE)
        .addComponent(btReadModuleState)
        .addGap(12, 12, 12))
    );

    jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cbModuleType, cbTurnout});

    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel9)
          .addComponent(cbModuleType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel10)
          .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel11)
          .addComponent(cbTurnout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel12)
          .addComponent(cbThreeway, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel13)
          .addComponent(cbCrossing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel14)
          .addComponent(cbSem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 175, Short.MAX_VALUE)
        .addComponent(btReadModuleState)
        .addContainerGap())
    );

    jTabbedPane1.addTab("Modul", jPanel2);

    getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    jTabbedPane1.getAccessibleContext().setAccessibleName("Modul");

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void ledPanelStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_ledPanelStateChanged
  {//GEN-HEADEREND:event_ledPanelStateChanged
    try {
      field.setLeds(ledPanel.getValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_ledPanelStateChanged

  private void spPWMStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spPWMStateChanged
  {//GEN-HEADEREND:event_spPWMStateChanged
    slPWM.setValue(((Number) spPWM.getValue()).intValue());
    try {
      field.setPWM(((Number) spPWM.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_spPWMStateChanged

  private void slPWMStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_slPWMStateChanged
  {//GEN-HEADEREND:event_slPWMStateChanged
    spPWM.setValue(slPWM.getValue());
  }//GEN-LAST:event_slPWMStateChanged

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
  {//GEN-HEADEREND:event_jButton1ActionPerformed
    try {
      edVCC.setValue(field.getVCC());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton1ActionPerformed

  private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton4ActionPerformed
  {//GEN-HEADEREND:event_jButton4ActionPerformed
    try {
      spCalibration.setValue(field.getCalibration());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton4ActionPerformed

  private void jButton5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton5ActionPerformed
  {//GEN-HEADEREND:event_jButton5ActionPerformed
    try {
      field.setCalibration(((Number) spCalibration.getValue()).byteValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton5ActionPerformed

  private void blinkMaskStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_blinkMaskStateChanged
  {//GEN-HEADEREND:event_blinkMaskStateChanged
    try {
      field.setBlinkMask(blinkMask.getValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_blinkMaskStateChanged

  private void blinkPhaseStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_blinkPhaseStateChanged
  {//GEN-HEADEREND:event_blinkPhaseStateChanged
    try {
      field.setBlinkPhase(blinkPhase.getValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_blinkPhaseStateChanged

  private void spDividerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spDividerStateChanged
  {//GEN-HEADEREND:event_spDividerStateChanged
    try {
      field.setBlinkDivider(((Number) spDivider.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_spDividerStateChanged

  private void jButton6ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton6ActionPerformed
  {//GEN-HEADEREND:event_jButton6ActionPerformed
    try {
      spDivider.setValue(field.getBlinkDivider());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton6ActionPerformed

  private void jButton7ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton7ActionPerformed
  {//GEN-HEADEREND:event_jButton7ActionPerformed
    try {
      field.setBlinkDivider(((Number) spDivider.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton7ActionPerformed

  private void jButton8ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton8ActionPerformed
  {//GEN-HEADEREND:event_jButton8ActionPerformed
    int val = ((Number) spDivider.getValue()).intValue();
    if (val < 255) {
      spDivider.setValue(val + 1);
    }
  }//GEN-LAST:event_jButton8ActionPerformed

  private void jButton9ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton9ActionPerformed
  {//GEN-HEADEREND:event_jButton9ActionPerformed
    int val = ((Number) spDivider.getValue()).intValue();
    if (val > 1) {
      spDivider.setValue(val - 1);
    }
  }//GEN-LAST:event_jButton9ActionPerformed

  private void jButton10ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton10ActionPerformed
  {//GEN-HEADEREND:event_jButton10ActionPerformed
    readAll();
  }//GEN-LAST:event_jButton10ActionPerformed

  private void jButton11ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton11ActionPerformed
  {//GEN-HEADEREND:event_jButton11ActionPerformed
    writeAll();
  }//GEN-LAST:event_jButton11ActionPerformed

  private void jButton12ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton12ActionPerformed
  {//GEN-HEADEREND:event_jButton12ActionPerformed
    writeVolatile();
  }//GEN-LAST:event_jButton12ActionPerformed

  private void jButton13ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton13ActionPerformed
  {//GEN-HEADEREND:event_jButton13ActionPerformed
    try {
      Set<State> state = field.getState();
      ckKeyPressed.setSelected(state.contains(State.KEY_PRESSED));
      ckKeyError.setSelected(state.contains(State.KEY_ERROR));
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton13ActionPerformed

  private void cbModuleTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbModuleTypeActionPerformed
  {//GEN-HEADEREND:event_cbModuleTypeActionPerformed
    if (!ignoreChangeEvent) {
      ModuleType selected = typeModel.getSelectedItem();
      if (selected != null && selected != ModuleType.UNKNOWN) {
        try {
          field.setModuleType(selected);
          btReadModuleStateActionPerformed(evt);
        } catch (IOException | TimeoutException | InterruptedException ex) {
          Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                     null,
                                                     ex);
        }
      }
    }
  }//GEN-LAST:event_cbModuleTypeActionPerformed

  private void jButton14ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton14ActionPerformed
  {//GEN-HEADEREND:event_jButton14ActionPerformed
    try {
      field.setDefaultPWM(((Number) spPWM.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton14ActionPerformed

  private void jButton15ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton15ActionPerformed
  {//GEN-HEADEREND:event_jButton15ActionPerformed
    try {
      spPWM.setValue(field.getDefaultPWM());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_jButton15ActionPerformed

  private void btReadModuleStateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btReadModuleStateActionPerformed
  {//GEN-HEADEREND:event_btReadModuleStateActionPerformed
    try {
      ModuleState ms = field.getModuleState();
      ignoreChangeEvent = true;
      setModuleState(ms);

    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    } finally {
      ignoreChangeEvent = false;
    }
  }//GEN-LAST:event_btReadModuleStateActionPerformed

  private void rdLedOffActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rdLedOffActionPerformed
  {//GEN-HEADEREND:event_rdLedOffActionPerformed
    writeLedState();
  }//GEN-LAST:event_rdLedOffActionPerformed

  private void rdLedOnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rdLedOnActionPerformed
  {//GEN-HEADEREND:event_rdLedOnActionPerformed
    writeLedState();
  }//GEN-LAST:event_rdLedOnActionPerformed

  private void rdLampTestActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rdLampTestActionPerformed
  {//GEN-HEADEREND:event_rdLampTestActionPerformed
    writeLedState();
  }//GEN-LAST:event_rdLampTestActionPerformed

  private void rdBlinkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rdBlinkActionPerformed
  {//GEN-HEADEREND:event_rdBlinkActionPerformed
    writeLedState();
  }//GEN-LAST:event_rdBlinkActionPerformed

  private void cbTurnoutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbTurnoutActionPerformed
  {//GEN-HEADEREND:event_cbTurnoutActionPerformed
    if (cbTurnout.isEnabled()) {
      writeModuleState();
    }
  }//GEN-LAST:event_cbTurnoutActionPerformed

  private void cbThreewayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbThreewayActionPerformed
  {//GEN-HEADEREND:event_cbThreewayActionPerformed
    if (cbThreeway.isEnabled()) {
      writeModuleState();
    }
  }//GEN-LAST:event_cbThreewayActionPerformed

  private void cbCrossingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbCrossingActionPerformed
  {//GEN-HEADEREND:event_cbCrossingActionPerformed
    if (cbCrossing.isEnabled()) {
      writeModuleState();
    }
  }//GEN-LAST:event_cbCrossingActionPerformed

  private void cbSemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbSemActionPerformed
  {//GEN-HEADEREND:event_cbSemActionPerformed
    if (cbSem.isEnabled()) {
      writeModuleState();
    }
  }//GEN-LAST:event_cbSemActionPerformed

  private void spDebounceStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spDebounceStateChanged
  {//GEN-HEADEREND:event_spDebounceStateChanged
    try {
      field.setDebounce(((Number) spDebounce.getValue()).intValue());
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_spDebounceStateChanged

  private int getModuleState()
  {
    if (cbTurnout.isEnabled()) {
      return turnoutModel.getSelectedItem().getMagic();
    } else if (cbThreeway.isEnabled()) {
      return threewayModel.getSelectedItem().getMagic();
    } else if (cbCrossing.isEnabled()) {
      return crossingModel.getSelectedItem().getMagic();
    } else if (cbSem.isEnabled()) {
      return semModel.getSelectedItem().getMagic();
    }
    return 0;
  }

  private LedState getLedState()
  {
    LedState s = LedState.OFF;
    if (rdBlink.isSelected()) {
      s = LedState.BLINK;
    } else if (rdLampTest.isSelected()) {
      s = LedState.LAMP_TEST;
    } else if (rdLedOff.isSelected()) {
      s = LedState.OFF;
    } else if (rdLedOn.isSelected()) {
      s = LedState.ON;
    }
    return s;
  }

  private void writeLedState()
  {
    LedState s = getLedState();
    ModuleState ms = ModuleState.valueOf(typeModel.getSelectedItem(),
                                         s,
                                         0);
    try {
      ignoreChangeEvent = true;
      field.setModuleState(ms);
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    } finally {
      ignoreChangeEvent = false;
    }
  }

  private void writeModuleState()
  {
    LedState ls = getLedState();
    int magic = getModuleState();
    ModuleState ms = ModuleState.valueOf(typeModel.getSelectedItem(),
                                         ls,
                                         magic);
    try {
      ignoreChangeEvent = true;
      field.setModuleState(ms);
    } catch (IOException | TimeoutException | InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    } finally {
      ignoreChangeEvent = false;
    }
  }

  private void setModuleState(ModuleState ms)
  {
    setModuleType(ms.getModuleType());
    rdBlink.setSelected(ms.getLedState() == LedState.BLINK);
    rdLampTest.setSelected(ms.getLedState() == LedState.LAMP_TEST);
    rdLedOff.setSelected(ms.getLedState() == LedState.OFF);
    rdLedOn.setSelected(ms.getLedState() == LedState.ON);
    if (cbCrossing.isEnabled()) {
      crossingModel.setSelectedItem(ms.getStateMagic());
    }
    if (cbSem.isEnabled()) {
      semModel.setSelectedItem(ms.getStateMagic());
    }
    if (cbThreeway.isEnabled()) {
      threewayModel.setSelectedItem(ms.getStateMagic());
    }
    if (cbTurnout.isEnabled()) {
      turnoutModel.setSelectedItem(ms.getStateMagic());
    }
  }

  private void setModuleType(ModuleType moduleType)
  {
    typeModel.setSelectedItem(moduleType);
    cbCrossing.setEnabled(moduleType == ModuleType.K1 || moduleType == ModuleType.K2);
    cbSem.setEnabled(moduleType == ModuleType.SEM_E || moduleType == ModuleType.SEM_W);
    cbThreeway.setEnabled(moduleType == ModuleType.DW1 || moduleType == ModuleType.DW2 || moduleType == ModuleType.DW3);
    cbTurnout.setEnabled(moduleType == ModuleType.W1 || moduleType == ModuleType.W2 || moduleType == ModuleType.W3 || moduleType
                                                                                                                              == ModuleType.W4);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE,
                                                                   null,
                                                                   ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE,
                                                                   null,
                                                                   ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE,
                                                                   null,
                                                                   ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE,
                                                                   null,
                                                                   ex);
    }
    //</editor-fold>
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> {
      try {
        new Main().setVisible(true);
      } catch (PortInUseException | UnsupportedCommOperationException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                   null,
                                                   ex);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final at.or.reder.tools.fieldtest.LedPanel blinkMask = new at.or.reder.tools.fieldtest.LedPanel();
  private final at.or.reder.tools.fieldtest.LedPanel blinkPhase = new at.or.reder.tools.fieldtest.LedPanel();
  private final javax.swing.JButton btReadModuleState = new javax.swing.JButton();
  private javax.swing.ButtonGroup buttonGroup1;
  private final javax.swing.JComboBox<CrossingState> cbCrossing = new javax.swing.JComboBox<>();
  private final javax.swing.JComboBox<at.or.reder.tools.fieldtest.model.ModuleType> cbModuleType = new javax.swing.JComboBox<>();
  private final javax.swing.JComboBox<SemaphoreState> cbSem = new javax.swing.JComboBox<>();
  private final javax.swing.JComboBox<ThreewayState> cbThreeway = new javax.swing.JComboBox<>();
  private final javax.swing.JComboBox<TurnoutState> cbTurnout = new javax.swing.JComboBox<>();
  private final javax.swing.JCheckBox ckKeyError = new javax.swing.JCheckBox();
  private final javax.swing.JCheckBox ckKeyPressed = new javax.swing.JCheckBox();
  private final javax.swing.JFormattedTextField edVCC = new javax.swing.JFormattedTextField();
  private final javax.swing.JButton jButton1 = new javax.swing.JButton();
  private javax.swing.JButton jButton10;
  private javax.swing.JButton jButton11;
  private javax.swing.JButton jButton12;
  private javax.swing.JButton jButton13;
  private javax.swing.JButton jButton14;
  private javax.swing.JButton jButton15;
  private javax.swing.JButton jButton4;
  private javax.swing.JButton jButton5;
  private javax.swing.JButton jButton6;
  private javax.swing.JButton jButton7;
  private javax.swing.JButton jButton8;
  private javax.swing.JButton jButton9;
  private javax.swing.JLabel jLabel15;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTabbedPane jTabbedPane1;
  private final javax.swing.JLabel lbVersion = new javax.swing.JLabel();
  private final at.or.reder.tools.fieldtest.LedPanel ledPanel = new at.or.reder.tools.fieldtest.LedPanel();
  private final javax.swing.JRadioButton rdBlink = new javax.swing.JRadioButton();
  private final javax.swing.JRadioButton rdLampTest = new javax.swing.JRadioButton();
  private final javax.swing.JRadioButton rdLedOff = new javax.swing.JRadioButton();
  private final javax.swing.JRadioButton rdLedOn = new javax.swing.JRadioButton();
  private final javax.swing.JSlider slPWM = new javax.swing.JSlider();
  private final javax.swing.JSpinner spCalibration = new javax.swing.JSpinner();
  private final javax.swing.JSpinner spDebounce = new javax.swing.JSpinner();
  private final javax.swing.JSpinner spDivider = new javax.swing.JSpinner();
  private final javax.swing.JSpinner spPWM = new javax.swing.JSpinner();
  // End of variables declaration//GEN-END:variables
}
