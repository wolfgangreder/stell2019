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
package at.or.reder.rpi.field;

import java.text.MessageFormat;

public final class Version implements Comparable<Version>
{

  private static final String FMT_VERSION = "{0,number,0}.{1,number,0}";
  private final int major;
  private final int minor;
  private String string;

  public Version(int version)
  {
    this.minor = (version & 0xff00) >> 8;
    this.major = version & 0xff;
  }

  public int getMajor()
  {
    return major;
  }

  public int getMinor()
  {
    return minor;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 61 * hash + this.major;
    hash = 61 * hash + this.minor;
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
    final Version other = (Version) obj;
    if (this.major != other.major) {
      return false;
    }
    return this.minor == other.minor;
  }

  @Override
  public int compareTo(Version o)
  {
    if (o == null) {
      return 1;
    }
    int result = Integer.compare(major,
                                 o.major);
    if (result == 0) {
      result = Integer.compare(minor,
                               o.minor);
    }
    return result;
  }

  @Override
  public synchronized String toString()
  {
    if (string == null) {
      string = MessageFormat.format(FMT_VERSION,
                                    major,
                                    minor);
    }
    return string;
  }

}
