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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeListener;

public interface Field {

  public short getAddress();

  public void addChangeListener(ChangeListener l);

  public void removeChangeListener(ChangeListener l);

  public boolean isKeyPressed();

  public Set<State> getLastState();

  public Set<State> getState() throws IOException, TimeoutException, InterruptedException;

  public int getLeds() throws IOException, TimeoutException, InterruptedException;

  public void setLeds(int leds) throws IOException, TimeoutException, InterruptedException;

  public int getBlinkMask() throws IOException, TimeoutException, InterruptedException;

  public void setBlinkMask(int bm) throws IOException, TimeoutException, InterruptedException;

  public int getBlinkPhase() throws IOException, TimeoutException, InterruptedException;

  public void setBlinkPhase(int bp) throws IOException, TimeoutException, InterruptedException;

  public int getBlinkDivider() throws IOException, TimeoutException, InterruptedException;

  public void setBlinkDivider(int blinkDivider) throws IOException, TimeoutException, InterruptedException;

  public int getPWM() throws IOException, TimeoutException, InterruptedException;

  public void setPWM(int pwm) throws IOException, TimeoutException, InterruptedException;

  public int getDefaultPWM() throws IOException, TimeoutException, InterruptedException;

  public void setDefaultPWM(int defaultPWM) throws IOException, TimeoutException, InterruptedException;

  public float getVCC() throws IOException, TimeoutException, InterruptedException;

  public int getCalibration() throws IOException, TimeoutException, InterruptedException;

  public void setCalibration(int cal) throws IOException, TimeoutException, InterruptedException;

  public int getDebounce() throws IOException, TimeoutException, InterruptedException;

  public void setDebounce(int deb) throws IOException, TimeoutException, InterruptedException;

  public ModuleType getLastModuleType();

  public ModuleType getModuleType() throws IOException, TimeoutException, InterruptedException;

  public void setModuleType(ModuleType type) throws IOException, TimeoutException, InterruptedException;

  public ModuleState getLastModuleState();

  public ModuleState getModuleState() throws IOException, TimeoutException, InterruptedException;

  public void setModuleState(ModuleState ms) throws IOException, TimeoutException, InterruptedException;

  public Version getVersion() throws IOException, TimeoutException, InterruptedException;

}
