/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.inventory.event;

import com.metratec.lib.tag.RfidTag;

/**
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
