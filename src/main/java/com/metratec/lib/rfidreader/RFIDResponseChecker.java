/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;


/**
 * Interface for checking whether RFID reader responses are of interest.
 * Implementations of this interface can be used to filter reader responses
 * and determine which ones should be processed further.
 * 
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
