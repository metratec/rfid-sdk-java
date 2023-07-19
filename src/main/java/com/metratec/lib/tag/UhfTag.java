/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.tag;

/**
 * @author mn
 *
 */
public class UhfTag extends RfidTag {
  /**
   * 
   */
  private static final long serialVersionUID = -4168348329862392918L;
  private String epc;
  private String tid;

  private Integer dataStartAddress;

  /**
   * 
   */
  public UhfTag() {
    super();
  }

  /**
   * @param epc transponder epc
   */
  public UhfTag(String epc) {
    this(epc, System.currentTimeMillis());
  }

  /**
   * @param firstSeenTimestamp first seen timestamp
   */
  public UhfTag(Long firstSeenTimestamp) {
    this("", firstSeenTimestamp);
  }

  /**
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna the current antenna
   */
  public UhfTag(Long firstSeenTimestamp, Integer antenna) {
    this("", firstSeenTimestamp, antenna);
  }

  /**
   * @param epc epc
   * @param firstSeenTimestamp first seen timestamp
   */
  public UhfTag(String epc, Long firstSeenTimestamp) {
    this(epc, null != firstSeenTimestamp ? firstSeenTimestamp : System.currentTimeMillis(), null);
  }

  /**
   * @param epc epc
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna the current antenna
   */
  public UhfTag(String epc, Long firstSeenTimestamp, Integer antenna) {
    super(firstSeenTimestamp);
    setEpc(epc);
    setAntenna(antenna);
  }

  /**
   * @return the epc
   */
  public String getEpc() {
    return epc;
  }

  /**
   * @param epc the epc to set
   */
  public void setEpc(String epc) {
    super.setId(epc);
    this.epc = epc;
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
    this.tid = tid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.tag.RfidTag#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    setEpc(id);
  }

  /**
   * @return the dataStartAddress
   */
  public Integer getDataStartAddress() {
    return dataStartAddress;
  }

  /**
   * @param dataStartAddress the dataStartAddress to set
   */
  public void setDataStartAddress(Integer dataStartAddress) {
    this.dataStartAddress = dataStartAddress;
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
    if (null != getEpc()) {
      buf.append("\"epc\":\"").append(getEpc()).append("\",");
    }
    if (null != getTid()) {
      buf.append("\"tid\":\"").append(getTid()).append("\",");
    }
    if (null != getRssi()) {
      buf.append("\"rssi\":\"").append(getRssi()).append("\",");
    }
    if (null != getRssi()) {
      buf.append("\"antenna\":\"").append(getAntenna()).append("\",");
    }
    if (null != getData()) {
      buf.append("\"data\":\"").append(getData()).append("\",");
      buf.append("\"dataStartAddress\":\"").append(getDataStartAddress()).append("\",");
    }
    buf.append("\"seenCount\":\"").append(getSeenCount()).append("\"");
    buf.append('}');
    return buf.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public UhfTag clone() {
    UhfTag tag = new UhfTag();
    tag.setAntenna(getAntenna());
    tag.setData(getData());
    tag.setDataStartAddress(getDataStartAddress());
    tag.setEpc(getEpc());
    tag.setFirstSeenTimestamp(getFirstSeenTimestamp());
    tag.setLastSeenTimestamp(getLastSeenTimestamp());
    tag.setRssi(getRssi());
    tag.setSeenCount(getSeenCount());
    tag.setTid(getTid());
    return tag;
  }
}
