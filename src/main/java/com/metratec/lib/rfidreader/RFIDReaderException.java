/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
/**
 * Exception for RFIDReader
 * 
 * @author Matthias Neumann
 * @version 0.1
 *
 */
package com.metratec.lib.rfidreader;

/**
 * The Exception Class for the metraTec RFID readers
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public class RFIDReaderException extends Exception {
  private static final long serialVersionUID = 1L;
  private int errorcode = 0;

  /**
   * Constructs a new RFID reader exception.
   * 
   * @see java.lang.Exception
   */
  public RFIDReaderException() {
    super();
  }

  /**
   * Constructs a new RFID reader exception with the specified detail message.
   * 
   * @param s the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   * @see java.lang.Exception#Exception(String message)
   */
  public RFIDReaderException(String s) {
    super(s);
  }

  /**
   * Constructs a new RFID reader exception with the specified error code.
   * 
   * @param errorcode the error code. The detail message is saved for later retrieval by the {@link #getErrorCode()}
   *        method.
   */
  public RFIDReaderException(int errorcode) {
    super();
    this.errorcode = errorcode;
  }

  /**
   * Constructs a new RFID reader exception with the specified detail message and error code.
   * 
   * @param errorcode the error code. The detail message is saved for later retrieval by the {@link #getErrorCode()}
   *        method.
   * @param s the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public RFIDReaderException(int errorcode, String s) {
    super(s);
    this.errorcode = errorcode;
  }

  /**
   * @return the error code ('0' if not initialize).
   */
  public int getErrorCode() {
    return errorcode;
  }
}
