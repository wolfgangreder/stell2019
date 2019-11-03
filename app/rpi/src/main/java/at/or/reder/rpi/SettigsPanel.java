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
import at.or.reder.dcc.LinkState;
import at.or.reder.dcc.LinkStateListener;
import javax.swing.SwingUtilities;

/**
 *
 * @author Wolfgang Reder
 */
public class SettigsPanel extends DevicePanel
{

  private LinkStateListener linkStateListener;

  public SettigsPanel()
  {
    initComponents();

  }

  @Override
  protected void doLinkStateChanged(LinkState linkState)
  {
    setControlState();
  }

  private void onLickState(Controller can,
                           LinkState state)
  {
    SwingUtilities.invokeLater(this::setControlState);
  }

  @Override
  protected void connectListener()
  {
    if (device != null) {
      if (linkStateListener == null) {
        linkStateListener = this::onLickState;
        device.addLinkStateListener(linkStateListener);
      }
    }
  }

  @Override
  protected void disconnectListener()
  {
    if (device != null) {
      if (linkStateListener != null) {
        device.removeLinkStateListener(linkStateListener);
        linkStateListener = null;
      }
    }
  }

  private void setControlState()
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

    jButton1 = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();
    jTextField1 = new javax.swing.JTextField();
    jButton2 = new javax.swing.JButton();

    jButton1.setText(org.openide.util.NbBundle.getMessage(SettigsPanel.class, "SettigsPanel.jButton1.text")); // NOI18N
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton1ActionPerformed(evt);
      }
    });

    jLabel1.setText(org.openide.util.NbBundle.getMessage(SettigsPanel.class, "SettigsPanel.jLabel1.text")); // NOI18N

    jTextField1.setText(org.openide.util.NbBundle.getMessage(SettigsPanel.class, "SettigsPanel.jTextField1.text")); // NOI18N

    jButton2.setText(org.openide.util.NbBundle.getMessage(SettigsPanel.class, "SettigsPanel.jButton2.text")); // NOI18N
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton2ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jButton1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton2))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(186, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton1)
          .addComponent(jButton2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(234, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
  {//GEN-HEADEREND:event_jButton1ActionPerformed
    Runtime.getRuntime().exit(0);
  }//GEN-LAST:event_jButton1ActionPerformed

  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
  {//GEN-HEADEREND:event_jButton2ActionPerformed
    Runtime.getRuntime().exit(125);
  }//GEN-LAST:event_jButton2ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JTextField jTextField1;
  // End of variables declaration//GEN-END:variables
}
