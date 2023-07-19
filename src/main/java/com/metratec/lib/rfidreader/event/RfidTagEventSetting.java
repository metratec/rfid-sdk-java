/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * @author mn
 *
 */
public class RfidTagEventSetting {
  private int flushInterval = 200;
  private int resetInterval = 1000;
  private int minSeenCount = 0;
  private int maxSeenCount = 0;

  /**
   * 
   */
  public RfidTagEventSetting() {
    super();
  }

  /**
   * @param flushInterval event flush interval
   * @param minSeenCount min tag seen count
   * @param maxSeenCount max tag seen count
   * @param resetInterval reset interval
   */
  public RfidTagEventSetting(int flushInterval, int minSeenCount, int maxSeenCount,
      int resetInterval) {
    super();
    setFlushInterval(flushInterval);
    setMinSeenCount(minSeenCount);
    setMaxSeenCount(maxSeenCount);
    setResetInterval(resetInterval);
  }

  /**
   * @return the flushInterval
   */
  public int getFlushInterval() {
    return flushInterval;
  }

  /**
   * @param flushInterval the flushInterval to set
   */
  public void setFlushInterval(int flushInterval) {
    this.flushInterval = 0 < flushInterval ? flushInterval : 0;
  }

  /**
   * @return the minSeenCount
   */
  public int getMinSeenCount() {
    return minSeenCount;
  }

  /**
   * @param minSeenCount the minSeenCount to set
   */
  public void setMinSeenCount(int minSeenCount) {
    this.minSeenCount = 0 < minSeenCount ? minSeenCount : 0;
  }

  /**
   * @return the maxSeenCount
   */
  public int getMaxSeenCount() {
    return maxSeenCount;
  }

  /**
   * @param maxSeenCount the maxSeenCount to set
   */
  public void setMaxSeenCount(int maxSeenCount) {
    this.maxSeenCount = 0 < maxSeenCount ? maxSeenCount : 0;
  }

  /**
   * @return the resetInterval
   */
  public int getResetInterval() {
    return resetInterval;
  }

  /**
   * @param resetInterval the resetInterval to set
   */
  public void setResetInterval(int resetInterval) {
    this.resetInterval = 0 < resetInterval ? resetInterval : 0;
  }


}
