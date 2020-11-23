/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller;

@FunctionalInterface
public interface PSControllerEventListener
{

  public void onControllerEvent(PSControllerEvent evt);

}
