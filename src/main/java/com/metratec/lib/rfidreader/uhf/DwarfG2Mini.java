/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.uhf;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>DwarfG2 UHF SMD Module</b><br>
 * A compact but powerful short-range UFH module for direct SMD soldering and external antenna connector
 * 
 * @author mn
 *
 */
public class DwarfG2Mini extends UHFReader {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISON = "DWARFG2_MINI 01000315";

  /**
   * Creates a new DwarfG2 with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public DwarfG2Mini(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public DwarfG2Mini(String identifier, ICommConnection connection, UHFReader.READER_MODE mode) {
    super(identifier, connection, mode, MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 class for communicate with the specified metraTec usb DwarfG2
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public DwarfG2Mini(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 class for communicate with the specified metraTec ethernet DwarfG2
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public DwarfG2Mini(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port), MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 class for communicate with the specified metraTec serial DwarfG2
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public DwarfG2Mini(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 class for communicate with the specified metraTec usb DwarfG2
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public DwarfG2Mini(String identifier, String usbDeviceSerialNumber, UHFReader.READER_MODE mode) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 class for communicate with the specified metraTec ethernet DwarfG2
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public DwarfG2Mini(String identifier, String ipAddress, int port, UHFReader.READER_MODE mode) {
    super(identifier, new TcpConnection(ipAddress, port), mode, MIN_READER_REVISON);
  }

  /**
   * Creates a new DwarfG2 class for communicate with the specified metraTec serial DwarfG2
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public DwarfG2Mini(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, UHFReader.READER_MODE mode) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode,
        MIN_READER_REVISON);
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    // not available
    // prepareInputsForEventHandling();

    // TRS not available
    addTRS(false);
    return message;
  }

  @Override
  public int getAntennaPort() throws CommConnectionException, RFIDReaderException {
    return 0;
  }

  @Override
  public void enableAdditionalTRS(boolean state) throws CommConnectionException, RFIDReaderException {
    // not available
    // super.enableAdditionalTRS(state);
  }


}
