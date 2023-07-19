/*******************************************************************************
 * S * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.uhf;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>DeskID UHF</b><br>
 * The DeskID UHF is a small and economical RFID reader and writer for the use with 868 MHz (UHF RFID, EU frequency). It
 * allows reading and writing EPC Class 1 Gen 2 tags directly with your PC or notebook computer. This makes it an
 * indispensable tool in UHF applications in which tags have to be tested, written to or initialized easily. The low
 * profile design housing looks great on any desktop and is connected to the PC via USB. As it is also USB powered,
 * setup is especially easy. Reading range depends on tag type and is typically in the range of 10 to 30 cm. Reading
 * several tags at once (anti collision mode) is also possible.
 * 
 * @author mn
 *
 */
public class DeskID_UHF extends UHFReader {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "DESKID_UHF 01010315";

  /**
   * Creates a new DeskID_UHF with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public DeskID_UHF(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISION);
  }

  /**
   * Creates a new DeskID_UHF with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public DeskID_UHF(String identifier, ICommConnection connection, UHFReader.READER_MODE mode) {
    super(identifier, connection, mode, MIN_READER_REVISION);
  }

  /**
   * Creates a new DeskID_UHF class for communicate with the specified metraTec usb DeskID_UHF
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public DeskID_UHF(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISION);
  }

  /**
   * Creates a new DeskID_UHF class for communicate with the specified metraTec serial DeskID_UHF
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public DeskID_UHF(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISION);
  }

  /**
   * Creates a new DeskID_UHF class for communicate with the specified metraTec usb DeskID_UHF
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public DeskID_UHF(String identifier, String usbDeviceSerialNumber, UHFReader.READER_MODE mode) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, MIN_READER_REVISION);
  }

  /**
   * Creates a new DeskID_UHF class for communicate with the specified metraTec serial DeskID_UHF
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
  public DeskID_UHF(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, UHFReader.READER_MODE mode) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode,
        MIN_READER_REVISION);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.uhf.UHFReader#switchAntennas(int)
   */
  @Override
  public void setMultiplexAntennas(int numberOfAntennas) throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "Switching the antennas is not supported by DeskId Uhf.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.uhf.UHFReader#setAntennaPort(int)
   */
  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "Setting the antenna is not supported by DeskId Uhf.");
  }
}
