/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import com.metratec.lib.tag.RfidTag;

/**
 * A event which is called if a new tags was found in the inventory
 * 
 * @author man
 * @param <T> {@link RfidTag} instance
 */
public class TagArrivedEvent<T extends RfidTag> extends TagEvent<T> {

  private static final long serialVersionUID = 2216014109897729470L;

  /**
   * @param identifier identifier
   * @param tag tag
   */
  public TagArrivedEvent(String identifier, T tag) {
    this(identifier, null, tag);

  }

  /**
   * @param identifier identifier
   * @param timestamp timestamp
   * @param tag tag
   */
  public TagArrivedEvent(String identifier, Long timestamp, T tag) {
    super(identifier, timestamp, tag);
  }

}
