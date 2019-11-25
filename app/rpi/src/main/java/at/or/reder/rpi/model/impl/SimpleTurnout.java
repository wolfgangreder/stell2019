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

import at.or.reder.dcc.AccessoryEvent;
import at.or.reder.dcc.AccessoryEventListener;
import at.or.reder.dcc.Controller;
import at.or.reder.dcc.LinkState;
import at.or.reder.rpi.LedPanel;
import at.or.reder.rpi.model.DecoderState;
import at.or.reder.rpi.model.Layout;
import at.or.reder.rpi.model.SymbolType;
import at.or.reder.rpi.model.TrackElement;
import at.or.reder.rpi.model.TrackElementBuilder;
import at.or.reder.rpi.model.TurnoutElement;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Wolfgang Reder
 */
public class SimpleTurnout extends AbstractTrackElement implements TurnoutElement
{

  public static final class Builder implements TrackElementBuilder
  {

    private short decoderAddress;
    private byte port;
    private Layout layout;
    private UUID id;
    private String label;
    private SymbolType type;
    private final Map<Path, Integer> pathMapping = new HashMap<>();

    public Builder decoderAddress(short decoderAddress)
    {
      this.decoderAddress = decoderAddress;
      return this;
    }

    public Builder port(byte port)
    {
      this.port = port;
      return this;
    }

    public Builder id(UUID id)
    {
      this.id = id;
      return this;
    }

    public Builder label(String label)
    {
      this.label = label;
      return this;
    }

    public Builder symbolType(SymbolType type)
    {
      switch (type) {
        case W1:
        case W2:
        case W3:
        case W4:
          this.type = type;
          break;
        default:
          throw new IllegalArgumentException();
      }
      return this;
    }

    public Builder addPathMapping(Path path,
                                  int mapping)
    {
      pathMapping.put(path,
                      mapping);
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
      return new SimpleTurnout(id,
                               layout,
                               label,
                               type,
                               decoderAddress,
                               decoderAddress,
                               port,
                               pathMapping);
    }

  }
  private final short decoderAddress;
  private final byte port;
  private Controller control;
  private Path currentPath;
  private final Map<Path, Integer> pathMapping;
  private final Map<Path, Map<Integer, LedPanel.LedState>> stateMapOn;
  private final Map<Path, Map<Integer, LedPanel.LedState>> stateMapBlink;
  private boolean ackPending;
  private final AccessoryEventListener eventListener = this::onAccessoryEvent;

  public SimpleTurnout(UUID id,
                       Layout layout,
                       String label,
                       SymbolType symbolType,
                       int twiAddress,
                       short decoderAddress,
                       byte port,
                       Map<Path, Integer> pathMapping)
  {
    super(id,
          layout,
          label,
          symbolType,
          twiAddress);
    this.decoderAddress = decoderAddress;
    this.port = port;
    this.pathMapping = new HashMap<>(pathMapping);
    setController(layout.getController());
    stateMapOn = initStateMap(symbolType,
                              LedPanel.LedState.ON);
    stateMapBlink = initStateMap(symbolType,
                                 LedPanel.LedState.BLINK);
  }

  private Map<Path, Map<Integer, LedPanel.LedState>> initStateMap(SymbolType symbol,
                                                                  LedPanel.LedState state)
  {
    Map<Path, Map<Integer, LedPanel.LedState>> result = new HashMap<>();
    Map<Integer, LedPanel.LedState> tmp;
    switch (symbol) {
      case W1:
        tmp = Map.of(4,
                     state,
                     5,
                     state);
        result.put(Path.A,
                   tmp);
        result.put(Path.C,
                   tmp);
        tmp = Map.of(1,
                     state,
                     5,
                     state);
        result.put(Path.B,
                   tmp);
        result.put(Path.D,
                   tmp);
        break;
      case W2:
        tmp = Map.of(4,
                     state,
                     5,
                     state);
        result.put(Path.A,
                   tmp);
        result.put(Path.C,
                   tmp);
        tmp = Map.of(5,
                     state,
                     6,
                     state);
        result.put(Path.B,
                   tmp);
        result.put(Path.D,
                   tmp);
        break;
      case W3:
        tmp = Map.of(3,
                     state,
                     6,
                     state);
        result.put(Path.A,
                   tmp);
        result.put(Path.C,
                   tmp);
        tmp = Map.of(5,
                     state,
                     6,
                     state);
        result.put(Path.B,
                   tmp);
        result.put(Path.D,
                   tmp);
        break;
      case W4:
        tmp = Map.of(1,
                     state,
                     8,
                     state);
        result.put(Path.A,
                   tmp);
        result.put(Path.C,
                   tmp);
        tmp = Map.of(4,
                     state,
                     8,
                     state);
        result.put(Path.B,
                   tmp);
        result.put(Path.D,
                   tmp);
        break;
    }
    return result;
  }

  @Override
  public Controller getController()
  {
    synchronized (this) {
      return control;
    }
  }

  @Override
  public final void setController(Controller controller)
  {
    synchronized (this) {
      if (this.control != null) {
        this.control.removeAccessoryEventListener(eventListener);
      }
      this.control = controller;
      if (this.control != null) {
        control.addAccessoryEventListener(eventListener);
      }
    }
  }

  @Override
  public void queryState()
  {
    if (control != null && control.getLinkState() == LinkState.CONNECTED) {
      try {
        control.getAccessoryState(decoderAddress,
                                  port);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  @Override
  public Path getCurrentPath()
  {
    return currentPath;
  }

  @Override
  public void setCurrentPath(Path path)
  {
    setCurrentPath(path,
                   false,
                   true);
  }

  private void setCurrentPath(Path path,
                              boolean fireAnyway,
                              boolean sendToController)
  {
    boolean fire = fireAnyway;
    if (currentPath != path) {
      currentPath = path;
      fire = true;
      if (sendToController) {
        List<DecoderState> decoderStates = getCurrentDecoderStates();
        for (DecoderState s : decoderStates) {
          try {
            control.setAccessoryState(s.getDecoderAddress(),
                                      s.getPort(),
                                      s.getState());
          } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
          }
        }
      }
    }
    if (fire) {
      changeSupport.fireChange();
    }
  }

  private void onAccessoryEvent(AccessoryEvent evt)
  {
    SwingUtilities.invokeLater(() -> {
      if (evt.getDeocder() == decoderAddress && evt.getPort() == port && (currentPath == null || ackPending || evt.getValue()
                                                                                                               != pathMapping.
                                                                          get(currentPath))) {
        Path newPath = pathMapping.entrySet().stream().filter((e) -> e.getValue() == evt.getValue()).map((e) -> e.getKey()).
                findFirst().orElse(null);
        if (newPath != null) {
          boolean fireStateChange = ackPending;
          ackPending = false;
          setCurrentPath(newPath,
                         fireStateChange,
                         false);
        }
      }
    });
  }

  @Override
  public void action()
  {
    Path newPath = currentPath != null ? Path.values()[(currentPath.ordinal() + 1) % 2] : Path.A;
    boolean fireStateChanged = !ackPending;
    ackPending = true;
    setCurrentPath(newPath,
                   fireStateChanged,
                   true);
  }

  @Override
  public List<DecoderState> getCurrentDecoderStates()
  {
    return Collections.singletonList(new SimpleDecoderState(decoderAddress,
                                                            port,
                                                            pathMapping.get(currentPath)));
  }

  @Override
  public Map<Integer, LedPanel.LedState> getCurrentLedStates()
  {
    switch (getRouteState()) {
      case ARMED:
        return stateMapBlink.get(currentPath);
      case FREE:
        return ackPending ? stateMapBlink.get(currentPath) : stateMapOn.get(currentPath);
      case LOCKED:
        return ackPending ? stateMapBlink.get(currentPath) : stateMapOn.get(currentPath);
      case OCCUPIED:
        return ackPending ? stateMapBlink.get(currentPath) : stateMapOn.get(currentPath);
      case PROTECTED:
        return ackPending ? stateMapBlink.get(currentPath) : stateMapOn.get(currentPath);
    }
    return Collections.emptyMap();
  }

}
