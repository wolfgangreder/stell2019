/*
 * Copyright 2020 Wolfgang Reder.
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
package at.or.reder.tools.fieldtest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class AbstractComboBoxModel<V> implements ComboBoxModel<V>
{

  public static <C extends Enum<C>> AbstractComboBoxModel<C> instanceOf(Class<C> clazz)
  {
    List<C> values = new ArrayList<>();
    Function<Object, C> mapper = null;
    try {
      Method methodValues = clazz.getMethod("values");
      C[] v = (C[]) methodValues.invoke(null);
      values.addAll(Arrays.asList(v));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(AbstractComboBoxModel.class.getName()).log(Level.SEVERE,
                                                                  null,
                                                                  ex);
    }
    try {
      Method valueOfMagic = clazz.getMethod("valueOfMagic",
                                            int.class);
      Method valueOf = clazz.getMethod("valueOf",
                                       String.class);
      if (clazz.isAssignableFrom(valueOfMagic.getReturnType()) && Modifier.isStatic(valueOfMagic.getModifiers()) && Modifier.
              isPublic(valueOfMagic.getModifiers())) {
        mapper = (Object anItem) -> {
          if (anItem == null) {
            return null;
          } else if (clazz.isInstance(anItem)) {
            return clazz.cast(anItem);
          } else if (anItem instanceof Number) {
            try {
              return (C) valueOfMagic.invoke(null,
                                             ((Number) anItem).intValue());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              Logger.getLogger(AbstractComboBoxModel.class.getName()).log(Level.SEVERE,
                                                                          null,
                                                                          ex);
            }
          } else {
            try {
              return (C) valueOf.invoke(null,
                                        anItem.toString());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              Logger.getLogger(AbstractComboBoxModel.class.getName()).log(Level.SEVERE,
                                                                          null,
                                                                          ex);
            }
          }
          return null;
        };
      }
    } catch (NoSuchMethodException | SecurityException ex) {
      Logger.getLogger(AbstractComboBoxModel.class.getName()).log(Level.SEVERE,
                                                                  null,
                                                                  ex);
    }
    if (mapper == null) {
      try {
        Method valueOf = clazz.getMethod("valueOf",
                                         String.class);
        mapper = (anItem) -> {
          if (anItem == null) {
            return null;
          } else if (clazz.isInstance(anItem)) {
            return clazz.cast(anItem);
          } else {
            try {
              return (C) valueOf.invoke(null,
                                        anItem.toString());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              Logger.getLogger(AbstractComboBoxModel.class.getName()).log(Level.SEVERE,
                                                                          null,
                                                                          ex);
            }
          }
          return null;
        };
      } catch (NoSuchMethodException | SecurityException ex) {
        Logger.getLogger(AbstractComboBoxModel.class.getName()).log(Level.SEVERE,
                                                                    null,
                                                                    ex);
      }
    }
    return instanceOf(values,
                      mapper);
  }

  public static <C> AbstractComboBoxModel<C> instanceOf(Collection<? extends C> items,
                                                        Function<Object, C> mapper)
  {
    Objects.requireNonNull(items,
                           "items is null");
    Objects.requireNonNull(mapper,
                           "mapper is null");
    return new AbstractComboBoxModel<>(items)
    {
      @Override
      protected C toItem(Object anItem)
      {
        return mapper.apply(anItem);
      }

    };
  }

  private final List<V> values;
  private V selected;
  private final Set<ListDataListener> listener = new CopyOnWriteArraySet<>();

  public AbstractComboBoxModel(Collection<? extends V> values)
  {
    this.values = values.stream().filter((v) -> v != null).collect(Collectors.toUnmodifiableList());
  }

  protected abstract V toItem(Object anItem);

  @Override
  public void setSelectedItem(Object anItem)
  {
    V oldSelected = selected;
    selected = toItem(anItem);
    if (!Objects.equals(oldSelected,
                        selected)) {
      fireSelectionChanged();
    }
  }

  @Override
  public V getSelectedItem()
  {
    return selected;
  }

  @Override
  public int getSize()
  {
    return values.size();
  }

  @Override
  public V getElementAt(int index)
  {
    return values.get(index);
  }

  protected void fireSelectionChanged()
  {
    if (listener.isEmpty()) {
      return;
    }
    ListDataEvent evt = new ListDataEvent(this,
                                          ListDataEvent.CONTENTS_CHANGED,
                                          -1,
                                          -1);
    for (ListDataListener l : listener) {
      l.contentsChanged(evt);
    }
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
