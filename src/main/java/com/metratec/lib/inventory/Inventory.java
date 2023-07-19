/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metratec.lib.inventory.event.DummyInventoryListener;
import com.metratec.lib.inventory.event.InventoryChangedEvent;
import com.metratec.lib.inventory.event.InventoryListener;
import com.metratec.lib.inventory.event.TagArrivedEvent;
import com.metratec.lib.inventory.event.TagDepartedEvent;
import com.metratec.lib.tag.RfidTag;


/**
 * Inventory which can throw events.<br>
 * First create the inventory and than start it with {@link #start()}. Then you cann add tags with
 * {@link #updateInventory(Collection)}. The inventory object will call the
 * <ul>
 * <li>{@link InventoryListener#tagArrive(TagArrivedEvent)} method if new tags are found</li>
 * <li>{@link InventoryListener#tagDeparted(TagDepartedEvent)} method if tags are lost
 * <li>{@link InventoryListener#inventoryChanged(InventoryChangedEvent)} method if the inventory is changed.</li>
 * </ul>
 * 
 * @author man
 * @param <T> {@link RfidTag} instance
 *
 */
public class Inventory<T extends RfidTag> {
  private Logger logger = LoggerFactory.getLogger(Inventory.class);
  private Hashtable<String, T> tagById = new Hashtable<>();
  private static long DEFAULT_TAG_KEEP_TIME = 5000L;
  private static long IS_STICKY = 0L;
  private long keepTime;
  private InventoryListener<T> changeListener;
  // private boolean isSticky = false;
  private Lock inventoryLock = new ReentrantLock();
  private String identifier;
  private Thread internalThread;
  private boolean isRunning = false;

  /**
   * Create a new inventory
   * 
   * @param identifier inventory identifier
   */
  public Inventory(String identifier) {
    this(identifier, null);
  }

  /**
   * Create a new inventory
   * 
   * @param identifier inventory identifier
   * @param listener the listener
   */
  public Inventory(String identifier, InventoryListener<T> listener) {
    this(identifier, listener, DEFAULT_TAG_KEEP_TIME);
  }

  /**
   * Create a new inventory
   * 
   * @param identifier inventory identifier
   * @param listener the listener
   * @param tagKeepTime the tag keep time (to disable automatic removing tags, set keep time to 0)
   */
  public Inventory(String identifier, InventoryListener<T> listener, long tagKeepTime) {
    super();
    this.identifier = identifier;
    addListener(listener);
    setKeepTime(tagKeepTime);
  }


  /**
   * initialise the inventory
   * 
   * @param inventory tags
   */
  public void initInventory(List<T> inventory) {
    if (null == inventory) {
      return;
    }
    for (T tag : inventory) {
      tagById.put(tag.getId(), tag);
    }
  }

  /**
   * update the current inventory
   * 
   * @param inventory {@link Collection} with the tag ids
   */
  public void updateInventory(Collection<T> inventory) {
    // add Tags
    if (null == inventory) {
      return;
    }
    List<T> newTags = new ArrayList<>();
    inventoryLock.lock();
    try {
      for (T tag : inventory) {
        try {
          T presentTag = tagById.get(tag.getId());
          if (null == presentTag) {
            newTags.add(tag);
            addTagToInventory(tag);
          } else {
            // update tag
            presentTag.updateTag(tag);
          }
        } catch (NullPointerException e) {
          if (null != tag) {
            throw e;
          }
        }
      }
    } finally {
      inventoryLock.unlock();
    }
    // clear inventory
    List<T> removedTags = checkInventory();
    if (!newTags.isEmpty() || !removedTags.isEmpty()) {
      changeListener.inventoryChanged(
          new InventoryChangedEvent<>(identifier, new ArrayList<>(tagById.values()), newTags, removedTags));
    }
  }

  public void addTag(T tag) {
    inventoryLock.lock();
    try {
      addTagToInventory(tag);
    } finally {
      inventoryLock.unlock();
    }
  }

  private void addTagToInventory(T tag) {
    tagById.put(tag.getId(), tag);
    changeListener.tagArrive(new TagArrivedEvent<>(identifier, tag.getFirstSeenTimestamp(), tag));
  }

  /**
   * check the current inventory
   */
  private void checkingInventory() {
    isRunning = true;
    long nextCheckTime;
    while (isRunning) {
      if (tagById.isEmpty()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        continue;
      }
      if (logger.isTraceEnabled()) {
        logger.trace("Check inventory " + tagById.keySet());
      }
      List<T> removedTags = checkInventory();
      if (null != removedTags && !removedTags.isEmpty()) {
        changeListener.inventoryChanged(
            new InventoryChangedEvent<>(identifier, new ArrayList<>(tagById.values()), null, removedTags));
      }
      // calculate next check time
      nextCheckTime = System.currentTimeMillis();
      for (T tag : tagById.values()) {
        if (tag.getLastSeenTimestamp() < nextCheckTime) {
          nextCheckTime = tag.getLastSeenTimestamp();
        }
      }
      nextCheckTime += keepTime;
      while (System.currentTimeMillis() < nextCheckTime && isRunning) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
      }

    }
    // else {
    // // do nothing...tag are only removed if it is found by another readpoint (with removeTag -
    // // method)
    // }
  }


  /**
   * check the current inventory for old tags
   * 
   * @return a list with removed tags
   */
  protected List<T> checkInventory() {
    List<T> removedTags = new ArrayList<>();
    if (keepTime == IS_STICKY) {
      // sticky...don't remove tags automatically
      return removedTags;
    }
    inventoryLock.lock();
    try {
      // check all tags
      long removeTime = System.currentTimeMillis() - keepTime;
      Enumeration<T> eKeys = tagById.elements();
      while (eKeys.hasMoreElements()) {
        T tag = eKeys.nextElement();
        if (tag.getLastSeenTimestamp() < removeTime) {
          removedTags.add(tag);
          removeTagFromInventory(tag);
        }
      }
      return removedTags;
    } finally {
      inventoryLock.unlock();
    }
  }

  public void removeTag(T tag) {
    inventoryLock.lock();
    try {
      removeTagFromInventory(tag);
    } finally {
      inventoryLock.unlock();
    }
  }

  private void removeTagFromInventory(T tag) {
    if (null != tagById.remove(tag.getId())) {
      changeListener.tagDeparted(new TagDepartedEvent<>(identifier, tag));
    }
  }

  /**
   * Return a {@link List} with the current inventory
   * 
   * @return a {@link List} with the current inventory
   */
  public List<T> getInventory() {
    List<T> removedTags = checkInventory();
    List<T> inv = new ArrayList<>();
    Enumeration<T> eKeys = tagById.elements();
    while (eKeys.hasMoreElements()) {
      inv.add(eKeys.nextElement());
    }
    removedTags.sort(null);
    if (!removedTags.isEmpty()) {
      changeListener.inventoryChanged(new InventoryChangedEvent<>(identifier, inv, null, removedTags));
    }
    return inv;
  }

  /**
   * @return the current tag keep time
   */
  public long getKeepTime() {
    return keepTime;
  }

  /**
   * @param keepTime the tag keep time to set
   */
  public void setKeepTime(long keepTime) {
    this.keepTime = keepTime;
    if (this.keepTime < 0) {
      this.keepTime = IS_STICKY;
    }
  }

  /**
   * start automatically checking the inventory for lost tags
   */
  public void start() {
    if (this.keepTime == IS_STICKY) {
      return;
    }
    internalThread = new Thread(new Runnable() {
      @Override
      public void run() {
        checkingInventory();
      }
    });
    internalThread.setDaemon(true);
    internalThread.start();

  }

  /**
   * stop automatically checking tags
   */
  public void stop() {
    isRunning = false;
    while (isAlive()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * @return the isSticky
   */
  public boolean isSticky() {
    return keepTime == IS_STICKY;
  }

  /**
   * Remove a tag from this list
   * 
   * @param tagEid tag eid to remove
   */
  public void removeTag(String tagEid) {
    // if (isSticky || keepTime == 0) {
    // checkLock.lock();
    // try {
    // if (null != timestampByTagEid.remove(tagEid)) {
    T tag = tagById.get(tagEid);
    if (null != tag) {
      removeTagFromInventory(tag);
      changeListener.inventoryChanged(
          new InventoryChangedEvent<>(identifier, new ArrayList<>(tagById.values()), null, Arrays.asList(tag)));
    }
    // }
    // } finally {
    // checkLock.unlock();
    // }
    // }
  }

  /**
   * Remove all tags from the inventory
   */
  public void clear() {
    inventoryLock.lock();
    try {
      Enumeration<String> eKeys = tagById.keys();
      while (eKeys.hasMoreElements()) {
        removeTagFromInventory(tagById.get(eKeys.nextElement()));
      }
    } finally {
      inventoryLock.unlock();
    }
  }

  /**
   * @return true if the inventory checking thread is alive
   */
  public boolean isAlive() {
    return null != internalThread && internalThread.isAlive();
  }

  /**
   * add a listener
   * 
   * @param listener inventory listener
   * 
   */
  private void addListener(InventoryListener<T> listener) {
    removeListener();
    changeListener = listener;
    if (null == changeListener) {
      changeListener = new DummyInventoryListener<>();
      return;
    }
  }

  /**
   * remove the listener
   */
  private void removeListener() {
    changeListener = null;
  }

}
