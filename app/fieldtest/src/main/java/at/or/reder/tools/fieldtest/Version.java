/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

import java.text.MessageFormat;

/**
 *
 * @author Wolfgang Reder
 */
public final class Version implements Comparable<Version>
{

  private static final String FMT_VERSION = "{0,number,0}.{1,number,0}.{2,number,0}";
  private final int major;
  private final int minor;
  private final int build;
  private String string;

  public Version(int version,
                 int build)
  {
    this.minor = (version & 0xff00) >> 8;
    this.major = version & 0xff;
    this.build = build;
  }

  public int getMajor()
  {
    return major;
  }

  public int getMinor()
  {
    return minor;
  }

  public int getBuild()
  {
    return build;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 61 * hash + this.major;
    hash = 61 * hash + this.minor;
    hash = 61 * hash + this.build;
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
    if (this.minor != other.minor) {
      return false;
    }
    return this.build == other.build;
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
    if (result == 0) {
      result = Integer.compare(build,
                               o.build);
    }
    return result;
  }

  @Override
  public synchronized String toString()
  {
    if (string == null) {
      string = MessageFormat.format(FMT_VERSION,
                                    major,
                                    minor,
                                    build);
    }
    return string;
  }

}