package com.metratec.lib.rfidreader.nfc;

/**
 * NTAG counter config
 */
public class NTagCounterConfig {
  private boolean enable;
  private boolean withPasswortProtection;


  /**
   * 
   */
  public NTagCounterConfig() {}


  /**
   * @param enable Enable or disable counter
   * @param withPasswortProtection Enable or disable password protected
   */
  public NTagCounterConfig(boolean enable, boolean withPasswortProtection) {
    this.enable = enable;
    this.withPasswortProtection = withPasswortProtection;
  }


  /**
   * @return if the counter configuration is enabled oder disabled
   */
  public boolean isEnable() {
    return enable;
  }

  /**
   * @param enable enable or disable counter
   */
  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  /**
   * @return the if the counter is with password protection
   */
  public boolean isWithPasswortProtection() {
    return withPasswortProtection;
  }

  /**
   * @param withPasswortProtection enable or disable password protected
   */
  public void setWithPasswortProtection(boolean withPasswortProtection) {
    this.withPasswortProtection = withPasswortProtection;
  }

}
