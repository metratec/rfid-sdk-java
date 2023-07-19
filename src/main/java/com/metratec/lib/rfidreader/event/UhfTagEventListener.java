/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.UhfTag;

/**
 * @author man
 *
 */
public interface UhfTagEventListener extends RfidTagEventListener<UhfTag> {

  @Override
  default void tagFound(RfidTagFound<UhfTag> event) {
    tagFound(new UhfTagFound(event));
  }

  @Override
  default void tagLost(RfidTagLost<UhfTag> event) {
    tagLost(new UhfTagLost(event));
  }
  
  void tagFound(UhfTagFound event);

  void tagLost(UhfTagLost event);

}
