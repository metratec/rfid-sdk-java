/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import java.io.Serializable;

/**
 * The noise measurement of the active antenna
 * 
 * @author mn
 *
 */
public class QuasarLRNoiseMeasure implements Serializable, Comparable<QuasarLRNoiseMeasure> {
  /**
   * 
   */
  private static final long serialVersionUID = 3218322447678557062L;
  private String name;
  private long timestamp;
  private Integer antenna;
  private int minimum;
  private int maximum;
  private int average;

  /**
   * 
   */
  public QuasarLRNoiseMeasure() {
    super();
  }

  /**
   * @param name reader name
   * @param timestamp the timestamp of measurement
   * @param antenna the current antenna port
   * @param minimum the minimum value
   * @param average the average value
   * @param maximum the maximum value
   */
  protected QuasarLRNoiseMeasure(String name, long timestamp, Integer antenna, int minimum,
      int average, int maximum) {
    super();
    this.name = name;
    this.timestamp = timestamp;
    this.antenna = antenna;
    this.minimum = minimum;
    this.average = average;
    this.maximum = maximum;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @return the activated antenna port or null
   */
  public Integer getAntenna() {
    return antenna;
  }

  /**
   * @return the minimum
   */
  public int getMinimum() {
    return minimum;
  }

  /**
   * @return the average
   */
  public int getAverage() {
    return average;
  }

  /**
   * @return the maximum
   */
  public int getMaximum() {
    return maximum;
  }

  /**
   * return this object as json string
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('{');
    builder.append("\"name\":\"").append(getName()).append('"');
    builder.append(",\"timestamp\":").append(getTimestamp());
    if (null != getAntenna()) {
      builder.append(",\"antenna\":").append(getAntenna());
    }
    builder.append(",\"minimum\":").append(getMinimum());
    builder.append(",\"maximum\":").append(getMaximum());
    builder.append(",\"average\":").append(getAverage());
    builder.append('}');
    return builder.toString();
  }

  @Override
  public int compareTo(QuasarLRNoiseMeasure o) {
    return Long.compare(this.getTimestamp(), o.getTimestamp());
  }
}
