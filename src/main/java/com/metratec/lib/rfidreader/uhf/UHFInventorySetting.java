package com.metratec.lib.rfidreader.uhf;

/**
 * Uhf inventory settings
 */
public class UHFInventorySetting {
  private boolean onlyNewTag = false;
  private boolean withRssi = false;
  private boolean withTid = false;
  private boolean fastStart = false;
  private boolean phase = false;
  private String select = "ALL";
  private String target = "A";
  private Integer rssiThreshold = -100;

  /**
   * @param onlyNewTag if true, only new tags will be reported
   * @param withRssi if true, append the RSSI value to the response
   * @param withTid if true, append the TID to the response
   * @param fastStart if true, does an inventory without putting all tags into session state A at the start. This can
   *        speed up the start of the inventory, but it requires that all tags are in "reset state"
   */
  public UHFInventorySetting(boolean onlyNewTag, boolean withRssi, boolean withTid, boolean fastStart) {
    this.onlyNewTag = onlyNewTag;
    this.withRssi = withRssi;
    this.withTid = withTid;
    this.fastStart = fastStart;
  }

  /**
   * @param onlyNewTag if true, only new tags will be reported
   * @param withRssi if true, append the RSSI value to the response
   * @param withTid if true, append the TID to the response
   * @param fastStart if true, does an inventory without putting all tags into session state A at the start. This can
   *        speed up the start of the inventory, but it requires that all tags are in "reset state"
   * @param phase if true, append the phase information to the inventory
   * @param select the select argument 'ALL', 'SL' or 'NSL'
   * @param target the target argument 'A' or 'B'
   * @param rssiThreshold the rssi threshold to set
   */
  public UHFInventorySetting(boolean onlyNewTag, boolean withRssi, boolean withTid, boolean fastStart, boolean phase,
      String select, String target, Integer rssiThreshold) {
    this(onlyNewTag, withRssi, withTid, fastStart);
    this.phase = phase;
    this.select = select;
    this.target = target;
    this.rssiThreshold = rssiThreshold;
  }

  /**
   * @return the the current 'only new tag' state
   */
  public boolean onlyNewTag() {
    return onlyNewTag;
  }

  /**
   * If true, the reader will only return the new transponder to the inventory.
   * 
   * @param onlyNewTag the reader only returns new transponders if this is set to true.
   */
  public void setOnlyNewTag(boolean onlyNewTag) {
    this.onlyNewTag = onlyNewTag;
  }

  /**
   * @return if true the rssi information is added to the inventory
   */
  public boolean withRssi() {
    return withRssi;
  }

  /**
   * Used to add the rssi information to the inventory
   * 
   * @param withRssi if true, add the rssi information to the inventory
   */
  public void setWithRssi(boolean withRssi) {
    this.withRssi = withRssi;
  }

  /**
   * @return if the tid is added to the inventory
   */
  public boolean withTid() {
    return withTid;
  }

  /**
   * Used to add the tid information to the inventory
   * 
   * @param withTid if true, add the tid information to the inventory
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
   * If fast start is enable, the reader does an inventory without putting all tags into session state A at the start.
   * This can speed up the start of the inventory, but it requires that all tags are in "reset state".
   * 
   * @param fastStart set to true to enable the fast start
   */
  public void setFastStart(boolean fastStart) {
    this.fastStart = fastStart;
  }

  /**
   * @return the phase
   */
  public boolean isPhase() {
    return phase;
  }

  /**
   * Used to add the phase information to the inventory
   * 
   * @param phase set true to append the phase information to the inventory.
   */
  public void setPhase(boolean phase) {
    this.phase = phase;
  }

  /**
   * @return the current configured select value
   */
  public String getSelect() {
    return select;
  }

  /**
   * Used to set which tags should respond. 'ALL' for all tags, 'SL' for tags with asserted selected flag or 'NSL' for
   * not-selected tags.
   * 
   * @param select 'ALL' for all tags, 'SL' for tags with asserted selected flag or 'NSL' for not-selected tags.
   */
  public void setSelect(String select) {
    this.select = select;
  }

  /**
   * @return the current configured target
   */
  public String getTarget() {
    return target;
  }

  /**
   * Used to set which tags should respond.
   * 
   * @param target Tags with inventoried state 'A' or 'B'.
   */
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * @return the current configured rssi threshold
   */
  public Integer getRssiThreshold() {
    return rssiThreshold;
  }

  /**
   * @param rssiThreshold the rssi threshold for tags. Only tags with an RSSI greater than or equal to rssi_threshold
   *        are reported.
   */
  public void setRssiThreshold(Integer rssiThreshold) {
    this.rssiThreshold = rssiThreshold;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    UHFInventorySetting other = (UHFInventorySetting) obj;
    return this.onlyNewTag == other.onlyNewTag && this.withRssi == other.withRssi && this.withTid == other.withTid
        && this.fastStart == other.fastStart && this.phase == other.phase && this.select.equals(other.select)
        && this.target.equals(other.target) && this.rssiThreshold.equals(this.rssiThreshold);
  }
}
