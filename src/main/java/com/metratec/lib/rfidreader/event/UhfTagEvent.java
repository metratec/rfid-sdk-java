/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.RfidTag;
import com.metratec.lib.tag.UhfTag;

/**
 * @author mn
 *
 */
public class UhfTagEvent extends RfidTagEvent<UhfTag> {

  private static final long serialVersionUID = -3400278872626852521L;

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   */
  public UhfTagEvent(String identifier, UhfTag tag) {
    super(identifier, tag);
  }

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   * @param timestamp event time
   */
  public UhfTagEvent(String identifier, UhfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.event.RfidTagEvent#getTags()
   */
  @Override
  public UhfTag getTag() {
    return (UhfTag) super.getTag();
  }

}
