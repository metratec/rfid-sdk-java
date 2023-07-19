/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * @author jannis becke
 *
 */
public class RfidReaderConnectionState extends RfidEvent {

  /**
   * 
   */
  private static final long serialVersionUID = 4049777049768955382L;
  private boolean isConnected;
  private String message;

  /**
   * @param masterEID receiver extended id
   * @param isConnected connected state
   * @param message message
   */
  public RfidReaderConnectionState(String masterEID, Boolean isConnected, String message) {
    this(masterEID, isConnected, message, System.currentTimeMillis());
  }

  /**
   * @param masterEID receiver extended id
   * @param isConnected connected state
   * @param message message
   * @param timestamp timestamp
   */
  public RfidReaderConnectionState(String masterEID, Boolean isConnected, String message,
      Long timestamp) {
    super(masterEID, timestamp);
    this.isConnected = isConnected;
    this.message = message;
  }

  /**
   * @return the isConnected
   */
  public Boolean isConnected() {
    return isConnected;
  }

  /**
   * @return the exception
   */
  public String getMessage() {
    return message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new StringBuilder().append(isConnected + " " + message).toString();
  }
}
