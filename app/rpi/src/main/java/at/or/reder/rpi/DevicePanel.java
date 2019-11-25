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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Wolfgang Reder
 */
public abstract class DevicePanel extends JPanel implements ContainerListener
{

  protected Controller device;
  private final List<DevicePanel> subDevices = new ArrayList<>();
  private LinkStateListener linkStateListener;

  @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
  protected DevicePanel()
  {
    addContainerListener(this);
  }

  public boolean isLinkedOpen()
  {
    return device != null && device.getLinkState() == LinkState.CONNECTED;
  }

  protected void doLinkStateChanged(LinkState linkState)
  {

  }

  private void onLinkStateEvent(Controller device,
                                LinkState linkState)
  {
    SwingUtilities.invokeLater(() -> doLinkStateChanged(linkState));
  }

  private void connectLinkStateListener()
  {
    if (device != null && linkStateListener == null) {
      linkStateListener = this::onLinkStateEvent;
      device.addLinkStateListener(linkStateListener);
    }
  }

  private void disconnectLinkStateListener()
  {
    if (device != null && linkStateListener != null) {
      device.removeLinkStateListener(linkStateListener);
      linkStateListener = null;
    }
  }

  protected void disconnectListener()
  {
  }

  protected void connectListener()
  {
  }

  private void addContainer(Container container)
  {
    container.addContainerListener(this);
    for (int i = 0; i < container.getComponentCount(); ++i) {
      Component comp = container.getComponent(i);
      if (comp instanceof DevicePanel) {
        subDevices.add((DevicePanel) comp);
      }
      if (comp instanceof Container) {
        addContainer((Container) comp);
      }
    }
  }

  @Override
  public void componentAdded(ContainerEvent e)
  {
    if (e.getChild() instanceof DevicePanel) {
      subDevices.add((DevicePanel) e.getChild());
    }
    if (e.getChild() instanceof Container) {
      addContainer((Container) e.getChild());
    }
  }

  private void removeContainer(Container container)
  {
    container.removeContainerListener(this);
    for (int i = 0; i < container.getComponentCount(); ++i) {
      Component comp = container.getComponent(i);
      if (comp instanceof DevicePanel) {
        subDevices.remove((DevicePanel) comp);
      }
      if (comp instanceof Container) {
        removeContainer((Container) container);
      }
    }
  }

  @Override
  public void componentRemoved(ContainerEvent e)
  {
    if (e.getChild() instanceof DevicePanel) {
      subDevices.remove((DevicePanel) e.getChild());
    }
    if (e.getChild() instanceof Container) {
      removeContainer((Container) e.getChild());
    }
  }

  public Controller getDevice()
  {
    return device;
  }

  public void setDevice(Controller device)
  {
    if (this.device != device) {
      disconnectLinkStateListener();
      disconnectListener();
      this.device = device;
      connectListener();
      connectLinkStateListener();
      for (DevicePanel panel : subDevices) {
        panel.setDevice(device);
      }
    }
  }

}
