/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * @author jannis becke
 *
 */
public interface RfidTagEventListener<T> {

  void tagFound(RfidTagFound<T> tag);

  void tagLost(RfidTagLost<T> tag);
}
