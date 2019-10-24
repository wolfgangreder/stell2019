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

import at.or.reder.rpi.SymbolType;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Wolfgang Reder
 */
public final class SymbolTypeComboBoxModel implements ComboBoxModel<SymbolType>
{

  private final Set<ListDataListener> listener = new CopyOnWriteArraySet<>();
  private SymbolType selected;

  @Override
  public void setSelectedItem(Object anItem)
  {
    SymbolType old = selected;
    if (anItem instanceof SymbolType) {
      selected = (SymbolType) anItem;
    } else {
      selected = null;
    }
    if (old != selected && !listener.isEmpty()) {
      ListDataEvent evt = new ListDataEvent(this,
                                            ListDataEvent.CONTENTS_CHANGED,
                                            -1,
                                            -1);
      for (ListDataListener l : listener) {
        l.contentsChanged(evt);
      }
    }
  }

  @Override
  public SymbolType getSelectedItem()
  {
    return selected;
  }

  @Override
  public int getSize()
  {
    return SymbolType.values().length;
  }

  @Override
  public SymbolType getElementAt(int index)
  {
    return SymbolType.values()[index];
  }

  @Override
  public void addListDataListener(ListDataListener l)
  {
    if (l != null) {
      listener.add(l);
    }
  }

  @Override
  public void removeListDataListener(ListDataListener l)
  {
    listener.remove(l);
  }

}
