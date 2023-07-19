/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

import java.io.Serializable;

/**
 * @author jannis becke
 *
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
