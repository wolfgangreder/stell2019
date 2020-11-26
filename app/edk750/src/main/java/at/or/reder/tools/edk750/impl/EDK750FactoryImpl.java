/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750.impl;

import at.or.reder.controller.PSController;
import at.or.reder.dcc.Controller;
import at.or.reder.tools.edk750.EDK750;
import at.or.reder.tools.edk750.EDK750Factory;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = EDK750Factory.class)
public class EDK750FactoryImpl implements EDK750Factory
{

  @Override
  public EDK750 createInstance(int address,
                               Controller dcc,
                               PSController ps,
                               Map<String, String> config) throws IOException
  {
    return new EDK750Impl(address,
                          dcc,
                          ps,
                          config);
  }

}
