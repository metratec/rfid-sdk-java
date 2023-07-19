/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.MfTag;

/**
 * @author man
 *
 */
public interface MfTagEventListener extends RfidTagEventListener<MfTag> {
  
  @Override
  default void tagFound(RfidTagFound<MfTag> event) {
    tagFound(new MfTagFound(event));
  }

  @Override
  default void tagLost(RfidTagLost<MfTag> event) {
    tagLost(new MfTagLost(event));
  }

  void tagFound(MfTagFound event);

  void tagLost(MfTagLost event);

}
