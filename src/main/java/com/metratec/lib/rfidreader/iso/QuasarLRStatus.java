/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import java.util.HashMap;

/**
 * The state of the quasar lr reader
 * 
 * @author mn
 *
 */
public class QuasarLRStatus extends HashMap<String, Object> implements Comparable<QuasarLRStatus> {
  /**
   * 
   */
  private static final long serialVersionUID = 2040064535890711971L;
  /**
   * the reader name key
   */
  protected static final String READER_NAME = "ReaderName";
  private static final String READER_MODE = "ReaderMode";
  private static final String RF_STATUS = "RFStatus";
  private static final String RF_POWER = "RFPower";
  private static final String FORWARD_POWER = "ForwardPower";
  private static final String REFLECTED_POWER = "ReflectedPower";
  private static final String SWR = "SWR";
  private static final String TEMPERATURE = "Temperature";
  /**
   * the timestamp key
   */
  protected static final String TIMESTAMP = "Timestamp";
  /**
   * the antenna key
   */
  protected static final String ANTENNA = "Anntena";

  /**
   * 
   */
  public QuasarLRStatus() {
    super();
  }

  /**
   * @return the mode
   */
  public String getReaderName() {
    try {
      return get(READER_NAME).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the mode
   */
  public String getReaderMode() {
    try {
      return get(READER_MODE).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the rfEnable
   */
  public String getRFStatus() {
    try {
      return get(RF_STATUS).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the rfPower in mW
   */
  public String getRFPower() {
    try {
      return get(RF_POWER).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the forwardPower in mW
   */
  public String getForwardPower() {
    try {
      return get(FORWARD_POWER).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the reflectedPower in mW
   */
  public String getReflectedPower() {
    try {
      return get(REFLECTED_POWER).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the swr
   */
  public String getSWR() {
    try {
      return get(SWR).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }


  /**
   * @return the temp in °C
   */
  public String getTemperature() {
    try {
      return get(TEMPERATURE).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * @return the reflectedPower in mW
   */
  public Long getTimestamp() {
    try {
      return Long.parseLong(get(TIMESTAMP).toString());
    } catch (NullPointerException | NumberFormatException e) {
      return null;
    }
  }

  /**
   * @return the temp in °C
   */
  public String getAntenna() {
    try {
      return get(ANTENNA).toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  @Override
  public int compareTo(QuasarLRStatus o) {
    Long timestamp = this.getTimestamp();
    Long timestampOther = o.getTimestamp();
    if (null == timestamp && null == timestampOther) {
      return 0;
    }
    if (null == timestamp) {
      return 1;
    }
    if (null == timestampOther) {
      return -1;
    }
    return timestamp.compareTo(timestampOther);
  }
}
