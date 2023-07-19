/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>Dwarf15 HF SMD Module</b><br>
 * One of the smallest HF RFID modules in the world for direct SMD soldering
 * 
 * @author mn
 *
 */
public class Dwarf15 extends ISOReader {

  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "DWARF15 02000307";
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION_OLD = "DWARF15 01000218";

  /**
   * Creates a new Dwarf15 with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public Dwarf15(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public Dwarf15(String identifier, ICommConnection connection, MODE mode, SRI sri) {
    super(identifier, connection, mode, sri, MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 class for communicate with the specified metraTec usb Dwarf15
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public Dwarf15(String identifier, String usbDeviceSerialNumber, MODE mode, SRI sri) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, sri, MIN_READER_REVISION,
        MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 class for communicate with the specified metraTec usb Dwarf15
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public Dwarf15(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 class for communicate with the specified metraTec ethernet Dwarf15
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public Dwarf15(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port), MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 class for communicate with the specified metraTec ethernet Dwarf15
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public Dwarf15(String identifier, String ipAddress, int port, MODE mode, SRI sri) {
    super(identifier, new TcpConnection(ipAddress, port), mode, sri, MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 class for communicate with the specified metraTec serial Dwarf15
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public Dwarf15(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new Dwarf15 class for communicate with the specified metraTec serial Dwarf15
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public Dwarf15(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, MODE mode, SRI sri) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode, sri,
        MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }


  /**
   * The second generation ISO 15693 devices with hardware revision &gt;= 02.00 allow setting power values of 100 or 200
   * (mW).
   * 
   * @param value power in mW - 100(mW) or 200(mW)
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  @Override
  public void setPower(int value) throws CommConnectionException, RFIDReaderException {
    if (Integer.parseInt(getHardwareRevision()) >= 200) {
      super.setPower(value);
    } else {
      throw new RFIDReaderException(RFIDErrorCodes.NOS, "Set power is not supported for hardware revision < 0200");
    }
  }

}
