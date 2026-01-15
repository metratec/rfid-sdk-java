/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import com.metratec.lib.tag.RfidTag;

/**
 * A dummy implementation of InventoryListener that provides empty implementations
 * for all inventory events. This class can be used as a base class when you only
 * need to handle specific inventory events rather than implementing all methods.
 * 
 * @param <T> the type of RFID tag handled by this listener
 * @author mn
 *
 */
public class DummyInventoryListener<T extends RfidTag> implements InventoryListener<T> {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.metratec.lib.inventory.InventoryListener#readPointTagArrive(com.metratec.lib.inventory.
   * TagArrived)
   */
  @Override
  public void tagArrive(TagArrivedEvent<T> tagArrived) {}

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.inventory.InventoryListener#readPointInventoryChanged(com.metratec.lib.
   * inventory.InventoryChanged)
   */
  @Override
  public void inventoryChanged(InventoryChangedEvent<T> readPointInventoryChanged) {}

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.metratec.lib.inventory.InventoryListener#readPointTagDeparted(com.metratec.lib.inventory.
   * TagDeparted)
   */
  @Override
  public void tagDeparted(TagDepartedEvent<T> tagDeparted) {}

}
