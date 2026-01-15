/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;

/**
 * Event listener interface for Mifare RFID tag events.
 * This interface extends RfidTagEventListener and provides specific handling
 * for Mifare tag found and lost events, with convenient wrapper methods.
 * 
 * @author man
 *
 */
public interface MfTagEventListener extends RfidTagEventListener<MfTag> {
  
  /**
   * Default implementation that converts generic RFID tag found events to Mifare-specific events.
   * This method automatically wraps the generic event and calls the Mifare-specific tagFound method.
   * 
   * @param event the generic RFID tag found event to convert
   */
  @Override
  default void tagFound(RfidTagFound<MfTag> event) {
    tagFound(new MfTagFound(event));
  }

  /**
   * Default implementation that converts generic RFID tag lost events to Mifare-specific events.
   * This method automatically wraps the generic event and calls the Mifare-specific tagLost method.
   * 
   * @param event the generic RFID tag lost event to convert
   */
  @Override
  default void tagLost(RfidTagLost<MfTag> event) {
    tagLost(new MfTagLost(event));
  }

  /**
   * Called when a Mifare tag is found or detected by the reader.
   * Implement this method to handle Mifare tag detection events.
   * 
   * @param event the Mifare tag found event containing tag details
   */
  void tagFound(MfTagFound event);

  /**
   * Called when a Mifare tag is lost or no longer detected by the reader.
   * Implement this method to handle Mifare tag loss events.
   * 
   * @param event the Mifare tag lost event containing tag details
   */
  void tagLost(MfTagLost event);

}
