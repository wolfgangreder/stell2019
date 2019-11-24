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
import at.or.reder.rpi.model.Layout;
import at.or.reder.rpi.model.LayoutState;
import at.or.reder.rpi.model.Route;
import at.or.reder.rpi.model.TrackElement;
import at.or.reder.rpi.model.TrackElementBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Wolfgang Reder
 */
public class DefaultLayout implements Layout
{

  public static final class Builder
  {

    private Controller controller;
    private TrackElementBuilder wgt;
    private TrackElementBuilder fht;
    private TrackElementBuilder hagt;
    private TrackElementBuilder sgt;
    private final List<TrackElementBuilder> elements = new ArrayList<>();
    private String name;

    public Builder controller(Controller controller)
    {
      this.controller = controller;
      return this;
    }

    public Builder wgt(TrackElementBuilder wgt)
    {
      this.wgt = wgt;
      return this;
    }

    public Builder fht(TrackElementBuilder fht)
    {
      this.fht = fht;
      return this;
    }

    public Builder hagt(TrackElementBuilder hagt)
    {
      this.hagt = hagt;
      return this;
    }

    public Builder sgt(TrackElementBuilder sgt)
    {
      this.sgt = sgt;
      return this;
    }

    public Builder trackElement(TrackElementBuilder element)
    {
      if (element != null) {
        elements.add(element);
      }
      return this;
    }

    public Builder name(String name)
    {
      this.name = name;
      return this;
    }

    public Layout build()
    {
      return new DefaultLayout(controller,
                               name,
                               wgt,
                               fht,
                               hagt,
                               sgt,
                               elements);
    }

  }
  private Controller controller;
  private LayoutState state;
  private final ChangeSupport changeSupport = new ChangeSupport(this);
  private final TrackElement wgt;
  private final TrackElement fht;
  private final TrackElement hagt;
  private final TrackElement sgt;
  private final List<TrackElement> elements;
  private final String name;

  @SuppressWarnings("LeakingThisInConstructor")
  public DefaultLayout(Controller controller,
                       String name,
                       TrackElementBuilder wgt,
                       TrackElementBuilder fht,
                       TrackElementBuilder hagt,
                       TrackElementBuilder sgt,
                       Collection<? extends TrackElementBuilder> elements)
  {
    this.controller = controller;
    this.name = name;
    state = LayoutState.NORMAL;
    this.wgt = wgt.layout(this).build();
    this.fht = fht.layout(this).build();
    this.hagt = hagt.layout(this).build();
    this.sgt = sgt.layout(this).build();
    this.elements = elements.stream().
            filter((e) -> e != null).
            map((b) -> b.layout(this).build()).
            collect(Collectors.toUnmodifiableList());
  }

  @Override
  public Controller getController()
  {
    synchronized (this) {
      return controller;
    }
  }

  @Override
  public void setController(Controller newController)
  {
    synchronized (this) {
      controller = newController;
      for (TrackElement e : elements) {
        e.setController(controller);
      }
    }
  }

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
    return wgt;
  }

  @Override
  public TrackElement getFHT()
  {
    return fht;
  }

  @Override
  public TrackElement getHAGT()
  {
    return hagt;
  }

  @Override
  public TrackElement getSGT()
  {
    return sgt;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public List<TrackElement> getElements()
  {
    return elements;
  }

  @Override
  public List<Route> getRoutes()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void action(TrackElement element)
  {
    if (elements.contains(element)) {
      element.action();
    }
  }

}
