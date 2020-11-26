/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.tools.edk750;

import java.util.EventListener;

/**
 *
 * @author Wolfgang Reder
 */
public interface EDKExceptionListener extends EventListener
{

  public void onEDKException(EDK750 sender,
                             String context,
                             Throwable error);

}
