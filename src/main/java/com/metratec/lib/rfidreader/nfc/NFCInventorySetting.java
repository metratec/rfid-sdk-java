package com.metratec.lib.rfidreader.nfc;

/**
 * Inventory settings for the NFC reader
 */
public class NFCInventorySetting {
  private boolean withTagDetails;
  private boolean onlyNewTags;
  private boolean singleSlot;

  /**
   * Create a new instance
   */
  public NFCInventorySetting() {}

  /**
   * Create a new instance
   * 
   * @param withTagDetails
   * @param onlyNewTags
   * @param singleSlot
   */
  public NFCInventorySetting(boolean withTagDetails, boolean onlyNewTags, boolean singleSlot) {
    this.withTagDetails = withTagDetails;
    this.onlyNewTags = onlyNewTags;
    this.singleSlot = singleSlot;
  }

  /**
   * @return whether the additional details are activated or deactivated
   */
  public boolean isWithTagDetails() {
    return withTagDetails;
  }

  /**
   * To add additional information about the tag in the inventory response. Only if the reader is not in AUTO mode
   * 
   * @param withTagDetails activate or deactivate
   */
  public void setWithTagDetails(boolean withTagDetails) {
    this.withTagDetails = withTagDetails;
  }

  /**
   * @return whether the only new tags is activated or deactivated
   */
  public boolean isOnlyNewTags() {
    return onlyNewTags;
  }

  /**
   * The only new tags filter only has an effect in ISO15 mode. If enabled, a Stay Quiet is sent to each tag in the
   * field after an successful inventory. This has the effect that any tag that remains in the field is only found once
   * in an inventory
   * 
   * @param onlyNewTags activate or deactivate
   */
  public void setOnlyNewTags(boolean onlyNewTags) {
    this.onlyNewTags = onlyNewTags;
  }

  /**
   * @return whether the single slot is activated or deactivated
   */
  public boolean isSingleSlot() {
    return singleSlot;
  }

  /**
   * Has only an effect in ISO15 mode. If it is set to 1 ISO15 inventories will be run in single slotted mode, resulting
   * in faster inventories. There will be no anti-collision loop performed so an inventory with multiple tags in the
   * field will result in failure.
   * 
   * @param useSingleSlot activate or deactivate
   */
  public void setSingleSlot(boolean useSingleSlot) {
    this.singleSlot = useSingleSlot;
  }

  @Override
  protected NFCInventorySetting clone() {
    return new NFCInventorySetting(isWithTagDetails(), isOnlyNewTags(), isSingleSlot());
  }
}
