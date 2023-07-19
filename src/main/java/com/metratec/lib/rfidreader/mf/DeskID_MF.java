/*******************************************************************************
 * S * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.mf;

import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.UsbConnection;

/**
 * <b>DeskID Mifare</b><br>
 * The DeskID MF is an especially compact HF RFID reader and writer for office RFID applications with ISO14443 tags.
 * Typical scenarios for using the DeskID MF are customer management applications (e.g. in the gym), and configuring of
 * door access systems or customer cards based on the common protocols MIFARE Classic®, MIFARE Ultralight®
 * 
 * @author mn
 *
 */
public class DeskID_MF extends MFReader {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISON = "DESKID_MIFARE 01010214";

  /**
   * Creates a new DeskID_MF with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public DeskID_MF(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISON);
  }

  /**
   * Creates a new DeskID_MF class for communicate with the specified metraTec usb DeskID_MF
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public DeskID_MF(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISON);
  }

  /**
   * Creates a new DeskID_MF class for communicate with the specified metraTec serial DeskID_MF
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public DeskID_MF(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISON);
  }

}
