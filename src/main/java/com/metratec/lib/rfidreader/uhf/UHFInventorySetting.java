package com.metratec.lib.rfidreader.uhf;

/**
 * Uhf inventory settings
 */
public class UHFInventorySetting {
  private boolean onlyNewTag;
  private boolean withRssi;
  private boolean withTid;
  private boolean fastStart;

  /**
   * @param onlyNewTag only new tags flag
   * @param withRssi with rssi flag
   * @param withTid with tid flag
   * @param fastStart the fast start flag
   */
  public UHFInventorySetting(boolean onlyNewTag, boolean withRssi, boolean withTid, boolean fastStart) {
    this.onlyNewTag = onlyNewTag;
    this.withRssi = withRssi;
    this.withTid = withTid;
    this.fastStart = fastStart;
  }

  /**
   * @return the onlyNewTag
   */
  public boolean onlyNewTag() {
    return onlyNewTag;
  }

  /**
   * @param onlyNewTag the onlyNewTag to set
   */
  public void setOnlyNewTag(boolean onlyNewTag) {
    this.onlyNewTag = onlyNewTag;
  }

  /**
   * @return the withRssi
   */
  public boolean withRssi() {
    return withRssi;
  }

  /**
   * @param withRssi the withRssi to set
   */
  public void setWithRssi(boolean withRssi) {
    this.withRssi = withRssi;
  }

  /**
   * @return the withTid
   */
  public boolean withTid() {
    return withTid;
  }

  /**
   * @param withTid the withTid to set
   */
  public void setWithTid(boolean withTid) {
    this.withTid = withTid;
  }

  /**
   * @return true if the fast start is enabled
   */
  public boolean isFastStart() {
    return fastStart;
  }

  /**
   * @param fastStart set to true to enable the fast start
   */
  public void setFastStart(boolean fastStart) {
    this.fastStart = fastStart;
  }

}
