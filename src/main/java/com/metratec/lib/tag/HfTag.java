/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.tag;

/**
 * @author mn
 *
 */
public class HfTag extends RfidTag {
  /**
   * 
   */
  private static final long serialVersionUID = 3103531027364206829L;
  private String tid;

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
    if (null != getAntenna()) {
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
  public HfTag clone() {
    HfTag tag = new HfTag();
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
