/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.UhfTag;

/**
 * Event representing the detection of a UHF RFID tag.
 * This event is fired when a UHF tag is found or detected by a UHF RFID reader.
 * 
 * @author mn
 *
 */
public class UhfTagFound extends UhfTagEvent {

  private static final long serialVersionUID = -3146078949023706787L;

  /**
   * Creates a new UHF tag found event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the UHF tag that was found
   * @param timestamp the timestamp when the tag was found
   */
  public UhfTagFound(String identifier, UhfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Creates a new UHF tag found event from a generic RFID tag found event.
   * 
   * @param event the generic RFID tag found event to convert
   */
  public UhfTagFound(RfidTagFound<UhfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
 
}
