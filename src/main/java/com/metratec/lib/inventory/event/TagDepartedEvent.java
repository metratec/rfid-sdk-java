/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import com.metratec.lib.tag.RfidTag;

/**
 * A event which is called if a tags is no longer in the inventory
 * 
 * @author man
 * @param <T> {@link RfidTag} instance
 */
public class TagDepartedEvent<T extends RfidTag> extends TagEvent<T> {

  private static final long serialVersionUID = -7236468322924638104L;

  /**
   * @param identifier identifier
   * @param tag tag
   */
  public TagDepartedEvent(String identifier, T tag) {
    this(identifier, null, tag);

  }

  /**
   * @param identifier identifier
   * @param timestamp timestamp
   * @param tag tag
   */
  public TagDepartedEvent(String identifier, Long timestamp, T tag) {
    super(identifier, timestamp, tag);
  }
}
