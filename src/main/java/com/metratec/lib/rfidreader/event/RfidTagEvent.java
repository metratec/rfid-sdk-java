/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.RfidTag;

/**
 * @author jannis becke
 *
 */
public class RfidTagEvent<T> extends RfidEvent {

  private static final long serialVersionUID = -4110846484368807570L;
  private T tag;

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   */
  public RfidTagEvent(String identifier, T tag) {
    this(identifier, tag, null);
  }

  /**
   * @param identifier reader identifier
   * @param tag the {@link RfidTag}
   * @param timestamp event time
   */
  public RfidTagEvent(String identifier, T tag, Long timestamp) {
    super(identifier, timestamp);
    this.tag = tag;
  }

  /**
   * @return the tags
   */
  public T getTag() {
    return tag;
  }

}
