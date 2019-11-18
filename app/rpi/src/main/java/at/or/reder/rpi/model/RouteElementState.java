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
package at.or.reder.rpi.model;

/**
 *
 * @author Wolfgang Reder
 */
public enum RouteElementState
{
  /**
   * Weiche oder Gleis ist frei.
   */
  FREE,
  /**
   * Weiche ist gesperrt, aber nicht durch eine Fahrstraße belegt.
   */
  LOCKED,
  /**
   * Weiche ist durch eine Fahrstraße gesperrt, kann aber von einer anderen Fahrstraße belegt werden (Schutzweiche).
   */
  PROTECTED,
  /**
   * Weiche oder Gleis ist durch eine Fahrstraße exklusiv belegt.
   */
  OCCUPIED;
}
