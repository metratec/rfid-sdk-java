/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * Generic event representing the detection of an RFID tag.
 * This is the base class for all tag found events and serves as a foundation
 * for technology-specific implementations (UHF, HF, Mifare, etc.).
 * 
 * @param <T> the type of RFID tag associated with this event
 * @author mn
 *
 */
public class RfidTagFound<T> extends RfidTagEvent<T> {

  private static final long serialVersionUID = 5630195390982611902L;

  /**
   * Creates a new RFID tag found event with a specific timestamp.
   * 
   * @param identifier the reader identifier
   * @param tag the RFID tag that was found
   * @param timestamp the timestamp when the tag was found
   */
  public RfidTagFound(String identifier, T tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

}
