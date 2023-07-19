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
 * <b>QuasarMF Mifare Reader</b><br>
 * The QuasarMF is an industrial RFID Reader for transponders that communicate using the ISO14443A
 * and the MIFARE® protocols MIFARE Classic®, MIFARE Ultralight®. It is one of a few readers
 * worldwide using MIFARE technology that has a single-ended antenna output which allows using
 * any HF RFID antenna instead of specially designed MIFARE antennas. This makes it even possible
 * to connect the reader to a multiplexer and read tags from several antennas sequentially.
 * 
 * @author mn
 *
 */
public class QuasarMF extends MFReader {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISON = "QUASAR_MIFARE 01010214";

  /**
   * Creates a new QuasarMF with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public QuasarMF(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISON);
  }

  /**
   * Creates a new QuasarMF class for communicate with the specified metraTec usb QuasarMF
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public QuasarMF(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISON);
  }

  /**
   * Creates a new QuasarMF class for communicate with the specified metraTec ethernet QuasarMF
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public QuasarMF(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port), MIN_READER_REVISON);
  }

  /**
   * Creates a new QuasarMF class for communicate with the specified metraTec serial QuasarMF
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public QuasarMF(String identifier, String portName, int baudrate, int dataBit, int stopBit,
      int parity, int flowControl) {
    super(identifier,
        new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISON);
  }


}
