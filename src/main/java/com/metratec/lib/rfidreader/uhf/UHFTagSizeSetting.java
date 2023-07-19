package com.metratec.lib.rfidreader.uhf;

public class UHFTagSizeSetting {
  private Integer start;
  private Integer min;
  private Integer max;

  /**
   * @param start expected number of tags
   */
  public UHFTagSizeSetting(Integer start) {
    this.start = start;
  }

  /**
   * @param start expected number of tags
   * @param min minimum number of tags
   * @param max maximum number of tags
   */
  public UHFTagSizeSetting(Integer start, Integer min, Integer max) {
    this.start = start;
    this.min = min;
    this.max = max;
  }

  /**
   * @return the start
   */
  public Integer getStart() {
    return start;
  }

  /**
   * @param start the start to set
   */
  public void setStart(Integer start) {
    this.start = start;
  }

  /**
   * @return the min
   */
  public Integer getMin() {
    return min;
  }

  /**
   * @param min the min to set
   */
  public void setMin(Integer min) {
    this.min = min;
  }

  /**
   * @return the max
   */
  public Integer getMax() {
    return max;
  }

  /**
   * @param max the max to set
   */
  public void setMax(Integer max) {
    this.max = max;
  }

}
