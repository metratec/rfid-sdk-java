package com.metratec.lib.rfidreader.nfc;

/**
 * NTAG Access configuration
 */
public class NTagAccessConfig {
  private int startAddress;
  private boolean readProtected;
  private int maxAttempts;


  /**
   * Create a new instance
   */
  public NTagAccessConfig() {}


  /**
   * Create a new instance
   * 
   * @param startAddress Page address from which password authentication is required
   * @param readProtected Indicates if read is also protected
   * @param maxAttempts Number of authentication attempts
   */
  public NTagAccessConfig(int startAddress, boolean readProtected, int maxAttempts) {
    this.startAddress = startAddress;
    this.readProtected = readProtected;
    this.maxAttempts = maxAttempts;
  }


  /**
   * @return the Page address from which password authentication is required
   */
  public int getStartAddress() {
    return startAddress;
  }

  /**
   * @param startAddress the Page address from which password authentication is required
   */
  public void setStartAddress(int startAddress) {
    this.startAddress = startAddress;
  }

  /**
   * @return true if read is also protected
   */
  public boolean isReadProtected() {
    return readProtected;
  }

  /**
   * @param readProtected Indicates if read is also protected
   */
  public void setReadProtected(boolean readProtected) {
    this.readProtected = readProtected;
  }

  /**
   * @return Number of authentication attempts
   */
  public int getMaxAttempts() {
    return maxAttempts;
  }

  /**
   * @param maxAttempts Number of authentication attempts
   */
  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }


}
