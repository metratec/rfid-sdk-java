/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.tag;

/**
 * Represents a High Frequency (HF) RFID tag.
 * HF tags typically operate at 13.56 MHz and support ISO standards for
 * identification and data storage. They include Tag Identifier (TID) and
 * type information for different HF tag variants.
 * 
 * @author mn
 *
 */
public class HfTag extends RfidTag {
  /**
   * 
   */
  private static final long serialVersionUID = 3103531027364206829L;
  private String tid;
  private String type;
  /**
   * 
   */
  public HfTag() {
    super();
  }

  /**
   * @param tid tag id
   */
  public HfTag(String tid) {
    this(tid, System.currentTimeMillis());
  }

  /**
   * @param firstSeenTimestamp first seen timestamp
   */
  public HfTag(Long firstSeenTimestamp) {
    this(null, firstSeenTimestamp, null);
  }

  /**
   * @param tid tag id
   * @param firstSeenTimestamp first seen timestamp
   */
  public HfTag(String tid, Long firstSeenTimestamp) {
    this(tid, firstSeenTimestamp, null);
  }

  /**
   * @param tid tag id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna the current antenna
   */
  public HfTag(String tid, Long firstSeenTimestamp, Integer antenna) {
    this(tid, firstSeenTimestamp, antenna, "HfTag");
  }

  /**
   * @param tid tag id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna the current antenna
   * @param type transponder type
   */
  public HfTag(String tid, Long firstSeenTimestamp, Integer antenna, String type) {
    super(firstSeenTimestamp);
    setTid(tid);
    setAntenna(antenna);
    setType(type);
  }

  /**
   * @return the tid
   */
  public String getTid() {
    return tid;
  }

  /**
   * @param tid the tid to set
   */
  public void setTid(String tid) {
    super.setId(tid);
    this.tid = tid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.tag.RfidTag#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    setTid(id);
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append('{');
    if (null != getId()) {
      buf.append("\"tid\":\"").append(getTid()).append("\",");
    }
    if (null != getType()) {
      buf.append("\"type\":\"").append(getType()).append("\",");
    }
    if (null != getRssi()) {
      buf.append("\"rssi\":\"").append(getRssi()).append("\",");
    }
    if (null != getAntenna()) {
      buf.append("\"antenna\":\"").append(getAntenna()).append("\",");
    }
    if (null != getData()) {
      buf.append("\"data\":\"").append(getData()).append("\",");
    }
    buf.append("\"seenCount\":\"").append(getSeenCount()).append("\"");
    buf.append('}');
    return buf.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.tag.RfidTag#clone()
   */
  @Override
  public HfTag clone() {
    HfTag tag = new HfTag();
    tag.setAntenna(getAntenna());
    tag.setData(getData());
    tag.setFirstSeenTimestamp(getFirstSeenTimestamp());
    tag.setLastSeenTimestamp(getLastSeenTimestamp());
    tag.setRssi(getRssi());
    tag.setSeenCount(getSeenCount());
    tag.setTid(getTid());
    tag.setType(getType());
    return tag;
  }
}
