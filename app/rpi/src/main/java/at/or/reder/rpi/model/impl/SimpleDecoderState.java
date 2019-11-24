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

import at.or.reder.rpi.model.DecoderState;

/**
 *
 * @author Wolfgang Reder
 */
public final class SimpleDecoderState implements DecoderState
{

  private final short address;
  private final byte port;
  private final byte state;

  public SimpleDecoderState(short address,
                            byte port,
                            int state)
  {
    this.address = address;
    this.port = port;
    this.state = (byte) state;
  }

  @Override
  public short getDecoderAddress()
  {
    return address;
  }

  @Override
  public byte getPort()
  {
    return port;
  }

  @Override
  public byte getState()
  {
    return state;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 97 * hash + this.address;
    hash = 97 * hash + this.port;
    hash = 97 * hash + this.state;
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleDecoderState other = (SimpleDecoderState) obj;
    if (this.address != other.address) {
      return false;
    }
    if (this.port != other.port) {
      return false;
    }
    return this.state == other.state;
  }

  @Override
  public String toString()
  {
    return "SimpleDecoderState{" + "address=" + address + ", port=" + port + ", state=" + state + '}';
  }

}
