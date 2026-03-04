/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

/**
 * Event representing the detection of a High Frequency (HF) RFID tag.
 * This event is fired when an HF tag is found or detected by an HF RFID reader.
 * 
 * @author mn
 *
 */
public class HfTagFound extends HfTagEvent {

  private static final long serialVersionUID = 6078620849415075887L;

  /**
   * Creates a new HF tag found event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the HF tag that was found
   * @param timestamp the timestamp when the tag was found
   */
  public HfTagFound(String identifier, HfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Creates a new HF tag found event from a generic RFID tag found event.
   * 
   * @param event the generic RFID tag found event to convert
   */
  public HfTagFound(RfidTagFound<HfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
}
