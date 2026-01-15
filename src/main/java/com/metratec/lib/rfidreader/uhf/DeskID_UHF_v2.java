/*******************************************************************************
 * S * Copyright (c) 2026 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.uhf;

import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>DeskID UHF</b><br>
 * The DeskID UHF is a compact and well-priced RFID reader/writer working at 868 MHz (UHF RFID, EU) or 902 – 928 MHz (FCC, USA).
 * Its main use is to read and write data to EPC Gen 2 transponders directly from your PC or laptop. Thus, the device is a handy
 * tool for all UHF applications for testing tags, writing an EPC, or just debugging your UHF gate.
 * 
 * @author mn
 *
 */
public class DeskID_UHF_v2 extends UHFReaderAT {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "DeskID_UHF_v2 01000106";

  /**
   * Creates a new DeskID_UHF with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public DeskID_UHF_v2(String identifier, ICommConnection connection) {
    super(identifier, connection);
  }

  /**
   * Creates a new DeskID_UHF class for communicate with the specified metraTec usb DeskID_UHF
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public DeskID_UHF_v2(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200));
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
  public DeskID_UHF_v2(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl));
  }

  @Override
  public List<Integer> getMultiplexAntennas() throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "Multiple antennas are not supported by DeskId Uhf.");
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

  @Override
  public int getAntennaPort() throws CommConnectionException, RFIDReaderException {
    return 1;
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
