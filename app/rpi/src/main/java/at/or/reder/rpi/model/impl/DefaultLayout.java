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
package at.or.reder.rpi.model.impl;

import at.or.reder.rpi.model.Layout;
import at.or.reder.rpi.model.LayoutState;
import at.or.reder.rpi.model.Route;
import at.or.reder.rpi.model.TrackElement;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Wolfgang Reder
 */
public class DefaultLayout implements Layout
{

  private LayoutState state;
  private final ChangeSupport changeSupport = new ChangeSupport(this);
  private final TrackElement wgt = null;
  private final TrackElement fht = null;
  private final TrackElement hagt = null;
  private final TrackElement sgt = null;

  @Override
  public LayoutState getLayoutState()
  {
    return state;
  }

  @Override
  public void addChangeListener(ChangeListener l)
  {
    changeSupport.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l)
  {
    changeSupport.removeChangeListener(l);
  }

  @Override
  public TrackElement getWGT()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public TrackElement getFHT()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public TrackElement getHAGT()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public TrackElement getSGT()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getName()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<TrackElement> getElements()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Route> getRoutes()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void action(TrackElement element)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
