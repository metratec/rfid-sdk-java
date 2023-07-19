/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;
import com.metratec.lib.tag.RfidTag;

/**
 * @author mn
 *
 */
public class HfTagEvent extends RfidTagEvent<HfTag> {

  /**
   *
   */
  private static final long serialVersionUID = -2623163523941887522L;

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   */
  public HfTagEvent(String identifier, HfTag tag) {
    super(identifier, tag);
  }

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   * @param timestamp event time
   */
  public HfTagEvent(String identifier, HfTag tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.event.RfidTagEvent#getTags()
   */
  @Override
  public HfTag getTag() {
    return (HfTag) super.getTag();
  }

}
