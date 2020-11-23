/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller;

import java.util.List;

/**
 *
 * @author Wolfgang Reder
 */
public interface PSControllerFactory
{

  public void startScanning();

  public void stopScanning();

  public void scan();

  public void addPSControllerEventListener(PSControllerEventListener l);

  public void removePSControllerEventListener(PSControllerEventListener l);

  List<PSController> getConnectedController();

}
