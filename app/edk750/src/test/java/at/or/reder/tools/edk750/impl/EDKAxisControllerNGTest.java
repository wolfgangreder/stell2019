/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750.impl;

import at.or.reder.dcc.DCCConstants;
import at.or.reder.dcc.Direction;
import at.or.reder.tools.edk750.DummyLoco;
import java.io.IOException;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Wolfgang Reder
 */
public class EDKAxisControllerNGTest
{

  public EDKAxisControllerNGTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  private String buildString(int f1,
                             boolean s1,
                             int f2,
                             boolean s2,
                             int fL,
                             boolean sl,
                             int fl2,
                             boolean sl2)
  {
    StringBuilder builder = new StringBuilder("0000> ");
    for (int i = 0; i < DCCConstants.NUM_FUNCTION; ++i) {
      if (i == f1) {
        builder.append(s1 ? '1' : '0');
      } else if (i == f2) {
        builder.append(s2 ? '1' : '0');
      } else if (i == fL) {
        builder.append(sl ? '1' : '0');
      } else if (i == fl2) {
        builder.append(sl2 ? '1' : '0');
      } else {
        builder.append('x');
      }
    }
    return builder.toString();
  }

//  @Test
  public void testGetBeamShift() throws IOException
  {
    final int funcPos = 5;
    final int funcNeg = 4;
    final int funcLo = 25;
    final int funcLo2 = 26;
    DummyLoco loc = new DummyLoco();
    loc.control(Direction.FORWARD,
                0);
    EDKAxisController ctrl = EDKAxisController.getBeamShift(loc);
    ctrl.stop();
    String result = loc.toString();
    String exp = buildString(funcNeg,
                             false,
                             funcPos,
                             false,
                             funcLo,
                             false,
                             funcLo2,
                             false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      false,
                      funcLo2,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      false,
                      funcLo2,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      false,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      false,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(EDKAxisController.LIMIT_1 - 1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      false,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-EDKAxisController.LIMIT_1 + 1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      false,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(EDKAxisController.LIMIT_1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      true,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-EDKAxisController.LIMIT_1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      true,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(EDKAxisController.LIMIT_2 - 1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      true,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-EDKAxisController.LIMIT_2 + 1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      true,
                      funcLo2,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(EDKAxisController.LIMIT_2);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      true,
                      funcLo2,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-EDKAxisController.LIMIT_2);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      true,
                      funcLo2,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(EDKAxisController.LIMIT_3 - 1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      funcLo,
                      false,
                      funcLo2,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-EDKAxisController.LIMIT_3 + 1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      funcLo,
                      false,
                      funcLo2,
                      false);
    assertEquals(exp,
                 result);
  }

  @Test
  public void testGetBeam() throws IOException
  {
  }

  @Test
  public void testGetWinch() throws IOException
  {
  }

}
