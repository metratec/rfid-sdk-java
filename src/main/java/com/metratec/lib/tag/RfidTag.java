/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.tag;

import java.io.Serializable;

/**
 * @author mn
 *
 */
public abstract class RfidTag implements Serializable, Cloneable, Comparable<RfidTag> {
  /**
   * 
   */
  private static final long serialVersionUID = 5813958776605078266L;
  private String id;
  private Long firstSeenTimestamp;
  private Long lastSeenTimestamp;
  private Integer seenCount;
  private Integer antenna;
  private Integer rssi;
  private String data;
  private boolean hasError = false;
  private String message = null;

  /**
   * 
   */
  public RfidTag() {
    this(null);
  }

  /**
   * @param firstSeenTimestamp first seen timestamp
   */
  public RfidTag(Long firstSeenTimestamp) {
    setFirstSeenTimestamp(firstSeenTimestamp);
    setLastSeenTimestamp(firstSeenTimestamp);
    setSeenCount(1);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the firstSeenTimestamp
   */
  public Long getFirstSeenTimestamp() {
    return firstSeenTimestamp;
  }

  /**
   * @param firstSeenTimestamp the firstSeenTimestamp to set
   */
  public void setFirstSeenTimestamp(Long firstSeenTimestamp) {
    this.firstSeenTimestamp = firstSeenTimestamp;
  }

  /**
   * @return the lastSeenTimestamp
   */
  public Long getLastSeenTimestamp() {
    return lastSeenTimestamp;
  }

  /**
   * @param lastSeenTimestamp the lastSeenTimestamp to set
   */
  public void setLastSeenTimestamp(Long lastSeenTimestamp) {
    this.lastSeenTimestamp = lastSeenTimestamp;
  }

  /**
   * @return the seenCount
   */
  public Integer getSeenCount() {
    return seenCount;
  }

  /**
   * @param seenCount the seenCount to set
   */
  public void setSeenCount(Integer seenCount) {
    this.seenCount = seenCount;
  }

  /**
   * @return the antenna
   */
  public Integer getAntenna() {
    return antenna;
  }

  /**
   * @param antenna the antenna to set
   */
  public void setAntenna(Integer antenna) {
    this.antenna = antenna;
  }

  /**
   * @return the rssi
   */
  public Integer getRssi() {
    return rssi;
  }

  /**
   * @param rssi the rssi to set
   */
  public void setRssi(Integer rssi) {
    this.rssi = rssi;
  }

  /**
   * @return the data
   */
  public String getData() {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(String data) {
    this.data = data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!RfidTag.class.isInstance(obj)) {
      return false;
    }
    try {
      return getId().equals(((RfidTag) obj).getId());
    } catch (NullPointerException e) {
      return super.equals(obj);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(RfidTag o) {
    try {
      return getId().compareTo(o.getId());
    } catch (NullPointerException e) {
      return -1;
    }
  }

  /**
   * update the tag with the new scanned one
   * 
   * @param tag the new scanned tag
   */
  public void updateTag(RfidTag tag) {
    setLastSeenTimestamp(tag.getLastSeenTimestamp());
    setRssi(tag.getRssi());
    setAntenna(tag.getAntenna());
    if (null != tag.getData()) {
      setData(tag.getData());
    }
    setSeenCount(getSeenCount() + tag.getSeenCount());
  }

  /**
   * @return the hasError
   */
  public boolean hasError() {
    return hasError;
  }

  /**
   * @param hasError the hasError to set
   */
  public void setHasError(boolean hasError) {
    this.hasError = hasError;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public abstract RfidTag clone();
}
