/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import com.metratec.lib.tag.RfidTag;


/**
 * Implementation for a periodic inventory check.<br>
 * Add the tags with {@link #addTag(RfidTag)} or {@link #addTags(List)}, then check the
 * inventory for losted tags with {@link #checkInventory()}
 * 
 * @author man
 * @param <T> {@link RfidTag} instance
 *
 */
public class InventoryWithTimestamp<T extends RfidTag> {
  // protected Logger logger = LoggerFactory.getLogger(SimpleInventory.class);
  private Hashtable<String, T> tagById = new Hashtable<>();
  private static long DEFAULT_TAG_KEEP_TIME = 5000l;
  private long keepTime;
  private String identifier;

  /**
   * @param identifier inventory identifier
   */
  public InventoryWithTimestamp(String identifier) {
    this(identifier, DEFAULT_TAG_KEEP_TIME);
  }


  /**
   * @param identifier inventory identifier
   * @param tagKeepTime the tag keep time (to disable automatic removing tags, set keep time to -1)
   */
  public InventoryWithTimestamp(String identifier, Long tagKeepTime) {
    super();
    this.identifier = identifier;
    setKeepTime(tagKeepTime);
  }

  /**
   * @param tag the tag to add
   * @return the tag last timestamp or <code>null</code> if the tag was not in inventory
   */
  public Long addTag(T tag) {
    T presentTag = tagById.get(tag.getId());
    if (null == presentTag) {
      tagById.put(tag.getId(), tag);
      return null;
    } else {
      long lastSeen = presentTag.getLastSeenTimestamp();
      presentTag.updateTag(tag);
      return lastSeen;
    }
  }

  /**
   * update the current inventory
   * 
   * @param inventory {@link List} with the founded tag ids
   * @return a {@link List} with all tags, who are new in the inventory
   */
  public List<T> addTags(List<T> inventory) {
    // add Tags
    List<T> newTags = new ArrayList<>();
    for (T tag : inventory) {
      if (null == addTag(tag)) {
        newTags.add(tag);
      }
    }
    return newTags;
  }


  /**
   * check the current inventory for tags there are no longer found
   * 
   * @return a list with removed tags
   */
  public List<T> checkInventory() {
    List<T> removedTags = new ArrayList<>();
    if (keepTime < 0) {
      // sticky...don't remove tags automatically
      return removedTags;
    }
    // check all tags
    long removeTime = System.currentTimeMillis() - keepTime;
    Enumeration<T> eKeys = tagById.elements();
    while (eKeys.hasMoreElements()) {
      T tag = eKeys.nextElement();
      if (tag.getLastSeenTimestamp() < removeTime) {
        removedTags.add(tag);
        removeTag(tag.getId());
      }
    }
    return removedTags;

  }



  /**
   * Return the current inventory
   * 
   * @return the current inventory
   */
  public List<T> getInventory() {
    List<T> inv = new ArrayList<>();
    Enumeration<T> eKeys = tagById.elements();
    while (eKeys.hasMoreElements()) {
      T tag = eKeys.nextElement();
      if (null != tag) {
        inv.add(tag);
      }
    }
    Collections.sort(inv);
    return inv;
  }

  /**
   * @return the keepTime
   */
  public long getKeepTime() {
    return keepTime;
  }

  /**
   * @param keepTime the keepTime to set
   */
  public void setKeepTime(Long keepTime) {
    if (null == keepTime) {
      this.keepTime = -1;
    } else {
      this.keepTime = keepTime;
    }

  }

  /**
   * Remove a tag from this list
   * 
   * @param tagEid tag eid to remove
   * @return the last tag timestamp
   */
  public Long removeTag(String tagEid) {
    try {
      return tagById.remove(tagEid).getLastSeenTimestamp();
    } catch (NullPointerException e) {
      return null;
    }

  }

  /**
   * 
   * @param tags {@link List} of tags
   * @return a {@link List} with removed tags
   */
  public List<String> removeTags(List<String> tags) {
    List<String> removed = new ArrayList<>();
    for (String tag : tags) {
      if (null != removeTag(tag)) {
        removed.add(tag);
      }
    }
    return removed;
  }

  /**
   * Remove all tags from the inventory
   * 
   * @return {@link List} with removed tags
   */
  public List<T> clear() {
    List<T> removedTags = new ArrayList<>();
    Enumeration<T> eKeys = tagById.elements();
    while (eKeys.hasMoreElements()) {
      T tag = eKeys.nextElement();
      removedTags.add(tag);
      removeTag(tag.getId());
    }
    return removedTags;
  }


  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }


}
