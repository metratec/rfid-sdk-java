/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

/**
 * Event representing a High Frequency (HF) RFID tag occurrence.
 * This event is fired when HF tags are detected, lost, or undergo
 * other state changes in HF/ISO RFID readers.
 * 
 * @author mn
 *
 */
public class HfTagEvent extends RfidTagEvent<HfTag> {

  /**
   *
   */
  private static final long serialVersionUID = -2623163523941887522L;

  /**
   * Creates a new HF tag event with the current timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the HF tag associated with this event
   */
  public HfTagEvent(String identifier, HfTag tag) {
    super(identifier, tag);
  }

  /**
   * Creates a new HF tag event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the HF tag associated with this event
   * @param timestamp the timestamp when the event occurred
   */
  public HfTagEvent(String identifier, HfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /**
   * Gets the HF tag associated with this event.
   * 
   * @return the HF tag that triggered this event
   */
  @Override
  public HfTag getTag() {
    return (HfTag) super.getTag();
  }

}
