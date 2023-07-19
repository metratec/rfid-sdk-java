/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * @author jannis becke
 *
 */
public interface RfidReaderEventListener {

  /**
   * reader input changed (only if the reader has inputs and supports this)
   * 
   * @param event {@link RfidReaderInputChange}
   */
  default void inputChange(RfidReaderInputChange event) {}

  /**
   * reader connection state changed
   * 
   * @param event {@link RfidReaderConnectionState}
   */
  void connectionState(RfidReaderConnectionState event);

}
