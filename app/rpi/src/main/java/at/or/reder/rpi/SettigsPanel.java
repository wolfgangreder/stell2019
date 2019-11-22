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
import at.or.reder.rpi.model.SymbolRotation;
import at.or.reder.rpi.model.SymbolRotationComboBoxModel;
import at.or.reder.rpi.model.SymbolType;
import at.or.reder.rpi.model.SymbolTypeComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Wolfgang Reder
 */
public class SettigsPanel extends DevicePanel
{

  private final SymbolTypeComboBoxModel typeModel = new SymbolTypeComboBoxModel();
  private final SymbolRotationComboBoxModel rotModel = new SymbolRotationComboBoxModel();
  private LinkStateListener linkStateListener;

  public SettigsPanel()
  {
    initComponents();
    typeModel.addListDataListener(new ListDataListener()
    {
      @Override
      public void contentsChanged(ListDataEvent e)
      {
        onSymbolTypeChanged(e);
      }

      @Override
      public void intervalAdded(ListDataEvent e)
      {
      }

      @Override
      public void intervalRemoved(ListDataEvent e)
      {
      }

    });
    rotModel.addListDataListener(new ListDataListener()
    {
      @Override
      public void contentsChanged(ListDataEvent e)
      {
        onSymbolRotationChanged(e);
      }

      @Override
      public void intervalAdded(ListDataEvent e)
      {
      }

      @Override
      public void intervalRemoved(ListDataEvent e)
      {
      }

    });
  }

  private void clearCorner()
  {
    symbolPanel1.clearCorner();
    symbolPanel2.clearCorner();
    symbolPanel3.clearCorner();
    symbolPanel4.clearCorner();
    symbolPanel5.clearCorner();
    symbolPanel6.clearCorner();
    symbolPanel7.clearCorner();
    symbolPanel8.clearCorner();
    symbolPanel9.clearCorner();
  }

  private void fixCorner()
  {
    clearCorner();
    symbolPanel1.fixCornerPoints();
    symbolPanel2.fixCornerPoints();
    symbolPanel3.fixCornerPoints();
    symbolPanel4.fixCornerPoints();
    symbolPanel5.fixCornerPoints();
    symbolPanel6.fixCornerPoints();
    symbolPanel7.fixCornerPoints();
    symbolPanel8.fixCornerPoints();
    symbolPanel9.fixCornerPoints();
  }

  private void onSymbolTypeChanged(ListDataEvent evt)
  {
    symbolPanel1.setSymbolType(typeModel.getSelectedItem());
    fixCorner();
  }

  private void onSymbolRotationChanged(ListDataEvent evt)
  {
    symbolPanel1.setRotation(rotModel.getSelectedItem());
    fixCorner();
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
    jButton2 = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    symbolPanel2 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel3 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel4 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel5 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel1 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel6 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel7 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel8 = new at.or.reder.rpi.SymbolPanel();
    symbolPanel9 = new at.or.reder.rpi.SymbolPanel();

    jButton1.setText(org.openide.util.NbBundle.getMessage(SettigsPanel.class, "SettigsPanel.jButton1.text")); // NOI18N
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton1ActionPerformed(evt);
      }
    });

    jButton2.setText(org.openide.util.NbBundle.getMessage(SettigsPanel.class, "SettigsPanel.jButton2.text")); // NOI18N
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton2ActionPerformed(evt);
      }
    });

    cbSymbolType.setModel(typeModel);

    cbRotation.setModel(rotModel);

    jPanel1.setLayout(new java.awt.GridLayout(3, 3));

    symbolPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel2.setName("1"); // NOI18N
    jPanel1.add(symbolPanel2);

    symbolPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel3.setName("2"); // NOI18N
    jPanel1.add(symbolPanel3);

    symbolPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel4.setName("3"); // NOI18N
    jPanel1.add(symbolPanel4);

    symbolPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel5.setName("4"); // NOI18N
    jPanel1.add(symbolPanel5);

    symbolPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel1.setName("9"); // NOI18N
    jPanel1.add(symbolPanel1);

    symbolPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel6.setName("5"); // NOI18N
    jPanel1.add(symbolPanel6);

    symbolPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel7.setName("6"); // NOI18N
    jPanel1.add(symbolPanel7);

    symbolPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel8.setName("7"); // NOI18N
    jPanel1.add(symbolPanel8);

    symbolPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    symbolPanel9.setName("8"); // NOI18N
    jPanel1.add(symbolPanel9);

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
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(cbSymbolType, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(cbRotation, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))))
        .addContainerGap(97, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton1)
          .addComponent(jButton2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(cbSymbolType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cbRotation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(97, Short.MAX_VALUE))
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
  private final javax.swing.JComboBox<SymbolRotation> cbRotation = new javax.swing.JComboBox<>();
  private final javax.swing.JComboBox<SymbolType> cbSymbolType = new javax.swing.JComboBox<>();
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JPanel jPanel1;
  private at.or.reder.rpi.SymbolPanel symbolPanel1;
  private at.or.reder.rpi.SymbolPanel symbolPanel2;
  private at.or.reder.rpi.SymbolPanel symbolPanel3;
  private at.or.reder.rpi.SymbolPanel symbolPanel4;
  private at.or.reder.rpi.SymbolPanel symbolPanel5;
  private at.or.reder.rpi.SymbolPanel symbolPanel6;
  private at.or.reder.rpi.SymbolPanel symbolPanel7;
  private at.or.reder.rpi.SymbolPanel symbolPanel8;
  private at.or.reder.rpi.SymbolPanel symbolPanel9;
  // End of variables declaration//GEN-END:variables
}
