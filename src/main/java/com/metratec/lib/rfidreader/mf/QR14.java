/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.mf;

import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;

/**
 * <b>QR14 Plug-In Module</b><br>
 * Compact Mifare Plug-In Module with integrated Antenna and a read range of up to 50 mm
 * 
 * @author mn
 *
 */
public class QR14 extends MFReader {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISON = "QR14 01010214";

  /**
   * Creates a new QR14 with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public QR14(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISON);
  }

  /**
   * Creates a new QR14 class for communicate with the specified metraTec usb QR14
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public QR14(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISON);
  }

  /**
   * Creates a new QR14 class for communicate with the specified metraTec ethernet QR14
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public QR14(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port), MIN_READER_REVISON);
  }

  /**
   * Creates a new QR14 class for communicate with the specified metraTec serial QR14
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public QR14(String identifier, String portName, int baudrate, int dataBit, int stopBit,
      int parity, int flowControl) {
    super(identifier,
        new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISON);
  }


}
