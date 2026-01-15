/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

/**
 * Event representing the loss of a High Frequency (HF) RFID tag.
 * This event is fired when an HF tag is no longer detected by an HF RFID reader.
 * 
 * @author mn
 *
 */
public class HfTagLost extends RfidTagLost<HfTag> {

  private static final long serialVersionUID = -5297586422554972508L;

  /**
   * Creates a new HF tag lost event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the HF tag that was lost
   * @param timestamp the timestamp when the tag was lost
   */
  public HfTagLost(String identifier, HfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Creates a new HF tag lost event from a generic RFID tag lost event.
   * 
   * @param event the generic RFID tag lost event to convert
   */
  public HfTagLost(RfidTagLost<HfTag> event) {
    super(event.getIdentifier(), event.getTag(), event.getTimestamp());
  }

}
