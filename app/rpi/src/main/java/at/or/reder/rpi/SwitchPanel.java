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

import at.or.reder.dcc.AccessoryEvent;
import at.or.reder.dcc.AccessoryEventListener;
import at.or.reder.dcc.LinkState;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.openide.util.Exceptions;

/**
 *
 * @author Wolfgang Reder
 */
public class SwitchPanel extends DevicePanel
{

  private AccessoryEventListener eventListener;
  private int state;

  /**
   * Creates new form SwitchPanel
   */
  public SwitchPanel()
  {
    initComponents();
  }

  private short getDecoder()
  {
    return 10;
  }

  private byte getPort()
  {
    return 0;
  }

  @Override

  protected void doLinkStateChanged(LinkState linkState)
  {
    if (linkState == LinkState.OPEN && device != null) {
      try {
        device.getAccessoryState(getDecoder(),
                                 getPort(),
                                 500);
      } catch (IOException | TimeoutException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  @Override
  protected void connectListener()
  {
    if (device != null && eventListener == null) {
      eventListener = this::onAccessoryEvent;
      device.addAccessoryEventListener(eventListener);
    }
  }

  private void onAccessoryEvent(AccessoryEvent evt)
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

    marchtrenkPanel1 = new at.or.reder.rpi.MarchtrenkPanel();

    setLayout(new java.awt.BorderLayout());
    add(marchtrenkPanel1, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private at.or.reder.rpi.MarchtrenkPanel marchtrenkPanel1;
  // End of variables declaration//GEN-END:variables
}
