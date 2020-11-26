/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750.impl;

import at.or.reder.dcc.Direction;
import at.or.reder.tools.edk750.DummyLoco;
import at.or.reder.zcan20.ZCAN;
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
                             int f3,
                             boolean s3)
  {
    StringBuilder builder = new StringBuilder("0000> ");
    for (int i = 0; i < ZCAN.NUM_FUNCTION; ++i) {
      if (i == f1) {
        builder.append(s1 ? '1' : '0');
      } else if (i == f2) {
        builder.append(s2 ? '1' : '0');
      } else if (i == f3) {
        builder.append(s3 ? '1' : '0');
      } else {
        builder.append('x');
      }
    }
    return builder.toString();
  }

  @Test
  public void testGetBeamShift() throws IOException
  {
    final int funcPos = 5;
    final int funcNeg = 4;
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
                             9,
                             false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(511);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-511);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(512);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-512);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      false);
    assertEquals(exp,
                 result);
  }

  @Test
  public void testGetBeam() throws IOException
  {
    final int funcPos = 8;
    final int funcNeg = 7;
    DummyLoco loc = new DummyLoco();
    loc.control(Direction.FORWARD,
                0);
    EDKAxisController ctrl = EDKAxisController.getBeam(loc);
    ctrl.stop();
    String result = loc.toString();
    String exp = buildString(funcNeg,
                             false,
                             funcPos,
                             false,
                             9,
                             false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(511);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-511);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(512);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-512);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      false);
    assertEquals(exp,
                 result);
  }

  @Test
  public void testGetWinch() throws IOException
  {
    final int funcPos = 3;
    final int funcNeg = 6;
    DummyLoco loc = new DummyLoco();
    loc.control(Direction.FORWARD,
                0);
    EDKAxisController ctrl = EDKAxisController.getWinch(loc);
    ctrl.stop();
    String result = loc.toString();
    String exp = buildString(funcNeg,
                             false,
                             funcPos,
                             false,
                             9,
                             false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1023);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(1);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(511);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-511);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      true);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(512);
    result = loc.toString();
    exp = buildString(funcNeg,
                      false,
                      funcPos,
                      true,
                      9,
                      false);
    assertEquals(exp,
                 result);
    ctrl.setSpeed(-512);
    result = loc.toString();
    exp = buildString(funcNeg,
                      true,
                      funcPos,
                      false,
                      9,
                      false);
    assertEquals(exp,
                 result);
  }

  @Test
  public void testStop() throws Exception
  {
  }

  @Test
  public void testSetSpeed() throws Exception
  {
  }

}
