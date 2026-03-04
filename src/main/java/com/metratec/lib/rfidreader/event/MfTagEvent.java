/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;
import com.metratec.lib.tag.RfidTag;

/**
 * Event representing a Mifare RFID tag occurrence.
 * This event is fired when Mifare tags are detected, lost, or undergo
 * other state changes in Mifare-compatible RFID readers.
 * 
 * @author mn
 *
 */
public class MfTagEvent extends RfidTagEvent<MfTag> {

  private static final long serialVersionUID = -7407574367012483388L;

  /**
   * @param identifier reader identifier
   * @param tag {@link RfidTag}
   */
  public MfTagEvent(String identifier, MfTag tag) {
    super(identifier, tag);
  }

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   * @param timestamp event time
   */
  public MfTagEvent(String identifier, MfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.event.RfidTagEvent#getTags()
   */
  @Override
  public MfTag getTag() {
    return (MfTag) super.getTag();
  }

}
