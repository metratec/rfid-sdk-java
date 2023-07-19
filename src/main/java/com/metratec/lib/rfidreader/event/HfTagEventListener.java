/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import com.metratec.lib.tag.HfTag;

/**
 * @author man
 *
 */
public interface HfTagEventListener extends RfidTagEventListener<HfTag>  {

  @Override
  default void tagFound(RfidTagFound<HfTag> event) {
    tagFound(new HfTagFound(event));
  }

  @Override
  default void tagLost(RfidTagLost<HfTag> event) {
    tagLost(new HfTagLost(event));
  }

  void tagFound(HfTagFound tag);

  void tagLost(HfTagLost tag);

}
