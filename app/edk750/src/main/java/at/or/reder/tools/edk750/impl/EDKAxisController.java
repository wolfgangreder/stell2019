/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750.impl;

import at.or.reder.dcc.Locomotive;
import java.io.IOException;

/* EDK 750
F3 -> Seilwinde Heben F28==1/F27==1/F27==0/F28==0 -> RY+
F6 -> Seilwinde Senken F28==1/F27==1/F27==0/F28==0 -> RY-
F4 -> Ausleger ausfahren F26==1/F25==1/F25==0/F26==0 -> LY-
F5 -> Ausleger einfahren F26==1/F25==1/F25==0/F26==0 -> LY+
 4  Volle geschwindigkeit F25=0,F26=0
3 F25=1,F26=0
2 F25=1,F26=1
1   Kleinste geschw.  F26=1
F7 -> Ausleger senken F24==1/F23==1/F23==0/F24==0 -> LX-
F8 -> Ausleger heben F24==1/F23==1/F23==0/F24==0 -> LX+
F9 -> Halbgeschwindigkeit F3-F8
 */
public final class EDKAxisController
{

  public static final int LIMIT_1 = 300;
  public static final int LIMIT_2 = 600;
  public static final int LIMIT_3 = 900;

  public static EDKAxisController getBeamShift(Locomotive loc)
  {
    return new EDKAxisController(loc,
                                 4,
                                 5,
                                 25);
  }

  public static EDKAxisController getBeam(Locomotive loc)
  {
    return new EDKAxisController(loc,
                                 7,
                                 8,
                                 23);
  }

  public static EDKAxisController getWinch(Locomotive loc)
  {
    return new EDKAxisController(loc,
                                 6,
                                 3,
                                 27);
  }

  private Locomotive loc;
  private final int onNeg;
  private final int onPos;
  private final int lo;
  private int currentPos;

  private EDKAxisController(Locomotive loc,
                            int onNeg,
                            int onPos,
                            int lo)
  {
    this.loc = loc;
    this.onNeg = onNeg;
    this.onPos = onPos;
    this.lo = lo;
  }

  public Locomotive getLoc()
  {
    return loc;
  }

  public void setLoc(Locomotive loc)
  {
    this.loc = loc;
  }

  private int getPos(int value)
  {
    if (value == 0) {
      return 0;
    } else if (value > 0) {
      if (value > LIMIT_3) {
        return 4;
      } else if (value > LIMIT_2) {
        return 3;
      } else if (value > LIMIT_1) {
        return 2;
      } else {
        return 1;
      }
    } else {
      if (value < -LIMIT_3) {
        return -4;
      } else if (value < -LIMIT_2) {
        return -3;
      } else if (value < -LIMIT_1) {
        return -2;
      } else {
        return -1;
      }
    }
  }

  public void stop() throws IOException
  {
    currentPos = 0;
    if (loc != null) {
      loc.setFunction(onNeg,
                      false);
      loc.setFunction(onPos,
                      false);
      loc.setFunction(lo,
                      false);
      loc.setFunction(lo + 1,
                      false);
    }
  }

  public void setSpeed(int speed) throws IOException
  {
    if (loc == null) {
      return;
    }
    int pos = getPos(speed);
    if (currentPos != pos) {
      if (pos == 0) {
        stop();
      } else {
        int apos = Math.abs(pos);
        loc.setFunction(lo,
                        apos == 3 || apos == 2);
        loc.setFunction(lo + 1,
                        apos == 1 || apos == 2);
        loc.setFunction(onNeg,
                        pos < 0);
        loc.setFunction(onPos,
                        pos > 0);
        currentPos = pos;
      }
    }
  }

}
