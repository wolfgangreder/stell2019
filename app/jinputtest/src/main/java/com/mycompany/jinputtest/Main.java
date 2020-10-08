/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.jinputtest;

import java.awt.geom.Point2D;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

/**
 *
 * @author wolfi
 */
public class Main extends javax.swing.JFrame
{

  private Controller controller;
  private Component xAxis;
  private Component yAxis;
  private Component zAxis;
  private Component rxAxis;
  private Component ryAxis;
  private Component rzAxis;
  private Component pov;
  private Component a;
  private Component b;
  private Component x;
  private Component y;
  private Component leftThumb;
  private Component leftThumb2;
  private Component leftThumb3;
  private Component rightThumb;
  private Component rightThumb2;
  private Component rightThumb3;
  private Component select;
  private Component start;
  private Component mode;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  public Main()
  {
    initComponents();
    connectController();
    registerListener();
    executor.scheduleAtFixedRate(this::pollController,
                                 20,
                                 20,
                                 TimeUnit.MILLISECONDS);
  }

  private void assignAxis(Component xa,
                          Component ya,
                          Component za,
                          DualAxisPanel panel,
                          JSlider slider)
  {
    float x = xa.getPollData();
    float y = ya.getPollData();
    float z = za.getPollData() * 1024;
    Point2D.Float p = new Point2D.Float(x,
                                        y);
    SwingUtilities.invokeLater(() -> {
      panel.setPosition(p);
      panel.repaint();
      slider.setValue((int) z);
    });

  }

  private void pollController()
  {
    try {
      if (controller != null && controller.poll()) {
        assignAxis(xAxis,
                   yAxis,
                   zAxis,
                   leftCross,
                   z);
        assignAxis(rxAxis,
                   ryAxis,
                   rzAxis,
                   rightCross,
                   rz);
        float p = pov.getPollData() * 8;
        SwingUtilities.invokeLater(() -> {
          povN.setSelected(p > 0 && p < 4);
          povE.setSelected(p > 2 && p < 6);
          povS.setSelected(p > 4 && p < 8);
          povW.setSelected(p == 1 || p > 6);
        });
        boolean ap = a.getPollData() > 0;
        boolean bp = b.getPollData() > 0;
        boolean xp = x.getPollData() > 0;
        boolean yp = y.getPollData() > 0;
        btnA.setSelected(ap);
        btnB.setSelected(bp);
        btnX.setSelected(xp);
        btnY.setSelected(yp);
        boolean lt = leftThumb.getPollData() > 0;
        boolean rt = rightThumb.getPollData() > 0;
        boolean lt2 = leftThumb2.getPollData() > 0;
        boolean rt2 = rightThumb2.getPollData() > 0;
        boolean lt3 = leftThumb3.getPollData() > 0;
        boolean rt3 = rightThumb3.getPollData() > 0;
        btLeftThumb.setSelected(lt);
        btRightThumb.setSelected(rt);
        btLeftThumb2.setSelected(lt2);
        btRightThumb2.setSelected(rt2);
        btLeftThumb3.setSelected(lt3);
        btRightThumb3.setSelected(rt3);

        boolean sel = select.getPollData() > 0;
        boolean sta = start.getPollData() > 0;
        boolean mod = mode.getPollData() > 0;
        btnSelect.setSelected(sel);
        btnStart.setSelected(sta);
        btnMode.setSelected(mod);
      } else {
        connectController();
      }
    } catch (Throwable th) {
    }
  }

  private void registerListener()
  {
    ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
    env.addControllerListener(new ControllerListener()
    {
      @Override
      public void controllerRemoved(ControllerEvent ev)
      {
        controller = null;
      }

      @Override
      public void controllerAdded(ControllerEvent ev)
      {
        if (controller == null) {
          connectController();
        }
      }
    });
  }

  private boolean connectController()
  {
    Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
    for (Controller c : controllers) {
      if ("Wireless Controller".equals(c.getName())) {
        controller = c;
        break;
      }
    }
    if (controller != null) {
      xAxis = controller.getComponent(Component.Identifier.Axis.X);
      yAxis = controller.getComponent(Component.Identifier.Axis.Y);
      zAxis = controller.getComponent(Component.Identifier.Axis.Z);

      rxAxis = controller.getComponent(Component.Identifier.Axis.RX);
      ryAxis = controller.getComponent(Component.Identifier.Axis.RY);
      rzAxis = controller.getComponent(Component.Identifier.Axis.RZ);

      pov = controller.getComponent(Component.Identifier.Axis.POV);

      a = controller.getComponent(Component.Identifier.Button.A);
      b = controller.getComponent(Component.Identifier.Button.B);
      x = controller.getComponent(Component.Identifier.Button.X);
      y = controller.getComponent(Component.Identifier.Button.Y);

      rightThumb = controller.getComponent(
          Component.Identifier.Button.RIGHT_THUMB);
      leftThumb = controller.getComponent(Component.Identifier.Button.LEFT_THUMB);
      rightThumb2 = controller.getComponent(
          Component.Identifier.Button.RIGHT_THUMB2);
      leftThumb2 = controller.getComponent(
          Component.Identifier.Button.LEFT_THUMB2);
      rightThumb3 = controller.getComponent(
          Component.Identifier.Button.RIGHT_THUMB3);
      leftThumb3 = controller.getComponent(
          Component.Identifier.Button.LEFT_THUMB3);

      select = controller.getComponent(Component.Identifier.Button.SELECT);
      start = controller.getComponent(Component.Identifier.Button.START);
      mode = controller.getComponent(Component.Identifier.Button.MODE);
    }
    return controller != null;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    jPanel1 = new javax.swing.JPanel();
    povN = new javax.swing.JToggleButton();
    povE = new javax.swing.JToggleButton();
    povW = new javax.swing.JToggleButton();
    povS = new javax.swing.JToggleButton();
    jPanel2 = new javax.swing.JPanel();
    btnX = new javax.swing.JToggleButton();
    btnB = new javax.swing.JToggleButton();
    btnY = new javax.swing.JToggleButton();
    btnA = new javax.swing.JToggleButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosed(java.awt.event.WindowEvent evt)
      {
        formWindowClosed(evt);
      }
    });

    leftCross.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

    rightCross.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

    z.setMaximum(1024);
    z.setMinimum(-1024);
    z.setOrientation(javax.swing.JSlider.VERTICAL);

    rz.setMaximum(1024);
    rz.setMinimum(-1024);
    rz.setOrientation(javax.swing.JSlider.VERTICAL);

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

    btLeftThumb.setText("LT");

    btRightThumb.setText("RT");

    btLeftThumb2.setText("LT2");

    btRightThumb2.setText("RT2");

    btLeftThumb3.setText("LT3");

    btRightThumb3.setText("RT3");

    btnSelect.setText("Select");

    btnStart.setText("Start");

    btnMode.setText("Mode");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(61, 61, 61)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGap(187, 187, 187)
                .addComponent(btnMode, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(332, 332, 332)))
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(leftCross, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addGroup(layout.createSequentialGroup()
                .addComponent(btLeftThumb, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btRightThumb, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(layout.createSequentialGroup()
                .addComponent(btLeftThumb2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btRightThumb2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(layout.createSequentialGroup()
                .addComponent(btLeftThumb3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btRightThumb3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addGroup(layout.createSequentialGroup()
                .addComponent(rz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightCross, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        .addGap(111, 111, 111))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(31, 31, 31)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(btLeftThumb2)
              .addComponent(btRightThumb2))
            .addGap(17, 17, 17)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(btRightThumb)
              .addComponent(btLeftThumb))
            .addGap(17, 17, 17)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(btRightThumb3)
              .addComponent(btLeftThumb3)))
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
            .addComponent(z, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
            .addComponent(rightCross, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(leftCross, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(btnSelect)
              .addComponent(btnStart)))
          .addGroup(layout.createSequentialGroup()
            .addGap(101, 101, 101)
            .addComponent(btnMode)))
        .addContainerGap(180, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void formWindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosed
  {//GEN-HEADEREND:event_formWindowClosed
    executor.shutdown();
  }//GEN-LAST:event_formWindowClosed

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
      java.util.logging.Logger.getLogger(Main.class.getName()).log(
          java.util.logging.Level.SEVERE,
          null,
          ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(
          java.util.logging.Level.SEVERE,
          null,
          ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(
          java.util.logging.Level.SEVERE,
          null,
          ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(Main.class.getName()).log(
          java.util.logging.Level.SEVERE,
          null,
          ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        new Main().setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
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
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private final com.mycompany.jinputtest.DualAxisPanel leftCross = new com.mycompany.jinputtest.DualAxisPanel();
  private javax.swing.JToggleButton povE;
  private javax.swing.JToggleButton povN;
  private javax.swing.JToggleButton povS;
  private javax.swing.JToggleButton povW;
  private final com.mycompany.jinputtest.DualAxisPanel rightCross = new com.mycompany.jinputtest.DualAxisPanel();
  private final javax.swing.JSlider rz = new javax.swing.JSlider();
  private final javax.swing.JSlider z = new javax.swing.JSlider();
  // End of variables declaration//GEN-END:variables
}
