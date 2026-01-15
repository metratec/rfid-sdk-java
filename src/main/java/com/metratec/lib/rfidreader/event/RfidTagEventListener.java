/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * Event listener interface for RFID tag events.
 * This interface defines the basic contract for handling tag found and lost events.
 * 
 * @param <T> the type of RFID tag handled by this listener
 * @author jannis becke
 *
 */
public interface RfidTagEventListener<T> {

  /**
   * Called when an RFID tag is found or detected by the reader.
   * Implement this method to handle tag detection events.
   * 
   * @param tag the RFID tag found event containing tag details
   */
  void tagFound(RfidTagFound<T> tag);

  /**
   * Called when an RFID tag is lost or no longer detected by the reader.
   * Implement this method to handle tag loss events.
   * 
   * @param tag the RFID tag lost event containing tag details
   */
  void tagLost(RfidTagLost<T> tag);
}
