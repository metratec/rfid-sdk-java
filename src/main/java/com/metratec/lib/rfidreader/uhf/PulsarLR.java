/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.uhf;

import java.util.ArrayList;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * <b>PulsarLR UHF Long Range Reader</b><br>
 * The right tool for the hardest UHF RFID applications. This reader can reach a reading distance of up to 12m with a
 * modern UHF RFID transponder and easily scan a few hundred tags per second. The four antenna ports give you the
 * flexibility to build complex RFID devices, such as RFID gates and tunnels.
 *
 * @author mn
 *
 */
public class PulsarLR extends UHFReaderGen2 {
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "PULSAR_LR     01000100";

  private List<Integer> currentAntennaPowers = null;
  private List<Integer> currentConnectedMultiplexer = null;

  /**
   * Creates a new PulsarLR with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection the communication interface (instance of {@link ICommConnection})
   */
  public PulsarLR(String identifier, ICommConnection connection) {
    super(identifier, connection);
  }

  /**
   * Creates a new PulsarLR class for communicate with the specified metraTec usb PulsarLR
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public PulsarLR(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200));
  }

  /**
   * Creates a new PulsarLR class for communicate with the specified metraTec ethernet PulsarLR
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet reader
   * @param port port of the ethernet reader
   */
  public PulsarLR(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port));
  }

  /**
   * Creates a new PulsarLR class for communicate with the specified metraTec serial PulsarLR
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 reader is connected
   * @param baudrate baudrate of the rs232 reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  public PulsarLR(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl));
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    getCurrentAntennaPowers();
    return message;
  }

  /**
   * the power value per antenna (index 0 == antenna 1)
   * 
   * @return List with the power values
   * @throws CommConnectionException
   * @throws RFIDReaderException
   */
  protected List<Integer> getCurrentAntennaPowers() throws CommConnectionException, RFIDReaderException {
    String response = communicateSynchronized("AT+PWR?");
    // +PWR: 12,12,12,12
    String[] values = splitLine(response.substring(6));
    List<Integer> antennaPowers = new ArrayList<>();
    for (String value : values) {
      antennaPowers.add(Integer.parseInt(value));
    }
    // update values
    this.currentAntennaPowers = new ArrayList<>(antennaPowers);
    return antennaPowers;
  }

  /**
   * set the power values for the antennas
   * 
   * @param antennaPowers list with the multiplexer size for each antenna
   * @throws CommConnectionException
   * @throws RFIDReaderException
   */
  protected void setCurrentAntennaPowers(List<Integer> antennaPowers)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+PWR", antennaPowers.toArray());
    this.currentAntennaPowers = new ArrayList<>(antennaPowers);
  }

  /**
   * Gets the current antenna power
   * @param antenna the antenna
   * @return the current antenna power
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public int getAntennaPower(int antenna) throws CommConnectionException, RFIDReaderException {
    if (antenna <= 0) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, String.format("Antenna %d is not available", antenna));
    }
    List<Integer> antennaPowers = getCurrentAntennaPowers();
    try {
      return antennaPowers.get(antenna - 1);
    } catch (IndexOutOfBoundsException e) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, String.format("Antenna %d is not available", antenna));
    }
  }

  /**
   * Sets the antenna power
   * 
   * @param antenna the antenna
   * @param power the rfid power to set
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setAntennaPower(int antenna, int power) throws CommConnectionException, RFIDReaderException {
    if (antenna <= 0) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, String.format("Antenna %d is not available", antenna));
    }
    try {
      List<Integer> antennaPowers = new ArrayList<>(this.currentAntennaPowers);
      antennaPowers.set(antenna - 1, power);
      setCurrentAntennaPowers(antennaPowers);
    } catch (IndexOutOfBoundsException e) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, String.format("Antenna %d is not available", antenna));
    }
  }

  /**
   * Gets the configured multiplexer size per antenna (index 0 == antenna 1)
   * 
   * @return List with the configured multiplexer size
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  protected List<Integer> getCurrentConnectedMultiplexer() throws CommConnectionException, RFIDReaderException {
    String response = communicateSynchronized("AT+EMX?");
    // +EMX: 3,0,0,0
    String[] values = splitLine(response.substring(6));
    List<Integer> multiplexer = new ArrayList<>();
    for (String value : values) {
      multiplexer.add(Integer.parseInt(value));
    }
    // update values
    this.currentConnectedMultiplexer = new ArrayList<>(multiplexer);
    return multiplexer;
  }

  /**
   * define the connected multiplexer
   * 
   * @param connectedMultiplexer list with the multiplexer size for each antenna
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  protected void setCurrentConnectedMultiplexer(List<Integer> connectedMultiplexer)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+EMX", connectedMultiplexer.toArray());
    this.currentConnectedMultiplexer = new ArrayList<>(connectedMultiplexer);
  }

  /**
   * Get the connected multiplexer (connected antennas per antenna port)
   * 
   * @param antennaPort the antenna port to which the multiplexer is connected
   * @return the multiplexer size
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public int getMultiplexer(int antennaPort) throws CommConnectionException, RFIDReaderException {
    if (1 > antennaPort || antennaPort > 4) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, String.format("Antenna %d is not available", antennaPort));
    }
    List<Integer> multiplexer = getCurrentConnectedMultiplexer();
    return multiplexer.get(antennaPort - 1);
  }

  /**
   * Sets the connected multiplexer (connected antennas per antenna port)
   * 
   * @param antennaPort the antenna port to which the multiplexer is connected
   * @param multiplexer the multiplexer size
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setMultiplexer(int antennaPort, int multiplexer) throws CommConnectionException, RFIDReaderException {
    if (1 > antennaPort || antennaPort > 4) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, String.format("Antenna %d is not available", antennaPort));
    }
    List<Integer> connectedMultiplexer = new ArrayList<>(this.currentConnectedMultiplexer);
    connectedMultiplexer.set(antennaPort - 1, multiplexer);
    setCurrentConnectedMultiplexer(connectedMultiplexer);
    // update antennas power values
    getCurrentAntennaPowers();
  }

  @Override
  public int getMultiplexAntennas() throws CommConnectionException, RFIDReaderException {
    // +MUX: 1
    String response = communicateSynchronized("AT+MUX?");
    String[] responses = splitLine(response.substring(6));
    if (responses.length > 1) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA,
          "A multiplex sequence is activated, please use 'getMultiplexAntennasSequence' command");
    }
    return Integer.parseInt(responses[0]);
  }

  /**
   * Gets the multiplex antenna sequence
   * 
   * @return List with the antenna sequence
   * @throws CommConnectionException
   * @throws RFIDReaderException
   */
  public List<Integer> getMultiplexAntennaSequence() throws CommConnectionException, RFIDReaderException {
    String response = communicateSynchronized("AT+MUX?");
    // +MUX: 1,2,3,....
    String[] values = splitLine(response.substring(6));
    if (values.length == 1) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA,
          "No multiplex sequence activated, please use 'getMultiplexAntennas' command");
    }
    List<Integer> sequence = new ArrayList<>();
    for (String value : values) {
      sequence.add(Integer.parseInt(value));
    }
    return sequence;
  }

  /**
   * Sets the multiplex antenna sequence
   * 
   * @param sequence the antenna sequence
   * @throws CommConnectionException
   * @throws RFIDReaderException
   */
  public void setMultiplexAntennaSequence(List<Integer> sequence) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+MUX", sequence.toArray());
  }
}
