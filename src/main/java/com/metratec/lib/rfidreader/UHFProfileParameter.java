/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

/**
 * Class which contains the metraTec uhf profile parameter
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public class UHFProfileParameter {
  /**
   * UHF Reader type enum
   * 
   * @author Matthias Neumann (neumann@metratec.com)
   */
  public enum UHFReaderType {
    /** DeskID */
    DESKID,
    /** Dwarf G2 */
    DWARFG2,
    /** Pulsar */
    PULSAR
  }

  private UHFReaderType readerType;
  private int digitizerHysteresis;
  private int highPassFrequency;
  private int linkFrequency;
  private int lowPassFrequency;
  private int encoding;
  private int noResponseWaitTime;
  private int transmitterPower;
  private int rxWaitTime;
  private int receiverGain;
  private int tari;
  private int trcal;
  private boolean isDifferentialMixerGain;
  private boolean isDeviceRatio8;
  private boolean isMixerInputAttenuation;
  private boolean isPRASK;
  private boolean isSettlingSpeedUp;

  /**
   * create a new UHF reader configuration with default values
   * 
   * @param readerType see {@link UHFReaderType}
   */
  public UHFProfileParameter(UHFReaderType readerType) {
    this.readerType = readerType;

  }

  /**
   * @param readerType the {@link UHFReaderType} to set
   */
  public void setReaderType(UHFReaderType readerType) {
    this.readerType = readerType;
  }

  /**
   * @return the reader type (see {@link UHFReaderType})
   */
  public UHFReaderType getReaderType() {
    return readerType;
  }

  /**
   * @return the hysteresis value of the digitizer in 3dB steps [0..7]
   */
  public int getDigitizerHysteresis() {
    return digitizerHysteresis;
  }

  /**
   * sets the hysteresis value of the digitizer
   * 
   * @param digitizerHysteresis value in 3dB steps [0,7]
   */
  public void setDigitizerHysteresis(int digitizerHysteresis) {
    this.digitizerHysteresis = digitizerHysteresis;
  }

  /**
   * @return the true if the 10dB Gain
   */
  public boolean isDifferentialMixerGain() {
    return isDifferentialMixerGain;
  }

  /**
   * @param isDifferentialMixerGain 10dB Gain if true
   */
  public void setDifferentialMixerGain(boolean isDifferentialMixerGain) {
    this.isDifferentialMixerGain = isDifferentialMixerGain;
  }

  /**
   * @return the true if device ratio is 8, else the device ratio is 64/3
   */
  public boolean isDeviceRatio8() {
    return isDeviceRatio8;
  }

  /**
   * @param isDeviceRatio8 true to set the device ratio to 8, else the device ratio to 64/3
   */
  public void setDeviceRatio8(boolean isDeviceRatio8) {
    this.isDeviceRatio8 = isDeviceRatio8;
  }

  /**
   * @return the high pass frequency
   */
  public int getHighPassFrequency() {
    return highPassFrequency;
  }

  /**
   * Sets the high pass frequency. Ideal value depends on Link Frequency. For 320 kHz values from 0 to 4 work best for
   * most tags
   * 
   * @param highPassFrequency the value to set [0,7]
   */
  public void setHighPassFrequency(int highPassFrequency) {
    this.highPassFrequency = highPassFrequency;
  }

  /**
   * @return the linkFrequency 0: 40kHz, 6: 160kHz, 9: 256kHz, 12: 320kHz, 15: 640kHz
   */
  public int getLinkFrequency() {
    return linkFrequency;
  }

  /**
   * Sets the link frequency defined in the EPC Gen2 standard. The following values can be set: 0: 40kHz, 6: 160kHz, 9:
   * 256kHz, 12: 320kHz, 15: 640kHz
   * 
   * @param linkFrequency the linkFrequency to set {0, 6, 9, 12, 15}
   */
  public void setLinkFrequency(int linkFrequency) {
    this.linkFrequency = linkFrequency;
  }

  /**
   * @return the low pass frequency, 0 for 640kHz, 4 for 320 kHz (default), 6 for 256 kHz and 7 for 160kHz and 40kHz
   */
  public int getLowPassFrequency() {
    return lowPassFrequency;
  }

  /**
   * Sets the low pass frequency. Ideal value depends on Link Frequency. Suggested values are 0 for 640kHz, 4 for 320
   * kHz (default), 6 for 256 kHz and 7 for 160kHz and 40kHz
   * 
   * @param lowPassFrequency the lowPassFrequency to set [0,7]
   */
  public void setLowPassFrequency(int lowPassFrequency) {
    this.lowPassFrequency = lowPassFrequency;
  }

  /**
   * @return the true if the mixer input attenuation is enable
   */
  public boolean isMixerInputAttenuation() {
    return isMixerInputAttenuation;
  }

  /**
   * Sets the mixer input attenuation
   * 
   * @param isMixerInputAttenuation true to enable the mixer input attenuation
   */
  public void setMixerInputAttenuation(boolean isMixerInputAttenuation) {
    this.isMixerInputAttenuation = isMixerInputAttenuation;
  }

  /**
   * @return the tag encoding (0: FMO, 1: MILLER2, 2: MILLER4, 3: MILLER8)
   */
  public int getEncoding() {
    return encoding;
  }

  /**
   * Sets the encoding of the tag answer
   * 
   * @param encoding the encoding to set (0: FMO, 1: MILLER2, 2: MILLER4, 3: MILLER8)
   */
  public void setEncoding(int encoding) {
    this.encoding = encoding;
  }

  /**
   * @return the no response wait time in 25,6&micro;s steps [0,255]
   */
  public int getNoResponseWaitTime() {
    return noResponseWaitTime;
  }

  /**
   * sets the no response wait time in 25,6&micro;s steps [0,255]
   * 
   * @param noResponseWaitTime the time to set [0,255]
   */
  public void setNoResponseWaitTime(int noResponseWaitTime) {
    this.noResponseWaitTime = noResponseWaitTime;
  }

  /**
   * @return the true if PR-ASK is activated, else is DSB-ASK activated
   */
  public boolean isPRASK() {
    return isPRASK;
  }

  /**
   * @param isPRASK set true to Activate Phase Reversal Amplitude Shift Keyed (PR-ASK) modulation otherwise Double
   *        Sideband Amplitude Shift Keyed (DSB-ASK)
   */
  public void setPRASK(boolean isPRASK) {
    this.isPRASK = isPRASK;
  }

  /**
   * @return the transmitterPower in dBm
   */
  public int getTransmitterPower() {
    return transmitterPower;
  }

  /**
   * @param transmitterPower the transmitter power to set, allowed values depends on the reader type
   */
  public void setTransmitterPower(int transmitterPower) {
    this.transmitterPower = transmitterPower;
  }

  /**
   * @return the time to wait before the receiver is activated in 6.4&micro;s steps [0,255]
   */
  public int getRxWaitTime() {
    return rxWaitTime;
  }

  /**
   * @param rxWaitTime the time to wait before the receiver is activated. Multiplier is 6.4&micro;s [0,255]
   */
  public void setRxWaitTime(int rxWaitTime) {
    this.rxWaitTime = rxWaitTime;
  }

  /**
   * @return the receiver gain, value must be multiplied by 3 dB
   */
  public int getReceiverGain() {
    return receiverGain;
  }

  /**
   * @param receiverGain the receiver gain, internally multiplied by 3 dB
   */
  public void setReceiverGain(int receiverGain) {
    this.receiverGain = receiverGain;
  }

  /**
   * @return the receiver settling speed up state
   */
  public boolean isSettlingSpeedUp() {
    return isSettlingSpeedUp;
  }

  /**
   * @param isSettlingSpeedUp if true, the receiver reading a bit faster for most tags
   */
  public void setSettlingSpeedUp(boolean isSettlingSpeedUp) {
    this.isSettlingSpeedUp = isSettlingSpeedUp;
  }

  /**
   * @return the tari value from EPC Gen2 (1 for 12.5&micro;s, 2 for 25&micro;s)
   */
  public int getTari() {
    return tari;
  }

  /**
   * @param tari the Tari value from EPC Gen2: 1 for 12.5&micro;s, 2 for 25&micro;s
   */
  public void setTari(int tari) {
    this.tari = tari;
  }

  /**
   * @return the TRcal value from EPC Gen2. (e.g. if value = 667, the TRcal is 66.7&micro;s)
   */
  public int getTrcal() {
    return trcal;
  }

  /**
   * @param trcal the TRcal value from the EPC Gen2. Default is 66.7&micro;s (value = 667)
   */
  public void setTrcal(int trcal) {
    this.trcal = trcal;
  }

}
