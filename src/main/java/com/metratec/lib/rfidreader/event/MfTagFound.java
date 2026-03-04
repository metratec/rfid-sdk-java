/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;

/**
 * Event representing the detection of a Mifare RFID tag.
 * This event is fired when a Mifare tag is found or detected by a Mifare-compatible RFID reader.
 * 
 * @author mn
 *
 */
public class MfTagFound extends MfTagEvent {

  private static final long serialVersionUID = 983614753433053502L;

  /**
   * Creates a new Mifare tag found event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the Mifare tag that was found
   * @param timestamp the timestamp when the tag was found
   */
  public MfTagFound(String identifier, MfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Creates a new Mifare tag found event from a generic RFID tag found event.
   * 
   * @param event the generic RFID tag found event to convert
   */
  public MfTagFound(RfidTagFound<MfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
}
