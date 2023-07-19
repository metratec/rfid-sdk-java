/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import com.metratec.lib.rfidreader.event.RfidEvent;
import com.metratec.lib.tag.RfidTag;

/**
 * A tag event
 * 
 * @author man
 * @param <T> {@link RfidTag} instance
 */
public abstract class TagEvent<T extends RfidTag> extends RfidEvent {
  private static final long serialVersionUID = 492023274691169544L;
  private T tag;

  /**
   * @param identifier identifier
   * @param tag tag
   */
  public TagEvent(String identifier, T tag) {
    this(identifier, null, tag);
  }

  /**
   * @param identifier identifier
   * @param timestamp timestamp
   * @param tag tag
   */
  public TagEvent(String identifier, Long timestamp, T tag) {
    super(identifier, timestamp);
    this.tag = tag;
  }

  /**
   * @return the tag
   */
  public T getTag() {
    return tag;
  }

}
