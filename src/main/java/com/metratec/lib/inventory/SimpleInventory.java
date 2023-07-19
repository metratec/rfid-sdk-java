/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory;

import java.util.ArrayList;
import java.util.List;
import com.metratec.lib.tag.RfidTag;

/**
 * Simple inventory implementation - designed for update an inventory with a list of all
 * transponder. Than you can get the added and removed transponder.
 * 
 * @author mn
 * @param <T> {@link RfidTag} instance
 *
 */
public class SimpleInventory<T extends RfidTag> {
  private List<T> inventory = new ArrayList<>();
  private List<T> addedTags = new ArrayList<>();
  private List<T> removedTags = new ArrayList<>();

  /**
   * Create an new instance. <br>
   * Usage: <br>
   * update an inventory with a list of all transponder ({@link #updateInventory(List)}. Than you
   * can get the added ({@link #getAddedTags()} and removed ({@link #getRemovedTags()} transponder.
   */
  public SimpleInventory() {}

  /**
   * update the current inventory and calculate the removed and added tags
   * 
   * @param tags new tags
   */
  public void updateInventory(List<T> tags) {
    List<T> oldInventory = inventory;
    addedTags = new ArrayList<>(tags);
    addedTags.removeAll(oldInventory);
    removedTags = new ArrayList<>(oldInventory);
    removedTags.removeAll(tags);
    inventory = new ArrayList<>(tags);
    for (RfidTag oldEntry : oldInventory) {
      for (RfidTag tag : inventory) {
        if (oldEntry.getId().equals(tag.getId())) {
          tag.setFirstSeenTimestamp(oldEntry.getFirstSeenTimestamp());
          tag.setSeenCount(oldEntry.getSeenCount() + tag.getSeenCount());
        }
      }
    }
  }

  /**
   * @return the current inventory
   */
  public List<T> getInventory() {
    return inventory;
  }

  /**
   * @return the added Tags
   */
  public List<T> getAddedTags() {
    return addedTags;
  }

  /**
   * @return the removed Tags
   */
  public List<T> getRemovedTags() {
    return removedTags;
  }


}
