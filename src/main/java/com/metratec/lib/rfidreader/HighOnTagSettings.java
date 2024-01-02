package com.metratec.lib.rfidreader;

/**
 * Class for the reader high on tag settings
 */
public class HighOnTagSettings {
  private boolean enable;
  private Integer outputPin;
  private Integer duration;

  /**
   * @param enable set to false for disable the high on tag feature
   */
  public HighOnTagSettings(boolean enable) {
    this.enable = enable;
  }

  /**
   * @param outputPin the output pin that signals a found tag
   * @param duration pin high duration in milliseconds [100..1000]
   */
  public HighOnTagSettings(Integer outputPin, Integer duration) {
    this(true);
    this.outputPin = outputPin;
    this.duration = duration;
  }

  /**
   * @return if the feature is enabled
   */
  public boolean isEnable() {
    return enable;
  }

  /**
   * @param enable set to false for disable the high on tag feature
   */
  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  /**
   * @return the output pin that signals a found tag
   */
  public Integer getOutputPin() {
    return outputPin;
  }

  /**
   * @param outputPin he output pin that signals a found tag
   */
  public void setOutputPin(Integer outputPin) {
    this.outputPin = outputPin;
  }

  /**
   * @return pin high duration in milliseconds
   */
  public Integer getDuration() {
    return duration;
  }

  /**
   * @param duration pin high duration in milliseconds [100..1000]
   */
  public void setDuration(Integer duration) {
    this.duration = duration;
  }


}
