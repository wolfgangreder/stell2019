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
package at.or.reder.rpi.model;

import at.or.reder.rpi.LedPanel;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Wolfgang Reder
 */
public interface TrackElement
{

  public UUID getId();

  public Layout getLayout();

  public String getLabel();

  public SymbolType getSymbolType();

  public RouteElementState getRouteState();

  public List<DecoderState> getCurrentDecoderStates();

  public List<DecoderState> switchState();

  public int getTWIAddress();

  public Map<Integer, LedPanel.LedState> getCurrentLedStates();

  public void addChangeListener(ChangeListener evt);

  public void removeChangeListener(ChangeListener evt);

  public void action();

}
