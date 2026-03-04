/*******************************************************************************
 * Copyright (c) 2026 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.event;

/**
 * Configuration settings for RFID tag event handling.
 * This class defines various timing and counting parameters that control
 * how tag events are processed, including flush intervals, reset intervals,
 * and minimum/maximum seen count thresholds.
 * 
 * @author mn
 *
 */
public class RfidTagEventSetting {
  private int flushInterval = 200;
  private int resetInterval = 1000;
  private int minSeenCount = 0;
  private int maxSeenCount = 0;

  /**
   * Creates a new RFID tag event setting with default values.
   * Default values: flushInterval=200ms, resetInterval=1000ms, 
   * minSeenCount=0, maxSeenCount=0.
   */
  public RfidTagEventSetting() {
    super();
  }

  /**
   * Creates a new RFID tag event setting with specified values.
   * 
   * @param flushInterval the interval in milliseconds for flushing events
   * @param minSeenCount the minimum number of times a tag must be seen
   * @param maxSeenCount the maximum number of times a tag can be seen
   * @param resetInterval the interval in milliseconds for resetting counters
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
   * Gets the event flush interval.
   * 
   * @return the flush interval in milliseconds
   */
  public int getFlushInterval() {
    return flushInterval;
  }

  /**
   * Sets the event flush interval.
   * 
   * @param flushInterval the flush interval in milliseconds (must be > 0)
   */
  public void setFlushInterval(int flushInterval) {
    this.flushInterval = 0 < flushInterval ? flushInterval : 0;
  }

  /**
   * Gets the minimum seen count threshold.
   * 
   * @return the minimum number of times a tag must be seen
   */
  public int getMinSeenCount() {
    return minSeenCount;
  }

  /**
   * Sets the minimum seen count threshold.
   * 
   * @param minSeenCount the minimum seen count (must be > 0)
   */
  public void setMinSeenCount(int minSeenCount) {
    this.minSeenCount = 0 < minSeenCount ? minSeenCount : 0;
  }

  /**
   * Gets the maximum seen count threshold.
   * 
   * @return the maximum number of times a tag can be seen
   */
  public int getMaxSeenCount() {
    return maxSeenCount;
  }

  /**
   * Sets the maximum seen count threshold.
   * 
   * @param maxSeenCount the maximum seen count (must be > 0)
   */
  public void setMaxSeenCount(int maxSeenCount) {
    this.maxSeenCount = 0 < maxSeenCount ? maxSeenCount : 0;
  }

  /**
   * Gets the reset interval for counters.
   * 
   * @return the reset interval in milliseconds
   */
  public int getResetInterval() {
    return resetInterval;
  }

  /**
   * Sets the reset interval for counters.
   * 
   * @param resetInterval the reset interval in milliseconds (must be > 0)
   */
  public void setResetInterval(int resetInterval) {
    this.resetInterval = 0 < resetInterval ? resetInterval : 0;
  }


}
