/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import java.util.EventListener;

public interface EDKAxisListener extends EventListener
{

  public void onAxisMove(EDKAxisEvent event);

}
