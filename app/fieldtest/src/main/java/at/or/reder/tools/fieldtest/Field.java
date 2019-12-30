/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.fieldtest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Wolfgang Reder
 */
public interface Field extends AutoCloseable
{

  public short getAddress();

  public int getLeds() throws IOException, TimeoutException, InterruptedException;

  public void setLeds(int leds) throws IOException, TimeoutException, InterruptedException;

  public int getBlinkMask() throws IOException, TimeoutException, InterruptedException;

  public void setBlinkMask(int bm) throws IOException, TimeoutException, InterruptedException;

  public int getBlinkPhase() throws IOException, TimeoutException, InterruptedException;

  public void setBlinkPhase(int bp) throws IOException, TimeoutException, InterruptedException;

  public int getPWM() throws IOException, TimeoutException, InterruptedException;

  public void setPWM(int pwm) throws IOException, TimeoutException, InterruptedException;

  public float getVCC() throws IOException, TimeoutException, InterruptedException;

  public byte getCalibration() throws IOException, TimeoutException, InterruptedException;

  public void setCalibration(byte cal) throws IOException, TimeoutException, InterruptedException;

  @Override
  public void close() throws IOException;

}
