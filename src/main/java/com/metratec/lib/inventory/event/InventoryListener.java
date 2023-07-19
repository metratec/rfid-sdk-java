/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import com.metratec.lib.tag.RfidTag;

/**
 * The listener interface for receiving tag events
 * 
 * @author man
 *
 */
public interface InventoryListener<T extends RfidTag> {

  /**
   * Called if a new tag is found
   * 
   * @param tagArrived {@link TagArrivedEvent}
   */
  void tagArrive(TagArrivedEvent<T> tagArrived);

  /**
   * Called if the inventory has changed
   * 
   * @param readPointInventoryChanged {@link InventoryChangedEvent}
   */
  void inventoryChanged(InventoryChangedEvent<T> readPointInventoryChanged);

  /**
   * Called if a tag is lost
   * 
   * @param tagDeparted {@link TagDepartedEvent}
   */
  void tagDeparted(TagDepartedEvent<T> tagDeparted);

}
