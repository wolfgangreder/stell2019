/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import at.or.reder.dcc.Controller;
import at.or.reder.dcc.Direction;
import at.or.reder.dcc.Locomotive;
import at.or.reder.zcan20.ZCAN;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Wolfgang Reder
 */
public class DummyLoco implements Locomotive
{

  private Integer speed;
  private Direction dir;
  private final SortedMap<Integer, Integer> functions = new TreeMap<>();

  public DummyLoco()
  {
  }

  @Override
  public Controller getController()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void close()
  {
  }

  @Override
  public boolean isOwner()
  {
    return true;
  }

  @Override
  public void takeOwnership()
  {
  }

  @Override
  public int getAddress()
  {
    return 0;
  }

  @Override
  public Integer getCurrentSpeed()
  {
    return speed;
  }

  @Override
  public Direction getDirection()
  {
    return dir;
  }

  @Override
  public void control(Direction dir,
                      int speed)
  {
    this.speed = speed;
    this.dir = dir;
  }

  @Override
  public SortedMap<Integer, Integer> getFunctions()
  {
    return functions;
  }

  @Override
  public void setFunctions(Map<Integer, Integer> functions)
  {
    this.functions.putAll(functions);
  }

  public void clearFunctions()
  {
    this.functions.clear();
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(MessageFormat.format("{0,number,0000}{1}",
                                        speed != null ? speed : 0,
                                        dir != null && dir == Direction.REVERSE ? '<' : '>'));
    builder.append(' ');
    for (int i = 0; i < ZCAN.NUM_FUNCTION; ++i) {
      Integer v = functions.get(i);
      if (v != null) {
        builder.append(v != 0 ? '1' : '0');
      } else {
        builder.append('x');
      }
    }
    return builder.toString();
  }

  @Override
  public byte readCV(int cvIndex,
                     int timeout)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void clearCV()
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void scanSpeed() throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void scanFunctions() throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
