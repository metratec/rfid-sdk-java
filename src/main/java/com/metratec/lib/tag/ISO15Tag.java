package com.metratec.lib.tag;

/**
 * The ISO15 Transponder class
 */
public class ISO15Tag extends HfTag {

  private static final long serialVersionUID = -8421890818503292763L;
  private int dsfid;

  /**
   * Create an new ISO15Tag instance
   * 
   * @param tid transponder id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   * @param type the transponder type
   */
  public ISO15Tag(String tid, Long firstSeenTimestamp, Integer antenna, String type) {
    super(tid, firstSeenTimestamp, antenna, type);
  }

  /**
   * Create an new ISO15Tag instance
   * 
   * @param tid transponder id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   */
  public ISO15Tag(String tid, Long firstSeenTimestamp, Integer antenna) {
    super(tid, firstSeenTimestamp, antenna, "ISO15");
  }

  /**
   * Create an new ISO15Tag instance
   * 
   * @param tid transponder id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   * @param dsfid the dsfid
   */
  public ISO15Tag(String tid, Long firstSeenTimestamp, Integer antenna, Integer dsfid) {
    super(tid, firstSeenTimestamp, antenna, "ISO15");
    this.dsfid = dsfid;
  }

  /**
   * Create an new ISO15Tag instance
   * 
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   * @param type the transponder type
   */
  public ISO15Tag(Long firstSeenTimestamp, Integer antenna, String type) {
    super(null, firstSeenTimestamp, antenna, type);
  }

  /**
   * Create an new ISO15Tag instance
   * 
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   */
  public ISO15Tag(Long firstSeenTimestamp, Integer antenna) {
    this(null, firstSeenTimestamp, antenna);
  }

  /**
   * @return the dsfid
   */
  public int getDsfid() {
    return dsfid;
  }

  /**
   * @param dsfid the dsfid to set
   */
  public void setDsfid(int dsfid) {
    this.dsfid = dsfid;
  }

}
