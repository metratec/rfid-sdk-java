/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.tag;

/**
 * @author mn
 *
 */
public class MfTag extends RfidTag {
  /**
   * 
   */
  private static final long serialVersionUID = 3922278161444143898L;
  private String tid;

  /**
   * 
   */
  public MfTag() {
    super();
  }

  /**
   * @param firstSeenTimestamp first seen timestamp
   */
  public MfTag(Long firstSeenTimestamp) {
    this(null, firstSeenTimestamp, null);
  }

  /**
   * @param tid tag id
   * @param firstSeenTimestamp first seen timestamp
   */
  public MfTag(String tid, Long firstSeenTimestamp) {
    this(tid, firstSeenTimestamp, null);
  }

  /**
   * @param tid tag id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna the current antenna
   */
  public MfTag(String tid, Long firstSeenTimestamp, Integer antenna) {
    super(firstSeenTimestamp);
    setTid(tid);
    setAntenna(antenna);
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
    if (null != getRssi()) {
      buf.append("\"rssi\":\"").append(getRssi()).append("\",");
    }
    if (null != getRssi()) {
      buf.append("\"antenna\":\"").append(getAntenna()).append("\",");
    }
    buf.setLength(buf.length() - 1);
    buf.append('}');
    return buf.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.tag.RfidTag#clone()
   */
  @Override
  public MfTag clone() {
    MfTag tag = new MfTag();
    tag.setAntenna(getAntenna());
    tag.setData(getData());
    tag.setFirstSeenTimestamp(getFirstSeenTimestamp());
    tag.setLastSeenTimestamp(getLastSeenTimestamp());
    tag.setRssi(getRssi());
    tag.setSeenCount(getSeenCount());
    tag.setTid(getTid());
    return tag;
  }
}
