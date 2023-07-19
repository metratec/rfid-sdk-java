/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * @author jannis becke
 *
 */
public class RfidReaderInputChange extends RfidEvent {

  /**
   * 
   */
  private static final long serialVersionUID = 8714548039820291872L;
  private Integer pin;
  private Boolean state;

  /**
   * @param identifier reader identifier
   * @param timestamp timestamp
   * @param pin pin
   * @param state state
   */
  public RfidReaderInputChange(String identifier, Long timestamp, Integer pin, Boolean state) {
    super(identifier, timestamp);
    this.pin = pin;
    this.state = state;
  }

  /**
   * @return the pin
   */
  public Integer getPin() {
    return pin;
  }

  /**
   * @param pin the pin to set
   */
  public void setPin(Integer pin) {
    this.pin = pin;
  }

  /**
   * @return the state
   */
  public Boolean getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(Boolean state) {
    this.state = state;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new StringBuilder().append("pin " + pin + " - " + state).toString();
  }

}
