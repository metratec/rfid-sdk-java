package com.metratec.lib.rfidreader.uhf;

public class UHFTagReportSetting {
  private String id;

  /**
   * @param idToUse tag id to use 'EPC' or 'TID'
   */
  public UHFTagReportSetting(String idToUse) {
    this.id = idToUse;
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

  public boolean withEpc() {
    return id.equals("EPC");
  }

  public boolean withTid() {
    return id.equals("TID");
  }
}
