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

import at.or.reder.rpi.LedPanel;
import at.or.reder.rpi.model.DecoderState;
import at.or.reder.rpi.model.Layout;
import at.or.reder.rpi.model.RouteElementState;
import at.or.reder.rpi.model.SymbolType;
import at.or.reder.rpi.model.TrackElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Wolfgang Reder
 */
public class SpecialTrackElement implements TrackElement
{

  public static TrackElement getWGT(Layout layout,
                                    int twiAddress)
  {
    return new SpecialTrackElement(layout,
                                   SymbolType.WGT,
                                   twiAddress,
                                   true);
  }

  public static TrackElement getFHT(Layout layout,
                                    int twiAddress)
  {
    return new SpecialTrackElement(layout,
                                   SymbolType.FHT,
                                   twiAddress,
                                   true);
  }

  public static TrackElement getHAGT(Layout layout,
                                     int twiAddress)
  {
    return new SpecialTrackElement(layout,
                                   SymbolType.HAGT,
                                   twiAddress,
                                   false);
  }

  public static TrackElement getSGT(Layout layout,
                                    int twiAddress)
  {
    return new SpecialTrackElement(layout,
                                   SymbolType.SGT,
                                   twiAddress,
                                   false);
  }

  private final Layout layout;
  private final SymbolType symbolType;
  private final int twiAddress;
  private final ChangeSupport changeSupport = new ChangeSupport(this);
  private RouteElementState state = RouteElementState.FREE;
  private final boolean stateful;

  private SpecialTrackElement(Layout layout,
                              SymbolType symbolType,
                              int twiAddress,
                              boolean stateful)
  {
    this.layout = layout;
    this.symbolType = symbolType;
    this.twiAddress = twiAddress;
    this.stateful = stateful;
  }

  @Override
  public UUID getId()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Layout getLayout()
  {
    return layout;
  }

  @Override
  public String getLabel()
  {
    return symbolType.getLabel();
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

  @Override
  public List<DecoderState> getCurrentDecoderStates()
  {
    return Collections.emptyList();
  }

  @Override
  public List<DecoderState> switchState()
  {
    return Collections.emptyList();
  }

  @Override
  public int getTWIAddress()
  {
    return twiAddress;
  }

  @Override
  public Map<Integer, LedPanel.LedState> getCurrentLedStates()
  {
    switch (state) {
      case ARMED:
        return Map.of(2,
                      LedPanel.LedState.BLINK);
      default:
        return Collections.emptyMap();
    }
  }

  @Override
  public void addChangeListener(ChangeListener listener)
  {
    changeSupport.addChangeListener(listener);
  }

  @Override
  public void removeChangeListener(ChangeListener listener
  )
  {
    changeSupport.removeChangeListener(listener);
  }

  @Override
  public void action()
  {
    if (stateful) {
      if (state == RouteElementState.ARMED) {
        state = RouteElementState.FREE;
      } else {
        state = RouteElementState.ARMED;
      }
    }
    changeSupport.fireChange();
  }

}
