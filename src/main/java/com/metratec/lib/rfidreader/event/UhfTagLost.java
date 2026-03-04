/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.UhfTag;

/**
 * Event representing the loss of a UHF RFID tag.
 * This event is fired when a UHF tag is no longer detected by a UHF RFID reader.
 * 
 * @author mn
 *
 */
public class UhfTagLost extends UhfTagEvent {

  private static final long serialVersionUID = 7278059847916249298L;

  /**
   * Creates a new UHF tag lost event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the UHF tag that was lost
   * @param timestamp the timestamp when the tag was lost
   */
  public UhfTagLost(String identifier, UhfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Creates a new UHF tag lost event from a generic RFID tag lost event.
   * 
   * @param event the generic RFID tag lost event to convert
   */
  public UhfTagLost(RfidTagLost<UhfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }

  
  
}
