/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.UhfTag;

/**
 * Event listener interface for Ultra High Frequency (UHF) RFID tag events.
 * This interface extends RfidTagEventListener and provides specific handling
 * for UHF tag found and lost events, with convenient wrapper methods.
 * 
 * @author man
 *
 */
public interface UhfTagEventListener extends RfidTagEventListener<UhfTag> {

  /**
   * Default implementation that converts generic RFID tag found events to UHF-specific events.
   * This method automatically wraps the generic event and calls the UHF-specific tagFound method.
   * 
   * @param event the generic RFID tag found event to convert
   */
  @Override
  default void tagFound(RfidTagFound<UhfTag> event) {
    tagFound(new UhfTagFound(event));
  }

  /**
   * Default implementation that converts generic RFID tag lost events to UHF-specific events.
   * This method automatically wraps the generic event and calls the UHF-specific tagLost method.
   * 
   * @param event the generic RFID tag lost event to convert
   */
  @Override
  default void tagLost(RfidTagLost<UhfTag> event) {
    tagLost(new UhfTagLost(event));
  }
  
  /**
   * Called when a UHF tag is found or detected by the reader.
   * Implement this method to handle UHF tag detection events.
   * 
   * @param event the UHF tag found event containing tag details
   */
  void tagFound(UhfTagFound event);

  /**
   * Called when a UHF tag is lost or no longer detected by the reader.
   * Implement this method to handle UHF tag loss events.
   * 
   * @param event the UHF tag lost event containing tag details
   */
  void tagLost(UhfTagLost event);

}
