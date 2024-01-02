package com.metratec.lib.rfidreader.uhf;

/**
 * Custom impinj settings
 */
public class CustomImpinjSettings {
  private boolean fastId;
  private boolean tagFocus;

  /**
   * Create the custom impinj settings with the given parameters
   * 
   * @param fastId True to allows to read the TagID together with the EPC and can speed up getting TID data
   * @param tagFocus True to uses a proprietary tag feature where each tag only answers once until it is repowered
   */
  public CustomImpinjSettings(boolean fastId, boolean tagFocus) {
    setFastId(fastId);
    setTagFocus(tagFocus);
  }

  /**
   * @return true if the fast id feature is set
   */
  public boolean isFastId() {
    return fastId;
  }

  /**
   * Allows to read the TagID together with the EPC and can speed up getting TID data.
   * 
   * @param fastId set to true to enable the feature
   */
  protected void setFastId(boolean fastId) {
    this.fastId = fastId;
  }

  /**
   * @return true if the tag focus is set
   */
  public boolean isTagFocus() {
    return tagFocus;
  }

  /**
   * Uses a proprietary tag feature where each tag only answers once until it is repowered. This allows to scan a high
   * number of tags because each tag only answers once and makes anti-collision easier for the following tags.
   * 
   * @param tagFocus set to true to enable the feature
   */
  protected void setTagFocus(boolean tagFocus) {
    this.tagFocus = tagFocus;
  }


}
