/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;

/**
 * Event representing the loss of a Mifare RFID tag.
 * This event is fired when a Mifare tag is no longer detected by a Mifare-compatible RFID reader.
 * 
 * @author mn
 *
 */
public class MfTagLost extends MfTagEvent {

  private static final long serialVersionUID = 4477252752848578861L;

  /**
   * Creates a new Mifare tag lost event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the Mifare tag that was lost
   * @param timestamp the timestamp when the tag was lost
   */
  public MfTagLost(String identifier, MfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Creates a new Mifare tag lost event from a generic RFID tag lost event.
   * 
   * @param event the generic RFID tag lost event to convert
   */
  public MfTagLost(RfidTagLost<MfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }
}
