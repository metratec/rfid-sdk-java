/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.tag;

/**
 * Class which contains the HF Tag Information
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public class HFTagInformation {
  /** tag id */
  private String tagID = "";
  /** Data storage format identifier support */
  private boolean isDSFIDSupported = false;
  /** Data storage format identifier */
  private int dsfid = 0;
  /** Application family identifier support */
  private boolean isAFISupported = false;
  /** Application family identifier */
  private int afi = 0;
  /** Vicinity integrated circuit card memory size support */
  private boolean isVICCMemorySizeSupported = false;
  /** Vicinity integrated circuit card block numbers */
  private int viccBlockCount = 0;
  /** Vicinity integrated circuit card block size */
  private int viccBlockSize = 0;
  /** IC Reference support */
  private boolean isICReferenceSupported = false;
  /** IC Reference */
  private int icReference = 0;

  /**
   * Construct a new HF tag information
   * 
   * @param tagID tag id
   */
  public HFTagInformation(String tagID) {
    this.tagID = tagID;
  }

  /**
   * Construct a new HF tag information
   * 
   * @param tagID tag id
   * @param isDSFIDSupported is DSFID supported
   * @param dsfid dsfid byte
   * @param isAFISupported is AFI supported
   * @param afi afi byte
   * @param isMemorySizeSupported is memory size supported
   * @param numberOfBlocks number of blocks
   * @param blockSize block size
   * @param isICReferenceSupported is reference supported
   * @param icReference ic reference
   */
  public HFTagInformation(String tagID, boolean isDSFIDSupported, int dsfid, boolean isAFISupported, int afi,
      boolean isMemorySizeSupported, int numberOfBlocks, int blockSize, boolean isICReferenceSupported, int icReference) {
    this.tagID = tagID;
    this.isDSFIDSupported = isDSFIDSupported;
    this.dsfid = dsfid;
    this.isAFISupported = isAFISupported;
    this.afi = afi;
    this.isVICCMemorySizeSupported = isMemorySizeSupported;
    this.viccBlockCount = numberOfBlocks;
    this.viccBlockSize = blockSize;
    this.isICReferenceSupported = isICReferenceSupported;
    this.icReference = icReference;
  }

  /**
   * return the tag id
   * 
   * @return tag id
   */
  public String getTagID() {
    return tagID;
  }

  /**
   * @return true if the Data storage format identifier is supported by the tag
   */
  public boolean isDSFIDSupported() {
    return isDSFIDSupported;
  }

  /**
   * @return the Data storage format identifier
   */
  public int getDSFID() {
    return dsfid;
  }

  /**
   * @return true if the Application family identifier is supported by the tag
   */
  public boolean isAFISupported() {
    return isAFISupported;
  }

  /**
   * @return the Application family identifier
   */
  public int getAFI() {
    return afi;
  }

  /**
   * @return true if the vicinity integrated circuit card memory size is supported by the tag
   */
  public boolean isMemorySizeSupported() {
    return isVICCMemorySizeSupported;
  }

  /**
   * @return the vicinity integrated circuit card memory size
   */
  public int getMemorySize() {
    return viccBlockCount * viccBlockSize;
  }

  /**
   * @return the vicinity integrated circuit card block size
   */
  public int getBlockSize() {
    return viccBlockSize;
  }

  /**
   * @return the vicinity integrated circuit card number of blocks
   */
  public int getNumberOfBlocks() {
    return viccBlockCount;
  }

  /**
   * @return true if the IC Reference is supported by the tag
   */
  public boolean isICReferenceSupported() {
    return isICReferenceSupported;
  }

  /**
   * @return the IC Reference
   */
  public int getICReference() {
    return icReference;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("tagId: ");
    buffer.append(getTagID());
    buffer.append("\risDSFIDSupported: ");
    buffer.append(isDSFIDSupported());
    if (isDSFIDSupported()) {
      buffer.append("\rdsfid: ");
      buffer.append(getDSFID());
    }
    buffer.append("\risAFISupported: ");
    buffer.append(isAFISupported());
    if (isAFISupported()) {
      buffer.append("\rafi: ");
      buffer.append(getAFI());
    }
    buffer.append("\risVICCMemorySizeSupported: ");
    buffer.append(isMemorySizeSupported());
    if (isMemorySizeSupported()) {
      buffer.append("\rviccBlockCount: ");
      buffer.append(getNumberOfBlocks());
      buffer.append("\rviccBlockSize: ");
      buffer.append(getBlockSize());
    }
    buffer.append("\risICReferenceSupported: ");
    buffer.append(isICReferenceSupported());
    if (isICReferenceSupported()) {
      buffer.append("\ricReference: ");
      buffer.append(getICReference());
    }
    return buffer.toString();
  }
}
