/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import java.util.ArrayList;
import java.util.List;
import com.metratec.lib.rfidreader.event.RfidEvent;
import com.metratec.lib.tag.RfidTag;

/**
 * @author man
 * @param <T> {@link RfidTag} instance
 *
 */
public class InventoryChangedEvent<T extends RfidTag> extends RfidEvent {
  private static final long serialVersionUID = -4242588366673410007L;
  private List<T> tags;
  private List<T> newTags;
  private List<T> lostTags;

  /**
   * @param identifier identifier
   * @param tags {@link List} with current tag ids
   * @param newTags {@link List} with new tag ids
   * @param lostTags {@link List} with lost tag ids
   */
  public InventoryChangedEvent(String identifier, List<T> tags, List<T> newTags, List<T> lostTags) {
    this(identifier, System.currentTimeMillis(), tags, newTags, lostTags);
  }

  /**
   * @param identifier identifier
   * @param timestamp timestamp
   * @param tags {@link List} with current tag ids
   * @param newTags {@link List} with new tag ids
   * @param lostTags {@link List} with lost tag ids
   */
  public InventoryChangedEvent(String identifier, Long timestamp, List<T> tags, List<T> newTags,
      List<T> lostTags) {
    super(identifier, timestamp);
    this.tags = null != tags ? tags : new ArrayList<>();
    this.newTags = null != newTags ? newTags : new ArrayList<>();
    this.lostTags = null != lostTags ? lostTags : new ArrayList<>();

  }

  /**
   * @return a list of tags currently in the inventory
   */
  public List<T> getTags() {
    return tags;
  }

  /**
   * @return a list of tags new in the inventory
   */
  public List<T> getNewTags() {
    return newTags;
  }

  /**
   * @return a list of tags lost in the inventory
   */
  public List<T> getLostTags() {
    return lostTags;
  }



}
