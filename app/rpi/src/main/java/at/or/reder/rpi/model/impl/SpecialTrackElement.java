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

import at.or.reder.dcc.Controller;
import at.or.reder.rpi.LedPanel;
import at.or.reder.rpi.model.DecoderState;
import at.or.reder.rpi.model.Layout;
import at.or.reder.rpi.model.RouteElementState;
import at.or.reder.rpi.model.SymbolType;
import at.or.reder.rpi.model.TrackElement;
import at.or.reder.rpi.model.TrackElementBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Wolfgang Reder
 */
public class SpecialTrackElement extends AbstractTrackElement
{

  private static final class Builder implements TrackElementBuilder
  {

    private int twiAddress;
    private SymbolType type;
    private boolean stateful;
    private Layout layout;

    public Builder twiAddress(int address)
    {
      twiAddress = address;
      return this;
    }

    public Builder symbolType(SymbolType type)
    {
      this.type = type;
      return this;
    }

    public Builder stateful(boolean s)
    {
      stateful = s;
      return this;
    }

    @Override
    public Builder layout(Layout layout)
    {
      this.layout = layout;
      return this;
    }

    @Override
    public TrackElement build()
    {
      return new SpecialTrackElement(UUID.randomUUID(),
                                     layout,
                                     type,
                                     twiAddress,
                                     stateful);
    }

  }

  public static TrackElementBuilder getWGT(int twiAddress)
  {
    return new Builder().stateful(true).symbolType(SymbolType.WGT).twiAddress(twiAddress);
  }

  public static TrackElementBuilder getFHT(int twiAddress)
  {
    return new Builder().stateful(true).symbolType(SymbolType.FHT).twiAddress(twiAddress);
  }

  public static TrackElementBuilder getHAGT(int twiAddress)
  {
    return new Builder().stateful(false).symbolType(SymbolType.HAGT).twiAddress(twiAddress);
  }

  public static TrackElementBuilder getSGT(int twiAddress)
  {
    return new Builder().stateful(false).symbolType(SymbolType.SGT).twiAddress(twiAddress);
  }

  private final boolean stateful;

  private SpecialTrackElement(UUID id,
                              Layout layout,
                              SymbolType symbolType,
                              int twiAddress,
                              boolean stateful)
  {
    super(id,
          layout,
          symbolType.getLabel(),
          symbolType,
          twiAddress);
    this.stateful = stateful;
  }

  @Override
  public Controller getController()
  {
    return null;
  }

  @Override
  public void setController(Controller controler)
  {
  }

  @Override
  public List<DecoderState> getCurrentDecoderStates()
  {
    return Collections.emptyList();
  }

  @Override
  public Map<Integer, LedPanel.LedState> getCurrentLedStates()
  {
    switch (getRouteState()) {
      case ARMED:
        return Map.of(2,
                      LedPanel.LedState.BLINK);
      default:
        return Collections.emptyMap();
    }
  }

  @Override
  public void action()
  {
    if (stateful) {
      if (getRouteState() == RouteElementState.ARMED) {
        setRouteState(RouteElementState.FREE);
      } else {
        setRouteState(RouteElementState.ARMED);
      }
    }
  }

}
