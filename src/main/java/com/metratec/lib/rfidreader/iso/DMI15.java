/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.TcpConnection;

/**
 * <b>Iot HF Reader/Writer</b><br>
 * An industrial short range RFID reader for 13.56 MHz with an integrated antenna and a power supply via Ethernet (PoE).
 * Inputs and outputs are PLC compatible.
 * 
 * @author mn
 *
 */
public class DMI15 extends ISOReader {

  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "DMI15 02000102";

  /**
   * Creates a new DMI15 with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection connection
   * @param mode mode
   * @param sri sri
   */
  public DMI15(String identifier, ICommConnection connection, MODE mode, SRI sri) {
    super(identifier, connection, mode, sri, MIN_READER_REVISION);
  }

  /**
   * Creates a new DMI15 with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection connection
   */
  public DMI15(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISION);
  }

  /**
   * Creates a new DMI15 class
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet uhf reader
   * @param port port of the ethernet uhf reader
   * @param mode mode
   * @param sri sri
   */
  public DMI15(String identifier, String ipAddress, int port, MODE mode, SRI sri) {
    this(identifier, new TcpConnection(ipAddress, port), mode, sri);
  }

  /**
   * Creates a new DMI15 class
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet uhf reader
   * @param port port of the ethernet uhf reader
   */
  public DMI15(String identifier, String ipAddress, int port) {
    this(identifier, new TcpConnection(ipAddress, port));
  }

}
