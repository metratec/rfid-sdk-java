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
import com.metratec.lib.rfidreader.ReaderType;

/**
 * <b>PulsarMX UHF Mid Range Reader</b><br>
 * The PulsarMX is a UHF RFID Reader for applications with medium read range between 2 and 5 m and up to 100 tags at the
 * same time in the field (anti collision). Typical applications include container tracking, reading data from sensor
 * tags or as a scanning station on a conveyor belt. With its low cost, it open up new possibilities for RFID which were
 * not economical before.
 * 
 * @author mn
 *
 */
public class PulsarMX extends UHFReader {
  /**
   * Minimal reader revision
   */
  protected final static String MIN_READER_REVISION = "PULSAR_MX     01000315";
  protected final static ReaderType BLA = ReaderType.DESKID_ISO;

  /**
   * Creates a new PulsarMX with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public PulsarMX(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public PulsarMX(String identifier, ICommConnection connection, READER_MODE mode) {
    super(identifier, connection, mode, MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX class for communicate with the specified metraTec usb PulsarMX
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public PulsarMX(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX class for communicate with the specified metraTec ethernet PulsarMX
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public PulsarMX(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port), MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX class for communicate with the specified metraTec serial PulsarMX
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public PulsarMX(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX class for communicate with the specified metraTec usb PulsarMX
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public PulsarMX(String identifier, String usbDeviceSerialNumber, READER_MODE mode) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX class for communicate with the specified metraTec ethernet PulsarMX
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   * @param mode the used RFID communication standart. ({@link UHFReader READER_MODE})
   */
  public PulsarMX(String identifier, String ipAddress, int port, READER_MODE mode) {
    super(identifier, new TcpConnection(ipAddress, port), mode, MIN_READER_REVISION);
  }

  /**
   * Creates a new PulsarMX class for communicate with the specified metraTec serial PulsarMX
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
  public PulsarMX(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, READER_MODE mode) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode,
        MIN_READER_REVISION);
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    prepareInputsForEventHandling();
    return message;
  }
}
