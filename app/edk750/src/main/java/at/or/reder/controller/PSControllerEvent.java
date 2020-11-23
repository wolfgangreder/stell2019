/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller;

import java.util.EventObject;

public final class PSControllerEvent extends EventObject
{

  private final PSController controller;
  private final PSControllerEventType type;

  public PSControllerEvent(PSControllerFactory source,
                           PSControllerEventType type,
                           PSController controller)
  {
    super(source);
    this.type = type;
    this.controller = controller;
  }

  public PSController getController()
  {
    return controller;
  }

  public PSControllerEventType getType()
  {
    return type;
  }

  @Override
  public PSControllerFactory getSource()
  {
    return (PSControllerFactory) super.getSource();
  }

}
