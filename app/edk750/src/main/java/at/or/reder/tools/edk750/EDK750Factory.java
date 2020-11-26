/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import at.or.reder.controller.PSController;
import at.or.reder.dcc.Controller;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Wolfgang Reder
 */
public interface EDK750Factory
{

  public default EDK750 createInstance(int address) throws IOException
  {
    return createInstance(address,
                          null,
                          null,
                          null);
  }

  public default EDK750 createInstance(int address,
                                       Map<String, String> config) throws IOException
  {
    return createInstance(address,
                          null,
                          null,
                          config);
  }

  public default EDK750 createInstance(int address,
                                       Controller dcc,
                                       Map<String, String> config) throws IOException
  {
    return createInstance(address,
                          dcc,
                          null,
                          config);
  }

  public EDK750 createInstance(int address,
                               Controller dcc,
                               PSController ps,
                               Map<String, String> config) throws IOException;

}
