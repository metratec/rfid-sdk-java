/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import java.io.Serializable;

/**
 * Base class for RFID reader events.
 * 
 * This class provides the foundation for all RFID-related events including
 * reader connection events, tag detection events, and inventory events.
 * 
 * @author jannis becke
 */
public class RfidEvent implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 185030807814971348L;
  private String identifier;
  private long timestamp;

  /**
   * @param identifier reader identifier
   * @param timestamp event time
   */
  public RfidEvent(String identifier, Long timestamp) {
    super();
    this.identifier = identifier;
    this.timestamp = null != timestamp ? timestamp : System.currentTimeMillis();
  }

  /**
   * @return the time stamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @return the masterEID
   */
  public String getIdentifier() {
    return identifier;
  }
}
