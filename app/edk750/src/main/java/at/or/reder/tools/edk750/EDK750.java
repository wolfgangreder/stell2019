/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import at.or.reder.controller.PSController;
import at.or.reder.dcc.Controller;
import java.beans.BeanProperty;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.util.Lookup;

/**
 *
 * @author Wolfgang Reder
 */
public interface EDK750 extends Lookup.Provider
{

  public static final String PROP_STARTED = "started";
  public static final String PROP_EMERGENCY = "inEmergencyMode";

  public boolean isAxisLock();

  public void setAxisLock(boolean axisLock);

  public boolean isStrictMode();

  public void setStrictMode(boolean strictMode);

  @BeanProperty(bound = true)
  public boolean isStarted();

  public void setStarted(boolean s);

  public PSController getPSController();

  public void setPSController(PSController controller);

  public String getControllerId();

  public void setControllerId(String controllerId);

  public Controller getDCCController();

  public void setDCCController(Controller dcc) throws IOException;

  public int getAddress();

  public void setAddress(int address) throws IOException;

  @BeanProperty(bound = true)
  public boolean isInEmergencyMode();

  public void addPropertyChangeListener(PropertyChangeListener listener);

  public void addPropertyChangeListener(String propName,
                                        PropertyChangeListener listener);

  public void removePropertyChangeListener(PropertyChangeListener listener);

  public void removePropertyChangeListener(String propName,
                                           PropertyChangeListener listener);

  public void addEDKExceptionListener(EDKExceptionListener l);

  public void removeEDKExceptionListener(EDKExceptionListener l);

  public void addEDKAxisListener(EDKAxisListener l);

  public void removeEDKAxisListener(EDKAxisListener l);

}
