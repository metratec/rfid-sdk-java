/*******************************************************************************
 * S * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>DeskID ISO</b><br>
 * The DeskID ISO is an especially compact HF RFID reader / writer for use in office and home environments. Typical
 * applications include customer management (e.g. in sports studios), the configuration of access control systems and
 * all other applications in which RFID tags are to be read with a PC or notebook computer. The low profile design
 * housing is meant for desktop use and allows every computer user to read and write RFID tags according to ISO 15693
 * (as long as they have not previously been locked). The read range varies between 5 and 10 cm depending on tag type.
 * Reading of multiple tags (anti collision feature) is also supported.
 * 
 * @author mn
 *
 */
public class DeskID_ISO extends ISOReader {

  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "DESKID_ISO 02000307";
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION_OLD = "DESKID_ISO 01000218";


  /**
   * Creates a new DeskID_ISO with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public DeskID_ISO(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new DeskID_ISO with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public DeskID_ISO(String identifier, ICommConnection connection, MODE mode, SRI sri) {
    super(identifier, connection, mode, sri, MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new DeskID_ISO class for communicate with the specified metraTec usb DeskID_ISO
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public DeskID_ISO(String identifier, String usbDeviceSerialNumber, MODE mode, SRI sri) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, sri, MIN_READER_REVISION,
        MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new DeskID_ISO class for communicate with the specified metraTec usb DeskID_ISO
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public DeskID_ISO(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new DeskID_ISO class for communicate with the specified metraTec serial DeskID_ISO
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
  public DeskID_ISO(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, MODE mode, SRI sri) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode, sri,
        MIN_READER_REVISION, MIN_READER_REVISION_OLD);
  }

  /**
   * Creates a new DeskID_ISO class for communicate with the specified metraTec serial DeskID_ISO
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public DeskID_ISO(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
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

  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS,
        "DeskID ISO has a integrated antenna, switching is not available");
  }
}
