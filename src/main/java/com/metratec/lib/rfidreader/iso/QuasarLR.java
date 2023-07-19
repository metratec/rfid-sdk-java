/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import java.util.Arrays;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>Quasar Long Range Industrial HF Reader/Writer</b><br>
 * The QuasarLR is an HF long range RFID reader/writer for demanding industrial applications, where high reading
 * reliability, high read ranges and extensive special tag features are needed. Highlights include an RF power of up to
 * 4 W which allows a read range of up to 60cm with the right antenna (e.g. our Lambda-40 Loop Antenna). This allows
 * even difficult applications directly at conveyor belts, in production machinery and in laundry applications.
 * 
 * @author mn
 *
 */
public class QuasarLR extends ISOReader {

  /**
   * Minimal reader revision
   */
  protected final static String MIN_READER_REVISION = "QuasarLR 01000220";
  private Integer currentAntenna = null;

  /**
   * Creates a new QuasarLR with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public QuasarLR(String identifier, ICommConnection connection) {
    super(identifier, connection, MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public QuasarLR(String identifier, ICommConnection connection, MODE mode, SRI sri) {
    super(identifier, connection, mode, sri, MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR class for communicate with the specified metraTec usb QuasarLR
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public QuasarLR(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR class for communicate with the specified metraTec ethernet QuasarLR
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public QuasarLR(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port), MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR class for communicate with the specified metraTec serial QuasarLR
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public QuasarLR(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl),
        MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR class for communicate with the specified metraTec usb QuasarLR
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public QuasarLR(String identifier, String usbDeviceSerialNumber, MODE mode, SRI sri) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, sri, MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR class for communicate with the specified metraTec ethernet QuasarLR
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   * @param mode {@link ISOReader.MODE}
   * @param sri {@link ISOReader.SRI}
   */
  public QuasarLR(String identifier, String ipAddress, int port, MODE mode, SRI sri) {
    super(identifier, new TcpConnection(ipAddress, port), mode, sri, MIN_READER_REVISION);
  }

  /**
   * Creates a new QuasarLR class for communicate with the specified metraTec serial QuasarLR
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
  public QuasarLR(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, MODE mode, SRI sri) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode, sri,
        MIN_READER_REVISION);
  }

  /**
   * The QuasarLR allows different output power levels to match antenna size, tag size or tag position. The power level
   * is given in milliwatt (mW). The minimum value is 500, the maximum is 8000 with steps of 250.
   * 
   * @param value power in mW from 500(mW) to 8000(mW) in 250(mW) steps
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  @Override
  @SuppressWarnings("PMD.UselessOverridingMethod")
  public void setPower(int value) throws CommConnectionException, RFIDReaderException {
    // only overwritten to fit the description
    super.setPower(value);
  }

  /**
   * @return the current {@link QuasarLRStatus}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public QuasarLRStatus getStatus() throws CommConnectionException, RFIDReaderException {
    String[] answers = communicateSynchronized("STA");
    if (answers.length >= 8 && answers[0].equals("OK!")) {
      QuasarLRStatus status = new QuasarLRStatus();
      status.put(QuasarLRStatus.READER_NAME, getIdentifier());
      status.put(QuasarLRStatus.TIMESTAMP, System.currentTimeMillis());
      status.put(QuasarLRStatus.ANTENNA, currentAntenna);
      for (String answer : answers) {
        String[] split = answer.split(":");
        if (split.length >= 2) {
          status.put(split[0], split[1].trim());
        }
      }
      return status;
    } else {
      throw new RFIDReaderException(RFIDErrorCodes.NER,
          "Wrong quasar lr status request answer: " + Arrays.toString(answers));
    }

  }

  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    super.setAntennaPort(port);
    currentAntenna = port;
  }

  /**
   * @return the current noise measure of the connected antenna
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public QuasarLRNoiseMeasure getNoiseMeasure() throws CommConnectionException, RFIDReaderException {
    String[] answers = communicateSynchronized("DRT 010B000304E700A8010000 CRC");
    long timestamp = System.currentTimeMillis();
    // String[] answers = {"COK", "0D00003CE7BA00D60013013E7D"};
    if (answers.length >= 2 && answers[0].equals("COK")) {
      byte[] arr = getByteFromHexString(answers[1]);
      return new QuasarLRNoiseMeasure(getIdentifier(), timestamp, currentAntenna, arr[6] << 8 | arr[5],
          arr[8] << 8 | arr[7], arr[10] << 8 | arr[9]);
    } else {
      throw new RFIDReaderException(RFIDErrorCodes.NER,
          "Wrong quasar lr noise measure request answer: " + Arrays.toString(answers));
    }
  }

  /**
   * get the reader answer from an request command acc to ISO15693
   * 
   * @param reqCommand request command
   * @return tag answer or null if no tag response. In case of write tag data this method returns an empty string if
   *         write is ok
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>CLD, Collision detected</li>
   *         <li>CER, CRC error</li>
   *         <li>TEC, Tag Error code</li>
   *         <li>NER, No expected response</li>
   *         </ul>
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  @Override
  protected String sendRequest(String reqCommand) throws CommConnectionException, RFIDReaderException {
    String[] answers = communicateSynchronized(reqCommand);
    if (4 == answers.length) {
      if (answers[3].startsWith("NCL")) {
        // no collision detect
        if (answers[2].startsWith("COK")) {
          if (answers[1].startsWith("00")) {
            // the quasar lr doesn't append the transponder crc to the answer
            return answers[1].substring(2);
          } else
          // if(answers[1].startsWith("01"))
          {
            throw new RFIDReaderException(RFIDErrorCodes.TEC, answers[1].substring(0, 4));
          }

        } else {
          throw new RFIDReaderException(RFIDErrorCodes.CER, "CRC error");
        }
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.CLD, "Collision detect");
      }
    } else if (1 == answers.length) {
      if (answers[0].equals("TNR")) {
        throw new RFIDReaderException(RFIDErrorCodes.TNR, answers[0]);
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER, answers[0]);
      }
    } else if (0 == answers.length) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "no data from reader");
    } else {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "wrong data: " + Arrays.toString(answers));
    }
  }
}
