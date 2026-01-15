package com.metratec.lib.tag;

/**
 * The ISO14A Transponder class
 */
public class ISO14ATag extends HfTag{
  
  private static final long serialVersionUID = 7996378791482953213L;

  /**
   * Create an new ISO14ATag instance
   * 
   * @param tid transponder id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   * @param type the transponder type
   */
  public ISO14ATag(String tid, Long firstSeenTimestamp, Integer antenna, String type) {
    super(tid, firstSeenTimestamp, antenna, type);
  }

  /**
   * Create an new ISO14ATag instance
   * 
   * @param tid transponder id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   */
  public ISO14ATag(String tid, Long firstSeenTimestamp, Integer antenna) {
    super(tid, firstSeenTimestamp, antenna, "ISO14A");
  }

  /**
   * Create an new ISO14ATag instance
   * 
   * @param tid transponder id
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   * @param sak the sak value
   * @param atqa the atqa value
   */
  public ISO14ATag(String tid, Long firstSeenTimestamp, Integer antenna, Integer sak, Integer atqa) {
    super(tid, firstSeenTimestamp, antenna, "ISO14A");
    this.sak = sak;
    this.atqa = atqa;
  }

  /**
   * Create an new ISO14ATag instance
   * 
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   * @param type the transponder type
   */
  public ISO14ATag(Long firstSeenTimestamp, Integer antenna, String type) {
    super(null, firstSeenTimestamp, antenna, type);
  }

  /**
   * Create an new ISO14ATag instance
   * 
   * @param firstSeenTimestamp first seen timestamp
   * @param antenna antenna number
   */
  public ISO14ATag(Long firstSeenTimestamp, Integer antenna) {
    this(null, firstSeenTimestamp, antenna);
  }

  private Integer sak;

  private Integer atqa;

  /**
   * @return the sak value
   */
  public Integer getSak() {
    return sak;
  }

  /**
   * @param sak the sak to set
   */
  public void setSak(Integer sak) {
    this.sak = sak;
  }

  /**
   * @return the atqa value
   */
  public Integer getAtqa() {
    return atqa;
  }

  /**
   * @param atqa the atqa to set
   */
  public void setAtqa(Integer atqa) {
    this.atqa = atqa;
  }

  
}
