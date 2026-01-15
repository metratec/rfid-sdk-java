package com.metratec.lib.rfidreader;

/**
 * The exception that is triggered when a transponder returns an error
 */
public class RFIDTransponderException extends RFIDReaderException{
  private static final long serialVersionUID = 5641677542627925980L;

  /**
   * Constructs a new RFID reader exception.
   * 
   * @see java.lang.Exception
   */
  public RFIDTransponderException() {
    super();
  }

  /**
   * Constructs a new RFID reader exception with the specified detail message.
   * 
   * @param s the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   * @see java.lang.Exception#Exception(String message)
   */
  public RFIDTransponderException(String s) {
    super(s);
  }

  /**
   * Constructs a new RFID reader exception with the specified error code.
   * 
   * @param errorCode the error code. The detail message is saved for later retrieval by the {@link #getErrorCode()}
   *        method.
   */
  public RFIDTransponderException(int errorCode) {
    super(errorCode);
  }

  /**
   * Constructs a new RFID reader exception with the specified detail message and error code.
   * 
   * @param errorCode the error code. The detail message is saved for later retrieval by the {@link #getErrorCode()}
   *        method.
   * @param s the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public RFIDTransponderException(int errorCode, String s) {
    super(errorCode, s);
  }
}
