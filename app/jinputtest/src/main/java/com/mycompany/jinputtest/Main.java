/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.jinputtest;

import at.or.reder.controller.PSController;
import at.or.reder.controller.PSController.Axis;
import at.or.reder.controller.PSControllerEvent;
import at.or.reder.controller.PSDirection;
import at.or.reder.controller.impl.PSControllerFactoryImpl;
import at.or.reder.dcc.Controller;
import at.or.reder.dcc.ControllerProvider;
import at.or.reder.dcc.Locomotive;
import at.or.reder.dcc.LocomotiveFuncEvent;
import at.or.reder.dcc.LocomotiveSpeedEvent;
import at.or.reder.dcc.LocomotiveTachoEvent;
import at.or.reder.tools.edk750.EDK750;
import at.or.reder.zcan20.MX10PropertiesSet;
import at.or.reder.zcan20.ZCAN;
import at.or.reder.zcan20.impl.MX10ControllerProvider;
import at.or.reder.zcan20.packet.Packet;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/* EDK 750
F0 -> Licht vorne/hinten  -> Y (Quadrat)
F1 -> Sound ein -> B (Kreis)
F2 -> Fahren / Drehen
      Fahren vorwärts L2, rück R2
      Drehen RX
F3 -> Seilwinde Heben F28==1/F27==1/F27==0/F28==0 -> RY+
F4 -> Ausleiger ausfahren F26==1/F25==1/F25==0/F26==0 -> LY-
F5 -> Ausleger einfahren F26==1/F25==1/F25==0/F26==0 -> LY+
F6 -> Seilwinde Senken F28==1/F27==1/F27==0/F28==0 -> RY-
F7 -> Ausleger senken F24==1/F23==1/F23==0/F24==0 -> LX-
F8 -> Ausleger heben F24==1/F23==1/F23==0/F24==0 -> LX+
F9 -> Halbgeschwindigkeit F3-F8
F10 -> Arbeitslicht -> X (Dreieck)
F11 -> Hinteres Licht aus -> (Options)
F12 -> Horn kurz -> Mode (PS)
F13 -> Horn dauerhaft (R1)
F14 -> Mute A (Kreuz)
F15 -> Horn sehr kurz (L1)
F16 -> Kompressor (North)
F17 -> Weichenknarren (South)
F18 -> in Arbeitsstellung Fahren (West)
F19 -> in Transportstellung Fahren (East)
F20 -> Endlagenabschaltung
Touchpad klick -> Strom aus
Share -> Strom ein
 */
/**
 *
 * @author wolfi
 */
public class Main extends javax.swing.JFrame
{

  public static final Set<String> ids = Set.of("ev:0003:054c:09cc:8111",
                                               "ev:0005:054c:09cc:8001");
  private PSController controller;
  private final PSControllerFactoryImpl factory;
  private final PropertyChangeListener controllerChangeListener = this::onControllerPropertyChange;
  private final Controller mx10;
  private final EDK750 edk;

  public Main() throws IOException, TimeoutException
  {
    factory = new PSControllerFactoryImpl();
    initComponents();
    factory.addPSControllerEventListener(this::onPSController);
    factory.startScanning();
    mx10 = connect2MX10();
    mx10.addLocomotiveSpeedEventListener(2062,
                                         this::onLocoSpeed);
    mx10.addLocomotiveFuncEventListener(2062,
                                        this::onLocoFunc);
    mx10.addLocomotiveTachoEventListener(2062,
                                         this::onLocoTacho);
    ZCAN zimo = mx10.getLookup().lookup(ZCAN.class);
//    zimo.addPacketListener(createSimplePacketMatcher(2062),
//                           this::onPacket);
    Locomotive edkLoc = mx10.getLocomotive(750);
    Map<String, String> config = new HashMap<>();
    edk = new EDK750(edkLoc,
                     controller,
                     config);
  }

  private Predicate<Packet> createSimplePacketMatcher(int address)
  {
    return (Packet p) -> p.getData().getShort(0) == (short) address
                                 && p.getCommandGroup().getMagic() == (byte) 0x18;
  }

  private ControllerProvider findMX10Provider()
  {
    Collection<? extends ControllerProvider> providerList = Lookup.getDefault().lookupAll(ControllerProvider.class);
    for (ControllerProvider p : providerList) {
      if (p.getId().equals(MX10ControllerProvider.ID)) {
        return p;
      }
    }
    return null;
  }

  private Controller connect2MX10()
  {
    Controller result = null;
    try {
      ControllerProvider mx10Provider = findMX10Provider();
      result = mx10Provider.createController(Map.of(MX10PropertiesSet.PROP_HOST,
                                                    "192.168.1.145"));
      result.open();
    } catch (IllegalArgumentException | IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return result;
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

  private void onControllerPropertyChange(PropertyChangeEvent evt)
  {
    switch (evt.getPropertyName()) {
      case PSController.PROP_LEFT: {
        Point p = controller.getLeft();
        Point2D.Float p2 = new Point2D.Float((float) (p.x) / (float) controller.getRangeMax(Axis.LX),
                                             (float) p.y / (float) controller.getRangeMax(Axis.LY));
        leftCross.setPosition(p2);
        leftCross.repaint();
      }
      break;
      case PSController.PROP_RIGHT: {
        Point p = controller.getRight();
        float fmx = controller.getRangeMax(Axis.RX);
        Point2D.Float p2 = new Point2D.Float((float) (p.x) / fmx,
                                             (float) p.y / (float) controller.getRangeMax(Axis.RY));
        rightCross.setPosition(p2);
        rightCross.repaint();
      }
      break;
      case PSController.PROP_APRESSED: {
        boolean en = (Boolean) evt.getNewValue();
        btnA.setSelected(en);
      }
      break;
      case PSController.PROP_BPRESSED: {
        boolean en = (Boolean) evt.getNewValue();
        btnB.setSelected(en);
      }
      break;
      case PSController.PROP_XPRESSED: {
        boolean en = (Boolean) evt.getNewValue();
        btnX.setSelected(en);
      }
      break;
      case PSController.PROP_YPRESSED: {
        boolean en = (Boolean) evt.getNewValue();
        btnY.setSelected(en);
      }
      break;

      case PSController.PROP_DIRECTION: {
        PSDirection dir = (PSDirection) evt.getNewValue();
        povN.setSelected(dir.isNorth());
        povE.setSelected(dir.isEast());
        povW.setSelected(dir.isWest());
        povS.setSelected(dir.isSouth());
      }
      break;
      case PSController.PROP_L1PRESSED: {
        boolean en = (Boolean) evt.getNewValue();
        btLeftThumb.setSelected(en);
      }
      break;
      case PSController.PROP_L2LEVEL: {
        z.setValue(controller.getL2Level());
      }
      break;
      case PSController.PROP_L2PRESSED:
        btLeftThumb2.setSelected(controller.isL2Pressed());
        break;
      case PSController.PROP_LEFTPRESSED:
        btLeftThumb3.setSelected(controller.isLeftPressed());
        break;
      case PSController.PROP_MODEPRESSED:
        btnMode.setSelected(controller.isModePressed());
        break;
      case PSController.PROP_R1PRESSED:
        btRightThumb.setSelected(controller.isR1Pressed());
        break;
      case PSController.PROP_R2LEVEL:
        rz.setValue(controller.getR2Level());
        break;
      case PSController.PROP_R2PRESSED:
        btRightThumb2.setSelected(controller.isR2Pressed());
        break;
      case PSController.PROP_RIGHTPRESSED:
        btRightThumb3.setSelected(controller.isRightPressed());
        break;
      case PSController.PROP_SELECTPRESSED:
        btnSelect.setSelected(controller.isSelectPressed());
        break;
      case PSController.PROP_STARTPRESSED:
        btnStart.setSelected(controller.isStartPressed());
        break;
      default:
        System.err.println("Unknown property " + evt.getPropertyName());
    }
  }

  private void connect(PSController c)
  {
    try {
      factory.stopScanning();
      controller = c;
      edk.setController(controller);
      edk.setControllerId(controller.getId());
      controller.addPropertyChangeListener(controllerChangeListener);
      slRX.setValue((int) (controller.getDeadRange(Axis.RX) * 100));
      slRY.setValue((int) (controller.getDeadRange(Axis.RY) * 100));
      spRangeMax.setValue(controller.getRangeMax(Axis.RX));
      ckLedOn.setSelected(controller.isLedOn());
      btLedColor.setBackground(controller.getColor());
    } catch (IOException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }

  private void disconnect()
  {
    if (controller != null) {
      edk.setController(null);
      controller.removePropertyChangeListener(controllerChangeListener);
    }
    controller = null;
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

    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel3 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    povN = new javax.swing.JToggleButton();
    povE = new javax.swing.JToggleButton();
    povW = new javax.swing.JToggleButton();
    povS = new javax.swing.JToggleButton();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    btnX = new javax.swing.JToggleButton();
    btnB = new javax.swing.JToggleButton();
    btnY = new javax.swing.JToggleButton();
    btnA = new javax.swing.JToggleButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        formWindowClosing(evt);
      }
    });

    btRightThumb.setText("RT");

    btnStart.setText("Start");

    btRightThumb3.setText("RT3");

    jPanel1.setLayout(new java.awt.GridBagLayout());

    povN.setText("N");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel1.add(povN, gridBagConstraints);

    povE.setText("E");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel1.add(povE, gridBagConstraints);

    povW.setText("W");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel1.add(povW, gridBagConstraints);

    povS.setText("S");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel1.add(povS, gridBagConstraints);

    btLeftThumb2.setText("LT2");

    jLabel1.setText("RX");

    slRX.setMajorTickSpacing(10);
    slRX.setPaintTicks(true);
    slRX.setValue(0);
    slRX.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        slRXStateChanged(evt);
      }
    });

    z.setMajorTickSpacing(64);
    z.setMaximum(1024);
    z.setOrientation(javax.swing.JSlider.VERTICAL);
    z.setPaintTicks(true);
    z.setPaintTrack(false);
    z.setValue(0);
    z.setEnabled(false);

    btRightThumb2.setText("RT2");

    rz.setMajorTickSpacing(64);
    rz.setMaximum(1024);
    rz.setOrientation(javax.swing.JSlider.VERTICAL);
    rz.setPaintTicks(true);
    rz.setPaintTrack(false);
    rz.setValue(0);
    rz.setEnabled(false);

    rightCross.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

    jLabel2.setText("RY");

    btLeftThumb.setText("LT");

    slRY.setMajorTickSpacing(10);
    slRY.setPaintTicks(true);
    slRY.setValue(0);
    slRY.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        slRYStateChanged(evt);
      }
    });

    leftCross.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

    btnSelect.setText("Select");

    btnMode.setText("Mode");

    jPanel2.setLayout(new java.awt.GridBagLayout());

    btnX.setText("X");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel2.add(btnX, gridBagConstraints);

    btnB.setText("B");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel2.add(btnB, gridBagConstraints);

    btnY.setText("Y");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel2.add(btnY, gridBagConstraints);

    btnA.setText("A");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.3333333333333333;
    gridBagConstraints.weighty = 0.3333333333333333;
    jPanel2.add(btnA, gridBagConstraints);

    spRangeMax.setModel(new javax.swing.SpinnerNumberModel(125, 10, 1023, 1));
    spRangeMax.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        spRangeMaxStateChanged(evt);
      }
    });

    btLedColor.setText("Led");
    btLedColor.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btLedColorActionPerformed(evt);
      }
    });

    btLeftThumb3.setText("LT3");

    ckLedOn.setText("Led ein/aus");
    ckLedOn.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        ckLedOnActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(332, 332, 332))
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(53, 53, 53)
                    .addComponent(btLedColor)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(ckLedOn)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                      .addComponent(jLabel1)
                      .addComponent(jLabel2))
                    .addGap(1, 1, 1)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                      .addComponent(slRY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(slRX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spRangeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                  .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(204, 204, 204)
                    .addComponent(btnMode, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(leftCross, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btLeftThumb, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btRightThumb, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btLeftThumb2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btRightThumb2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btLeftThumb3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btRightThumb3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(rz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightCross, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(btLeftThumb2)
              .addComponent(btRightThumb2))
            .addGap(17, 17, 17)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(btRightThumb)
              .addComponent(btLeftThumb))
            .addGap(17, 17, 17)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(btRightThumb3)
              .addComponent(btLeftThumb3)))
          .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
            .addComponent(z, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rightCross, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(leftCross, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGap(18, 18, 18)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btnSelect)
              .addComponent(btnStart))
            .addGap(25, 25, 25)
            .addComponent(btnMode)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(slRY, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btLedColor, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(ckLedOn)
                .addComponent(jLabel1))
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(spRangeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(slRX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        .addContainerGap())
    );

    jTabbedPane1.addTab("PS Controller", jPanel3);

    getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void ckLedOnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ckLedOnActionPerformed
  {//GEN-HEADEREND:event_ckLedOnActionPerformed
    if (controller != null) {
      try {
        controller.setLedOn(ckLedOn.isSelected());
      } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                   null,
                                                   ex);
      }
    }
  }//GEN-LAST:event_ckLedOnActionPerformed

  private void btLedColorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btLedColorActionPerformed
  {//GEN-HEADEREND:event_btLedColorActionPerformed
    try {
      Color newColor = JColorChooser.showDialog(this,
                                                "Farbe",
                                                controller.getColor());
      controller.setColor(newColor);
      btLedColor.setBackground(newColor);
    } catch (IOException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                                 null,
                                                 ex);
    }
  }//GEN-LAST:event_btLedColorActionPerformed

  private void slRXStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_slRXStateChanged
  {//GEN-HEADEREND:event_slRXStateChanged
    float tmp = slRX.getValue();
    controller.setDeadRange(Axis.RX,
                            tmp / 100f);
  }//GEN-LAST:event_slRXStateChanged

  private void slRYStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_slRYStateChanged
  {//GEN-HEADEREND:event_slRYStateChanged
    float tmp = slRY.getValue();
    controller.setDeadRange(Axis.RY,
                            tmp / 100f);
  }//GEN-LAST:event_slRYStateChanged

  private void spRangeMaxStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spRangeMaxStateChanged
  {//GEN-HEADEREND:event_spRangeMaxStateChanged
    controller.setRangeMax(Axis.RX,
                           ((Number) spRangeMax.getValue()).intValue());
  }//GEN-LAST:event_spRangeMaxStateChanged

  private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
  {//GEN-HEADEREND:event_formWindowClosing
    if (mx10 != null) {
      try {
        mx10.close();
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }//GEN-LAST:event_formWindowClosing

  private final AtomicInteger eventCounter = new AtomicInteger();
  private final Set<Byte> toIgnore = Set.of((byte) 1,
                                            (byte) 8,
                                            (byte) 0x10);

  private void onPacket(ZCAN zcan,
                        Packet packet)
  {
    ByteBuffer data = packet.getData();
    byte selector = data.get(3);
    if (!toIgnore.contains(selector)) {
      int b = data.get(4) & 0xff;
      System.err.println(MessageFormat.format("{0}:{1} {2}",
                                              new Object[]{eventCounter.incrementAndGet(), packet.toString(), b}));
    }
  }

  int lastTacho = -1;

  private void onLocoTacho(LocomotiveTachoEvent evt)
  {
    if (evt.isSpeedSet()) {
      if (lastTacho == -1 || evt.getSpeed() != lastTacho) {
        lastTacho = evt.getSpeed();
        System.err.println(MessageFormat.format("{2,number,0}:Locomotive {0,number,0}: Tacho {1} km/h",
                                                new Object[]{evt.getDecoder(), evt.getSpeed(), eventCounter.
                                                             incrementAndGet()}));
      }
    }
    if (evt.isDirectionSet()) {
      System.err.println(MessageFormat.format("{3,number,0}:Locomotive {0,number,0}: Direction {1}, Pending {2} ",
                                              new Object[]{evt.getDecoder(), evt.getDirection(), evt.isDirectionPending(),
                                                           eventCounter.incrementAndGet()}));

    }
    if (evt.isVoltageSet()) {
      System.err.println(MessageFormat.format("{2,number,0}:Locomotive {0,number,0}: Voltage {1,number,0.0}V ",
                                              new Object[]{evt.getDecoder(), evt.getVoltage(),
                                                           eventCounter.incrementAndGet()}));

    }
  }

  private void onLocoSpeed(LocomotiveSpeedEvent evt)
  {
    System.err.println(MessageFormat.format("{3,number,0}:Locomotive {0,number,0}: Speed: {1}, Direction {2}",
                                            new Object[]{evt.getDecoder(), evt.getSpeed(), evt.getDirection(), eventCounter.
                                                         incrementAndGet()}));
  }

  private void onLocoFunc(LocomotiveFuncEvent evt)
  {
    System.err.println(MessageFormat.format("{3,number,0}:Locomotive {0,number,0}: Function: {1}, Value {2}",
                                            new Object[]{evt.getDecoder(), evt.getFuncNr(), evt.getFuncValue(), eventCounter.
                                                         incrementAndGet()}));
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    File f = new File("./logging.properties");
    boolean loggerInitialized = false;
    if (f.canRead()) {
      try (InputStream is = new FileInputStream(f)) {
        LogManager.getLogManager().readConfiguration(is);
        loggerInitialized = true;
      } catch (IOException ex) {
      }
    }
    if (!loggerInitialized) {
      try (InputStream is = Main.class
              .getResourceAsStream("/logging.properties")) {
        if (is
                    != null) {
          LogManager.getLogManager().readConfiguration(is);
        }

      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }

//    /* Set the Nimbus look and feel */
//    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
//     */
//    try {
//      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//        if ("Nimbus".equals(info.getName())) {
//          javax.swing.UIManager.setLookAndFeel(info.getClassName());
//          break;
//        }
//      }
//    } catch (ClassNotFoundException ex) {
//      java.util.logging.Logger.getLogger(Main.class.getName()).log(
//              java.util.logging.Level.SEVERE,
//              null,
//              ex);
//    } catch (InstantiationException ex) {
//      java.util.logging.Logger.getLogger(Main.class.getName()).log(
//              java.util.logging.Level.SEVERE,
//              null,
//              ex);
//    } catch (IllegalAccessException ex) {
//      java.util.logging.Logger.getLogger(Main.class.getName()).log(
//              java.util.logging.Level.SEVERE,
//              null,
//              ex);
//    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//      java.util.logging.Logger.getLogger(Main.class.getName()).log(
//              java.util.logging.Level.SEVERE,
//              null,
//              ex);
//    }
//    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> {
      try {
        new Main().setVisible(true);
      } catch (IOException | TimeoutException ex) {
        Exceptions.printStackTrace(ex);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final javax.swing.JButton btLedColor = new javax.swing.JButton();
  private final javax.swing.JToggleButton btLeftThumb = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btLeftThumb2 = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btLeftThumb3 = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btRightThumb = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btRightThumb2 = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btRightThumb3 = new javax.swing.JToggleButton();
  private javax.swing.JToggleButton btnA;
  private javax.swing.JToggleButton btnB;
  private final javax.swing.JToggleButton btnMode = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btnSelect = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btnStart = new javax.swing.JToggleButton();
  private javax.swing.JToggleButton btnX;
  private javax.swing.JToggleButton btnY;
  private final javax.swing.JCheckBox ckLedOn = new javax.swing.JCheckBox();
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JTabbedPane jTabbedPane1;
  private final com.mycompany.jinputtest.DualAxisPanel leftCross = new com.mycompany.jinputtest.DualAxisPanel();
  private javax.swing.JToggleButton povE;
  private javax.swing.JToggleButton povN;
  private javax.swing.JToggleButton povS;
  private javax.swing.JToggleButton povW;
  private final com.mycompany.jinputtest.DualAxisPanel rightCross = new com.mycompany.jinputtest.DualAxisPanel();
  private final javax.swing.JSlider rz = new javax.swing.JSlider();
  private final javax.swing.JSlider slRX = new javax.swing.JSlider();
  private final javax.swing.JSlider slRY = new javax.swing.JSlider();
  private final javax.swing.JSpinner spRangeMax = new javax.swing.JSpinner();
  private final javax.swing.JSlider z = new javax.swing.JSlider();
  // End of variables declaration//GEN-END:variables
}
