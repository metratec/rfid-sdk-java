/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * Generic event representing the loss of an RFID tag.
 * This is the base class for all tag lost events and serves as a foundation
 * for technology-specific implementations (UHF, HF, Mifare, etc.).
 * 
 * @param <T> the type of RFID tag associated with this event
 * @author mn
 *
 */
public class RfidTagLost<T> extends RfidTagEvent<T> {

  private static final long serialVersionUID = -8962542078797483491L;

  /**
   * Creates a new RFID tag lost event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the RFID tag that was lost
   * @param timestamp the timestamp when the tag was lost
   */
  public RfidTagLost(String identifier, T tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

}
