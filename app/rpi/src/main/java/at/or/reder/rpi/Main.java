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

import at.or.reder.dcc.Controller;
import at.or.reder.dcc.ControllerProvider;
import at.or.reder.dcc.LinkState;
import at.or.reder.dcc.LinkStateListener;
import at.or.reder.dcc.PropertySet;
import at.or.reder.zcan20.MX10PropertiesSet;
import java.awt.CardLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Wolfgang Reder
 */
public final class Main extends JFrame implements ContainerListener
{

  public static final Logger LOGGER = Logger.getLogger("at.or.reder.rpi");
  private static final String PROP_LAYOUT = "layout";
  private static final String LAYOUT_DASHBOARD = "dashboard";
  private static final String LAYOUT_POWER = "power";
  private static final String LAYOUT_DRIVE = "drive";
  private static final String LAYOUT_EDK = "edk";

  private static final String LAYOUT_SWITCH = "switch";
  private static final String LAYOUT_SETTINGS = "settings";
  private static final String LAYOUT_PROGRAM = "program";
  private Controller device;
  private final List<DevicePanel> devicePanels = new ArrayList<>();
  private final CardLayout cardLayout;
  private final LinkStateListener linkListener = this::onLinkConnectionChanged;

  public Main(boolean undecorated) throws IOException
  {
    setUndecorated(undecorated);
    cardPanel.addContainerListener(this);
    initComponents();
    btEDK.putClientProperty(PROP_LAYOUT,
                            LAYOUT_EDK);
    cardLayout = (CardLayout) cardPanel.getLayout();
    setBounds(0,
              0,
              800,
              480);
    tryReconnect();
    showSelectedPanel(btDashboard);
    Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
  }

  private void onLinkConnectionChanged(Controller device,
                                       LinkState state)
  {
    if (state == LinkState.BROKEN) {
      RequestProcessor.getDefault().schedule(this::tryReconnect,
                                             5000,
                                             TimeUnit.MILLISECONDS);
    }
  }

  private String getHostName()
  {
    String os = System.getProperty("os.name").toLowerCase();
    String result = null;
    if (os.contains("win")) {
      result = System.getenv("COMPUTERNAME");
      if (result == null || result.isBlank()) {
        result = execReadToString("hostname");
      }
    } else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
      result = System.getenv("HOSTNAME");
      if (result == null || result.isBlank()) {
        result = execReadToString("hostname");
      }
      if (result == null || result.isBlank()) {
        result = execReadToString("cat /etc/hostname");
      }
    }
    return result;
  }

  private static String execReadToString(String execCommand)
  {
    try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
      return s.hasNext() ? s.next() : "";
    } catch (IOException ex) {
      LOGGER.log(Level.WARNING,
                 ex,
                 () -> "Cannot execute " + execCommand);
    }
    return null;
  }

  private void tryReconnect()
  {
    doReconnect();
    if (device.getLinkState() != LinkState.CONNECTED) {
      RequestProcessor.getDefault().schedule(this::tryReconnect,
                                             5000,
                                             TimeUnit.MILLISECONDS);
    }
  }

  private ControllerProvider getMX10ControllerProvider()
  {
    UUID mx10 = UUID.fromString("761e3de9-d8b0-46ee-b44b-738671f15b88");
    for (ControllerProvider provider : Lookup.getDefault().lookupAll(ControllerProvider.class)) {
      if (provider.getId().equals(mx10)) {
        return provider;
      }
    }
    return null;
  }

  public void connect()
  {
    try {
      disconnect();
      if (device == null) {
        ControllerProvider provider = getMX10ControllerProvider();
        PropertySet set = provider.getPropertySet();
        Map<String, String> props = set.getDefaultProperties();
//        props.put(MX10PropertiesSet.PROP_HOST,
//                  "230.1.1.1");
//        props.put(MX10PropertiesSet.PROP_OUTPORT,
//                  "14373");
        props.put(MX10PropertiesSet.PROP_APPNAME,
                  getHostName());
        device = provider.createController(props);
        for (DevicePanel p : devicePanels) {
          p.setDevice(device);
        }
//      device.getPowerStateInfo(EnumSet.of(PowerPort.OUT_1,
//                                          PowerPort.OUT_2));
        device.addLinkStateListener(linkListener);
      }
      device.open();
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  public void disconnect()
  {
    if (device != null) {
      try {
        device.close();
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  private void doReconnect()
  {
    disconnect();
    connect();
  }

  private void showSelectedPanel(JComponent button)
  {
    String clientKey = (String) button.getClientProperty(PROP_LAYOUT);
    if (clientKey != null) {
      cardLayout.show(cardPanel,
                      clientKey);
    }
  }

  @Override
  public void componentAdded(ContainerEvent e)
  {
    if (e.getChild() instanceof DevicePanel) {
      devicePanels.add((DevicePanel) e.getChild());
    }
  }

  @Override
  public void componentRemoved(ContainerEvent e)
  {
    if (e.getChild() instanceof DevicePanel) {
      devicePanels.remove((DevicePanel) e.getChild());
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
   * this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings(
          "unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    trafficLight1 = new eu.hansolo.steelseries.extras.TrafficLight();
    buttonGroup1 = new javax.swing.ButtonGroup();
    jPanel1 = new javax.swing.JPanel();
    dashboardPanel1 = new at.or.reder.rpi.DashboardPanel();
    energyPanel1 = new at.or.reder.rpi.EnergyPanel();
    drivePanel1 = new at.or.reder.rpi.DrivePanel();
    switchPanel1 = new at.or.reder.rpi.SwitchPanel();
    programPanel1 = new at.or.reder.rpi.ProgramPanel();
    settigsPanel1 = new at.or.reder.rpi.SettigsPanel();
    eDKPanel1 = new at.or.reder.rpi.edk.EDKPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        formWindowClosing(evt);
      }
    });

    jPanel1.setMinimumSize(new java.awt.Dimension(894, 35));
    jPanel1.setPreferredSize(new java.awt.Dimension(894, 35));
    jPanel1.setLayout(new java.awt.GridLayout(1, 0));

    buttonGroup1.add(btDashboard);
    btDashboard.putClientProperty(PROP_LAYOUT,LAYOUT_DASHBOARD);
    btDashboard.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btDashboard.setSelected(true);
    btDashboard.setText("Übersicht");
    btDashboard.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btDashboardActionPerformed(evt);
      }
    });
    jPanel1.add(btDashboard);

    buttonGroup1.add(btEnergy);
    btEnergy.putClientProperty(PROP_LAYOUT,LAYOUT_POWER);
    btEnergy.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btEnergy.setText("Energie");
    btEnergy.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btEnergyActionPerformed(evt);
      }
    });
    jPanel1.add(btEnergy);

    buttonGroup1.add(btDrive);
    btDrive.putClientProperty(PROP_LAYOUT,LAYOUT_DRIVE);
    btDrive.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btDrive.setText("Fahren");
    btDrive.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btDriveActionPerformed(evt);
      }
    });
    jPanel1.add(btDrive);

    buttonGroup1.add(btEDK);
    btEDK.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btEDK.setText("EDK 750");
    btEDK.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btEDKActionPerformed(evt);
      }
    });
    jPanel1.add(btEDK);

    buttonGroup1.add(btSwitch);
    btSwitch.putClientProperty(PROP_LAYOUT,LAYOUT_SWITCH);
    btSwitch.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btSwitch.setText("Schalten");
    btSwitch.setMargin(new java.awt.Insets(14, 14, 14, 14));
    btSwitch.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btSwitchActionPerformed(evt);
      }
    });
    jPanel1.add(btSwitch);

    buttonGroup1.add(btProgram);
    btProgram.putClientProperty(PROP_LAYOUT,LAYOUT_PROGRAM);
    btProgram.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btProgram.setText("Programmieren");
    btProgram.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btProgramActionPerformed(evt);
      }
    });
    jPanel1.add(btProgram);

    buttonGroup1.add(btSettings);
    btSettings.putClientProperty(PROP_LAYOUT,LAYOUT_SETTINGS);
    btSettings.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
    btSettings.setText("Einstellungen");
    btSettings.setMargin(new java.awt.Insets(14, 14, 14, 14));
    btSettings.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btSettingsActionPerformed(evt);
      }
    });
    jPanel1.add(btSettings);

    getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

    cardPanel.setLayout(new java.awt.CardLayout());
    cardPanel.add(dashboardPanel1, "dashboard");
    cardPanel.add(energyPanel1, "power");
    cardPanel.add(drivePanel1, "drive");
    cardPanel.add(switchPanel1, "switch");
    cardPanel.add(programPanel1, "program");
    cardPanel.add(settigsPanel1, "settings");
    cardPanel.add(eDKPanel1, "edk");

    getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

    setLocation(new java.awt.Point(0, 0));
  }// </editor-fold>//GEN-END:initComponents

  private void btDashboardActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btDashboardActionPerformed
  {//GEN-HEADEREND:event_btDashboardActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btDashboardActionPerformed

  private void btEnergyActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btEnergyActionPerformed
  {//GEN-HEADEREND:event_btEnergyActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btEnergyActionPerformed

  private void btDriveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btDriveActionPerformed
  {//GEN-HEADEREND:event_btDriveActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btDriveActionPerformed

  private void btSwitchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSwitchActionPerformed
  {//GEN-HEADEREND:event_btSwitchActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btSwitchActionPerformed

  private void btProgramActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btProgramActionPerformed
  {//GEN-HEADEREND:event_btProgramActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btProgramActionPerformed

  private void btSettingsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSettingsActionPerformed
  {//GEN-HEADEREND:event_btSettingsActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btSettingsActionPerformed

  private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
  {//GEN-HEADEREND:event_formWindowClosing
    disconnect();
  }//GEN-LAST:event_formWindowClosing

  private void btEDKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btEDKActionPerformed
  {//GEN-HEADEREND:event_btEDKActionPerformed
    showSelectedPanel((JComponent) evt.getSource());
  }//GEN-LAST:event_btEDKActionPerformed

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
      try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
        if (is != null) {
          LogManager.getLogManager().readConfiguration(is);
        }
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    final AtomicBoolean undekorated = new AtomicBoolean(true);
    for (String a : args) {
      if ("-u-".equals(a)) {
        undekorated.set(false);
      }
    }
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
      java.util.logging.Logger.getLogger(Main.class
              .getName()).log(java.util.logging.Level.SEVERE,
                              null,
                              ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(Main.class
              .getName()).log(java.util.logging.Level.SEVERE,
                              null,
                              ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(Main.class
              .getName()).log(java.util.logging.Level.SEVERE,
                              null,
                              ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(Main.class
              .getName()).log(java.util.logging.Level.SEVERE,
                              null,
                              ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(() -> {
      try {
        new Main(undekorated.get()).setVisible(true);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private final javax.swing.JToggleButton btDashboard = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btDrive = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btEDK = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btEnergy = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btProgram = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btSettings = new javax.swing.JToggleButton();
  private final javax.swing.JToggleButton btSwitch = new javax.swing.JToggleButton();
  private javax.swing.ButtonGroup buttonGroup1;
  private final javax.swing.JPanel cardPanel = new javax.swing.JPanel();
  private at.or.reder.rpi.DashboardPanel dashboardPanel1;
  private at.or.reder.rpi.DrivePanel drivePanel1;
  private at.or.reder.rpi.edk.EDKPanel eDKPanel1;
  private at.or.reder.rpi.EnergyPanel energyPanel1;
  private javax.swing.JPanel jPanel1;
  private at.or.reder.rpi.ProgramPanel programPanel1;
  private at.or.reder.rpi.SettigsPanel settigsPanel1;
  private at.or.reder.rpi.SwitchPanel switchPanel1;
  private eu.hansolo.steelseries.extras.TrafficLight trafficLight1;
  // End of variables declaration//GEN-END:variables
}
