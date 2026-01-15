package com.metratec.lib.rfidreader.nfc;

/**
 * NTAG Mirror Configuration
 */
public class NTagMirrorConfig {
  /** NTAG Mirror Mode */
  public enum MirrorMode {
    /** Mirror mode off */
    OFF,
    /** UID mirror mode */
    UID,
    /** CNT mirror mode */
    CNT,
    /** Use both mirror modes */
    BOTH
  }

  /**
   * Create the mirror configuration
   */
  public NTagMirrorConfig() {}

  /**
   * Create the mirror configuration
   * 
   * @param mode The mirror mode.
   * @param page The start page where the configured data is mirrored to.
   * @param offset Byte Offset of the mirrored data in the Mirror Page.
   */
  public NTagMirrorConfig(MirrorMode mode, int page, int offset) {
    this.mode = mode;
    this.page = page;
    this.offset = offset;
  }

  private MirrorMode mode;
  private int page;
  private int offset;

  /**
   * @return The mirror mode
   */
  public MirrorMode getMode() {
    return mode;
  }

  /**
   * @param mode The mirror mode to set
   */
  public void setMode(MirrorMode mode) {
    this.mode = mode;
  }

  /**
   * @return The start page where the configured data is mirrored to.
   */
  public int getPage() {
    return page;
  }

  /**
   * @param page The start page to which the configured data is mirrored
   */
  public void setPage(int page) {
    this.page = page;
  }

  /**
   * @return Byte Offset of the mirrored data in the Mirror Page.
   */
  public int getOffset() {
    return offset;
  }

  /**
   * @param offset Byte Offset of the mirrored data in the mirror page to be set
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }


}
