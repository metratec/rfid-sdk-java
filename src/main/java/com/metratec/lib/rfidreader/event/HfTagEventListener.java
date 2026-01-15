/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

/**
 * Event listener interface for High Frequency (HF) RFID tag events.
 * This interface extends RfidTagEventListener and provides specific handling
 * for HF tag found and lost events, with convenient wrapper methods.
 * 
 * @author man
 *
 */
public interface HfTagEventListener extends RfidTagEventListener<HfTag>  {

  /**
   * Default implementation that converts generic RFID tag found events to HF-specific events.
   * This method automatically wraps the generic event and calls the HF-specific tagFound method.
   * 
   * @param event the generic RFID tag found event to convert
   */
  @Override
  default void tagFound(RfidTagFound<HfTag> event) {
    tagFound(new HfTagFound(event));
  }

  /**
   * Default implementation that converts generic RFID tag lost events to HF-specific events.
   * This method automatically wraps the generic event and calls the HF-specific tagLost method.
   * 
   * @param event the generic RFID tag lost event to convert
   */
  @Override
  default void tagLost(RfidTagLost<HfTag> event) {
    tagLost(new HfTagLost(event));
  }

  /**
   * Called when an HF tag is found or detected by the reader.
   * Implement this method to handle HF tag detection events.
   * 
   * @param tag the HF tag found event containing tag details
   */
  void tagFound(HfTagFound tag);

  /**
   * Called when an HF tag is lost or no longer detected by the reader.
   * Implement this method to handle HF tag loss events.
   * 
   * @param tag the HF tag lost event containing tag details
   */
  void tagLost(HfTagLost tag);

}
