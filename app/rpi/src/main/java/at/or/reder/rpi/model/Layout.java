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

import at.or.reder.dcc.Controller;
import java.util.List;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Wolfgang Reder
 */
public interface Layout
{

  public Controller getController();

  public void setController(Controller newController);

  public LayoutState getLayoutState();

  public void addChangeListener(ChangeListener l);

  public void removeChangeListener(ChangeListener l);

  public TrackElement getWGT();

  public TrackElement getFHT();

  public TrackElement getHAGT();

  public TrackElement getSGT();

  public String getName();

  public List<TrackElement> getElements();

  public default TrackElement getTrackElementByLabel(String label)
  {
    return getElements().stream().filter((e) -> e.getLabel().equals(label)).findFirst().orElse(null);
  }

  public List<Route> getRoutes();

  public void action(TrackElement element);

}
