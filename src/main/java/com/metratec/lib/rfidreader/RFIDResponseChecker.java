/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;


/**
 * @author jannis becke
 *
 */
public interface RFIDResponseChecker {

  /**
   * @param response reader response
   * @return true if the reponse is of interest
   */
  boolean isResponseInteresting(String response);


}
