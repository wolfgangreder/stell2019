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
import at.or.reder.rpi.model.RouteElementState;
import at.or.reder.rpi.model.SymbolType;
import at.or.reder.rpi.model.TrackElement;
import java.util.UUID;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Wolfgang Reder
 */
public abstract class AbstractTrackElement implements TrackElement
{

  private final UUID id;
  private final Layout layout;
  private final String label;
  private final SymbolType symbolType;
  private final int twiAddress;
  private RouteElementState state = RouteElementState.FREE;
  protected final ChangeSupport changeSupport = new ChangeSupport(this);

  protected AbstractTrackElement(UUID id,
                                 Layout layout,
                                 String label,
                                 SymbolType symbolType,
                                 int twiAddress)
  {
    this.id = id;
    this.layout = layout;
    this.label = label;
    this.symbolType = symbolType;
    this.twiAddress = twiAddress;
  }

  @Override
  public UUID getId()
  {
    return id;
  }

  @Override
  public Layout getLayout()
  {
    return layout;
  }

  @Override
  public String getLabel()
  {
    return label;
  }

  @Override
  public SymbolType getSymbolType()
  {
    return symbolType;
  }

  @Override
  public RouteElementState getRouteState()
  {
    return state;
  }

  protected void setRouteState(RouteElementState newState)
  {
    if (state != newState) {
      this.state = newState;
      changeSupport.fireChange();
    }
  }

  @Override
  public int getTWIAddress()
  {
    return twiAddress;
  }

  @Override
  public void addChangeListener(ChangeListener evt)
  {
    if (evt != null) {
      changeSupport.addChangeListener(evt);
    }
  }

  @Override
  public void removeChangeListener(ChangeListener evt)
  {
    changeSupport.removeChangeListener(evt);
  }

}
