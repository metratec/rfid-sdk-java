/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.tag.RfidTag;
import com.metratec.lib.tag.UhfTag;

/**
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public abstract class MetratecReaderGen2<T extends RfidTag> extends MetratecReader<T> {

  private int currentAntennaPort;
  private boolean useSingleAntenna = true;

  private Pattern splitResponsePattern = Pattern.compile("\r");
  private Pattern splitLinePattern = Pattern.compile(",");

  /**
   * Construct a new StandardReader instance with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection connection
   */
  public MetratecReaderGen2(String identifier, ICommConnection connection) {
    super(identifier, connection);
  }

  /**
   * Sends an command to the connected reader (appends crc and command end sign automatically)
   * 
   * @param command command
   * @param parameters command parameters
   * @return the reader answer as a string array
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  protected String communicateSynchronized(String command, Object... parameters)
      throws CommConnectionException, RFIDReaderException {
    if (receiveHandler.isConnected() && receiveHandler.isAlive()) {
      communicateLock.lock();
      try {
        clearResponseBuffer();
        receiveHandler.sendCommand(prepareCommand(command, parameters));

        String resp = receiveData();
        if (!resp.startsWith(command)) {
          throw new RFIDReaderException(RFIDErrorCodes.NER, command + "expected, " + resp + " received");
        }
        String data = "";
        while (true) {
          resp = receiveData();
          if (resp.startsWith("OK")) {
            return data;
          }
          data += resp;
          if (resp.startsWith("ERROR")) {
            if (data.contains("<") && data.contains(">")) {
              data = data.substring(data.indexOf("<") + 1, data.lastIndexOf(">"));
            }
            throw new RFIDReaderException(RFIDErrorCodes.NER, data);
          }
        }
      } finally {
        communicateLock.unlock();
      }
    } else {
      throw new CommConnectionException(ICommConnection.CONNECTION_LOST, "not connected");
    }
  }

  /**
   * add a new rfid tag event to the event handler
   * 
   * @param tagEvent the event
   */
  protected void addNewInventoryEvent(List<T> tags) {
    getInternalInventory().updateInventory(tags);
  }

  @Override
  public List<T> getInventory() throws RFIDReaderException, CommConnectionException {
    if (getInternalInventory().isAlive()) {
      return getInternalInventory().getInventory();
    } else {
      List<T> inv;
      if (useSingleAntenna) {
        // prefix_length = len("+INV: ")
        inv = parseInventory(splitResponse(communicateSynchronized("AT+INV")), 6, true); // prefix_length
      } else {
        // prefix_length = len("+MINV: ")
        inv = getInventoryMultiplex(true);
      }
      addNewInventoryEvent(inv);
      return inv;
    }
  }

  public List<T> getInventoryMultiplex() throws RFIDReaderException, CommConnectionException {
    return getInventoryMultiplex(true);
  }

  public List<T> getInventoryMultiplex(boolean throwAntennaErrors) throws RFIDReaderException, CommConnectionException {
    // parse multiple inventory
    // +MINV: <Antenna Error>
    // +MINV: <ROUND FINISHED, ANT=1>
    // +MINV: 3034257BF468D480000003EB
    // +MINV: <ROUND FINISHED, ANT=2>
    // +MINV: <Operation Error (6AC0B)>
    // +MINV: <ROUND FINISHED, ANT=3>
    // +MINV: <NO TAGS FOUND>
    // +MINV: <ROUND FINISHED, ANT=4>
    if (getInternalInventory().isAlive()) {
      return getInternalInventory().getInventory();
    } else {
      String[] answers = splitResponse(communicateSynchronized("AT+MINV"));
      List<T> inventory = new ArrayList<>();
      int last_index = 0;
      String errors = "";
      for (int i = 0; i < answers.length; i++) {
        if (answers[i].startsWith("+MINV: <R")) {
          // means antenna round finished
          int index = i + 1;
          String[] antenna = Arrays.copyOfRange(answers, last_index, index);
          // prefix_length = len("+MINV: ")
          try {
            inventory.addAll(parseInventory(antenna, 7, throwAntennaErrors));
          } catch (RFIDReaderException e) {
            if (!errors.isEmpty()) {
              errors += ", ";
            }
            errors += e.getMessage();
          }
          last_index = index;
        }
      }
      if (!errors.isEmpty()) {
        throw new RFIDReaderException(RFIDErrorCodes.ARH, errors);
      }
      return inventory;
    }
  }

  /**
   * parse the inventory response
   * 
   * @param answers reader answers
   * @param timestamp timestamp
   * @return a {@link List} with {@link UhfTag}s
   * @throws RFIDReaderException if an error occurs
   */
  protected List<T> parseInventory(String[] answers, int prefix_length) throws RFIDReaderException {
    return parseInventory(answers, prefix_length, false);
  }

  /**
   * parse the inventory response
   * 
   * @param answers reader answers
   * @param timestamp timestamp
   * @param throwError throwing antenna errors or not
   * @return a {@link List} with {@link UhfTag}s
   * @throws RFIDReaderException if an error occurs
   */
  protected abstract List<T> parseInventory(String[] answers, int prefix_length, boolean throwError)
      throws RFIDReaderException;

  protected abstract List<T> parseInventoryReport(String answers[], int prefix_length) throws RFIDReaderException;

  /**
   * Reset the reader
   * 
   * @throws RFIDReaderException if a error occurs, see {@link RFIDReaderException#getErrorCode()} and
   *         {@link RFIDReaderException#getMessage()} for more details
   * @throws CommConnectionException if a error occurs, see {@link CommConnectionException#getErrorCode()} and
   *         {@link CommConnectionException#getMessage()} for more details
   */
  public void reset() throws RFIDReaderException, CommConnectionException {
    communicateSynchronized("AT+RST");
    try {
      TimeUnit.MILLISECONDS.sleep(200);
    } catch (InterruptedException e) {
    }
    prepareDevice();
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    return prepareStandardReader();
  }

  /**
   * prepare the reader communication
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  private String prepareStandardReader() throws CommConnectionException, RFIDReaderException {
    configureConnection();
    setCurrentAntennaPort(getAntennaPort());
    enableInputEvents(true);
    return "connected";
  }

  /**
   * <b>For internal use!</b><br>
   * Sends an command to the connected reader (appends crc and command end sign automatically)
   * 
   * @param command command
   * @param parameters command parameters
   */
  @Override
  protected String prepareCommand(String command, Object... parameters) {
    StringBuffer sendCommand = new StringBuffer();
    boolean firstParameter = true;
    sendCommand.setLength(0);
    sendCommand.append(command);
    for (Object parameter : parameters) {
      if (null == parameter)
        continue;
      String value = String.valueOf(parameter);
      if (value.isEmpty())
        continue;
      if (firstParameter) {
        sendCommand.append('=');
        firstParameter = false;
      } else {
        sendCommand.append(',');
      }
      sendCommand.append(value);
    }
    sendCommand.append("\r");
    return sendCommand.toString();
  }

  /**
   * <b>For internal use!</b><br>
   * get the reader answer
   * 
   * @return the reader answer as a string array
   * @throws RFIDReaderException if an reader exception occurs
   */
  @Override
  protected String[] checkData(String answer) throws RFIDReaderException {
    return splitResponse(answer);
  }

  protected String[] splitResponse(String response) {
    return splitResponsePattern.split(response, 0);
  }

  protected String[] splitLine(String line) {
    return splitLinePattern.split(line, 0);
  }

  private boolean commandReceived = false;

  @Override
  protected boolean handleResponse(String response) {
    // check first char of message
    switch (response.charAt(0)) {
      case '\r':
        return false;
      case '\n':
        return false;
      case 'A':
        // AT command
        commandReceived = true;
        return true;
      case 'O':
        // OK
        commandReceived = false;
        return true;
      case 'E':
        // Error
        commandReceived = false;
        return true;
      case '+':
        if (commandReceived) {
          return true;
        }
        // Handle Event
        switch (response.charAt(0)) {
          case '+':
            switch (response.charAt(1)) {
              case 'H': // Heartbeat
                return false;
              case 'C':
                // Inventory event
                try {
                  if (response.charAt(2) == 'M') {
                    // prefix_length = len("+CMINV: ")
                    addNewInventoryEvent(parseInventory(splitResponse(response), 8));
                  } else if (response.charAt(5) == 'R') {
                    // prefix_length = len("+CINVR: ")
                    addNewInventoryEvent(parseInventoryReport(splitResponse(response), 8));
                  } else {
                    // prefix_length = len("+CINV: ")
                    addNewInventoryEvent(parseInventory(splitResponse(response), 7));
                  }
                } catch (RFIDReaderException e) {
                  getLogger().debug("Error parse inventory - {}", e.toString());
                }
                break;
              case 'I':
                // input changed
                if (response.startsWith("+IEV: ")) {
                  // +IEV: 1,HIGH
                  // +IEV: 2,LOW
                  String[] split = splitLine(response.substring(6));
                  getEventHandler().inputChange(new RfidReaderInputChange(getIdentifier(), System.currentTimeMillis(),
                      Integer.parseInt(split[0]), "HIGH".equals(split[1])));
                }
                break;
            }
            break;
        }
      default:
        break;
    }
    if (commandReceived) {
      return true;
    }
    return false;
  }

  /**
   * enable or disable the input events
   * 
   * @param enable set to true if the input events should be activated
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if error occurs
   */
  public void enableInputEvents(boolean enable) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+IEV", enable ? 1 : 0);
  }

  /**
   * check the current reader state (standby, continues read, crc, ..) and prepare the communication
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  private void configureConnection() throws CommConnectionException, RFIDReaderException {
    receiveHandler.setEOF(true);
    int configuredReceiveTimeout = getReceiveTimeout();
    setReceiveTimeout(3000); // Timeout for configure the connection
    // check CNR MODE and CRC MODE
    try {
      communicateLock.lock();
      clearResponseBuffer();
      send("ATI");
      String response;
      boolean check = true;
      while (check) {
        try {
          response = receiveRaw();
          // System.out.println("Recv: " + response.replace("\r", "<CR>").replace("\n", "<LF>"));
          if (response.contains("OK")) {
            check = false;
          }
        } catch (CommConnectionException e) {
          if (e.getErrorCode() == ICommConnection.RECV_TIMEOUT) {
            // reader sleeping?
            throw e;
          } else {
            throw e;
          }
        }
      }

    } finally {
      communicateLock.unlock();
      // reset the receive timeout
      setReceiveTimeout(configuredReceiveTimeout);
    }
    communicateSynchronized("ATE1");
    setHeartbeatInterval(this.heartBeatInterval);
  }

  /**
   * Check the antenna
   * 
   * @throws RFIDReaderException if an antenna error occurs
   * @throws CommConnectionException if an communication error occurs
   */
  public void checkAntennas() throws RFIDReaderException, CommConnectionException {
    int orgAntenna = currentAntennaPort;
    boolean orgUseSingleAntenna = useSingleAntenna;
    try {

      HashMap<Integer, String> antennasWithProblems = new HashMap<>();
      for (int i = 1; i <= 4; i++) {
        try {
          setAntennaPort(i);
        } catch (RFIDReaderException e) {
          // antenna port not available
          break;
        }

        try {
          getInventory();
        } catch (RFIDReaderException e) {
          antennasWithProblems.put(i, e.getMessage());
        }
      }
      if (antennasWithProblems.isEmpty()) {
        return;
      }
      StringBuilder message = new StringBuilder();
      antennasWithProblems.forEach((antenna, error) -> {
        if (message.length() > 0) {
          message.append(", ");
        }
        message.append("Antenna ").append(antenna).append(":").append(error);
      });
      throw new RFIDReaderException(RFIDErrorCodes.ARH, message.toString());
    } finally {
      currentAntennaPort = orgAntenna;
      useSingleAntenna = orgUseSingleAntenna;
    }
  }

  @Override
  public void stop() throws CommConnectionException {
    if (isConnected()) {
      try {
        stopInventory();
        stopInventoryReport();
      } catch (Exception e) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug(getIdentifier() + " error stop scanning " + e.getLocalizedMessage());
        }
      }
      try {
        followUp();
      } catch (Exception e) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug(getIdentifier() + " error reset configuration " + e.getLocalizedMessage());
        }
      }
    }
    super.stop();
  }

  /**
   * Follow up the reader, set eof and crc mode back
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  protected void followUp() throws RFIDReaderException, CommConnectionException {
    // TODO
    // setCRC(wasCRC);
    // setEndOfFrame(wasEOF);
  }

  /**
   * Sets the reader in a power saving mode
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void standby() throws RFIDReaderException, CommConnectionException {
    // TODO
    throw new RFIDReaderException(RFIDErrorCodes.RES, "not implemented yet");
  }

  /**
   * Ends the power saving mode
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void wakeUp() throws RFIDReaderException, CommConnectionException {
    // TODO
    throw new RFIDReaderException(RFIDErrorCodes.RES, "not implemented yet");
  }

  @Override
  protected void setHeartbeatInterval(int intervalInSeconds) throws RFIDReaderException, CommConnectionException {
    if (0 > intervalInSeconds || intervalInSeconds > 60) {
      throw new RFIDReaderException(RFIDErrorCodes.NOR, "Number out of range ([0,60])");
    }
    try {
      communicateSynchronized("AT+HBT", intervalInSeconds);
      receiveHandler.setHeartBeatInterval(intervalInSeconds);
    } catch (RFIDReaderException e) {
      getLogger().warn("Error set heartbeat - {}", e.toString());
      // disable hbt
      receiveHandler.setHeartBeatInterval(0);
    }
  }

  /**
   * Gets the firmware revision
   * 
   * @return the device type, hardware architecture and firmware revision of the reader
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public String[] getRevision() throws RFIDReaderException, CommConnectionException {
    // +SW: PULSAR_LR 0100
    // +HW: PULSAR_LR 0100
    // +SERIAL: 2020090817420000
    return checkData(communicateSynchronized("ATI"));
  }

  @Override
  public ReaderType getReaderType() throws RFIDReaderException, CommConnectionException {
    String[] info = getRevision();
    if (info[0].toLowerCase().contains(ReaderType.PULSAR_LR.getFirmwareName().toLowerCase())) {
      return ReaderType.PULSAR_LR;
    }
    return ReaderType.UNKNOWN;
  }

  /**
   * @param readerType {@link ReaderType}
   * @param expectedReaderRevisions expected reader revisions
   * @throws RFIDReaderException if an error occurs
   * @throws CommConnectionException if an error occurs
   */
  protected void checkReaderType(ReaderType readerType, String... expectedReaderRevisions)
      throws CommConnectionException, RFIDReaderException {
    List<String> allowedReaderVersions = Arrays.asList(expectedReaderRevisions);
    if (allowedReaderVersions.isEmpty()) {
      new RFIDReaderException(RFIDErrorCodes.WRT, "no expected reader given");
    }
    Collections.sort(allowedReaderVersions);
    for (String expected : allowedReaderVersions) {
      String expectedName = expected.substring(0, expected.length() - 8).trim();
      Integer minVersion = Integer.parseInt(expected.substring(expected.length() - 4));
      if (expectedName.equals(readerType.getFirmwareName())) {
        if (minVersion <= readerType.getFirmwareRevision()) {
          // ok
          return;
        }
        throw new RFIDReaderException(RFIDErrorCodes.WFR, "Reader " + readerType.getFirmwareRevision()
            + " found, but min Revision 0" + minVersion + " needed. Please Update the Reader Firmware.");
      }
    }
    throw new RFIDReaderException(RFIDErrorCodes.WRT,
        "Reader " + allowedReaderVersions.get(0) + " expected but " + readerType.getFirmwareName() + " connected");
  }

  @Override
  public String getFirmwareRevision() throws RFIDReaderException, CommConnectionException {
    String revision = getRevision()[0];
    // +SW: PULSAR_LR 0100
    return revision.substring(5, revision.length());
  }

  @Override
  public String getSerialNumber() throws RFIDReaderException, CommConnectionException {
    String revision = getRevision()[2];
    // +SERIAL: 2020090817420000
    return revision.substring(9, revision.length());
  }

  @Override
  public String getHardwareRevision() throws RFIDReaderException, CommConnectionException {
    String revision = getRevision()[1];
    // +HW: PULSAR_LR 0100
    return revision.substring(5, revision.length());
  }

  /**
   * Sets the verbosity level of the reader The different levels are:
   * <ul>
   * <li>0: Only necessary data (EPC, User Data, etc.) is returned. Tag errors are suppressed. Inventory only returns an
   * answer if any tag was found. The number of tags found is not send. Hardware triggered errors ( BOF CCE CRT PLE SRT
   * UER URE ) and parsing errors ( DNS EDX EHF EHX NCM NOR NOS NRF NSS UCO UPA WDL ) are send.</li>
   * <li>1: Default, most tag communication errors added. For multiple profiles active for every tag only one error
   * occurs.</li>
   * <li>2: All tag communication errors including RXE and CRE normally indicating a collision are send. Errors on every
   * tag are shown as often as they occur.</li>
   * </ul>
   * 
   * @param level level (0-2)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  protected void setVerbosityLevel(int level) throws CommConnectionException, RFIDReaderException {
    // TODO
    throw new RFIDReaderException(RFIDErrorCodes.RES, "not implemented yet");
  }


  @Override
  public boolean getInput(int pin) throws RFIDReaderException, CommConnectionException {
    if (1 > pin || pin > 2) {
      throw new RFIDReaderException(RFIDErrorCodes.NOR, "Number out of range ([1,2])");
    }
    String[] responses = checkData(communicateSynchronized("AT+IN?"));
    return responses[pin - 1].contains("HIGH");
  }

  /**
   * Sets the state of an output pin
   * 
   * @param pin output pin
   * @param state true for high state, false for low state
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void setOutput(int pin, boolean state) throws RFIDReaderException, CommConnectionException {
    if (1 > pin || pin > 4) {
      throw new RFIDReaderException(RFIDErrorCodes.NOR, "Number out of range ([1,4])");
    }
    String command = "AT+OUT=";
    for (int i = 1; i < 5; i++) {
      command += i != pin ? "," : state ? "1" : "0";
    }
    communicateSynchronized(command);
  }

  /**
   * Gets the current output pin state
   * 
   * @param pin output pin
   * @return the current output pin state
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range,
   */
  public boolean getOutput(int pin) throws RFIDReaderException, CommConnectionException {
    if (1 > pin || pin > 4) {
      throw new RFIDReaderException(RFIDErrorCodes.NOR, "Number out of range ([1,4])");
    }
    String[] responses = checkData(communicateSynchronized("AT+OUT?"));
    return responses[pin - 1].contains("HIGH");
  }

  /**
   * test if the reader is alive
   * 
   * @throws CommConnectionException possible ICommConnection Errorcodes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  @SuppressWarnings("PMD.EmptyCatchBlock")
  public void ping() throws CommConnectionException {
    try {
      getRevision();
    } catch (RFIDReaderException e) {
      // rfid exception but reader is alive
    }
  }

  /**
   * 
   * @param response the reader response
   * @param callInfo call information
   * @throws RFIDReaderException possible error codes (define in class {@link RFIDErrorCodes}):
   *         <ul>
   *         <li>UPA - Wrong parameter</li>
   *         <li>NOR - Number out of Range</li>
   *         <li>NER - not expected response</li>
   *         </ul>
   */
  protected void handleUnexpectedResponse(String response, String callInfo) throws RFIDReaderException {
    if (response.equals("UPA"))
      throw new RFIDReaderException(RFIDErrorCodes.UPA, callInfo + " - Wrong parameter");
    if (response.equals("NOR"))
      throw new RFIDReaderException(RFIDErrorCodes.NOR, callInfo + " - Number out of Range");
    if (response.equals("EDX"))
      throw new RFIDReaderException(RFIDErrorCodes.EDX, callInfo + " - Error decimal expected");
    if (response.equals("EHX"))
      throw new RFIDReaderException(RFIDErrorCodes.EHX, callInfo + " - Error hex expected");
    if (response.equals("WDL"))
      throw new RFIDReaderException(RFIDErrorCodes.WDL, callInfo + " - Wrong datalength");
    if (response.equals("NSS"))
      throw new RFIDReaderException(RFIDErrorCodes.NSS, callInfo + " - No standard selected");
    if (response.equals("NRF"))
      throw new RFIDReaderException(RFIDErrorCodes.NRF, callInfo + " - no RF-Field active");
    if (response.equals("NOS"))
      throw new RFIDReaderException(RFIDErrorCodes.NOS, callInfo + " - not supported");
    if (response.equals("ARH"))
      throw new RFIDReaderException(RFIDErrorCodes.ARH, callInfo + " - Antenna Reflectivity High");
    if (response.startsWith("HBE"))
      throw new RFIDReaderException(RFIDErrorCodes.HBE_XX, callInfo + " - " + response);
    throw new RFIDReaderException(RFIDErrorCodes.NER, callInfo + " - not expected response: " + response);
  }

  /**
   * gets a byte array from a hex string
   * 
   * @param str hex string
   * @return byte array
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, data are not hex data</li>
   *         <li>WDL, hex data have a wrong lenght</li>
   *         </ul>
   */
  protected byte[] getByteFromHexString(String str) throws RFIDReaderException {
    if (1 == str.length() % 2)
      throw new RFIDReaderException(RFIDErrorCodes.WDL, "Wrong hex data Length");
    if (!str.matches("\\p{XDigit}*"))
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "No Hex String");
    byte[] data = new byte[str.length() / 2];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
    }
    return data;
  }

  /**
   * @return the currentAntennaPort
   */
  protected int getCurrentAntennaPort() {
    return currentAntennaPort;
  }

  /**
   * @param currentAntennaPort the currentAntennaPort to set
   */
  protected void setCurrentAntennaPort(int currentAntennaPort) {
    this.currentAntennaPort = currentAntennaPort;
  }

  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+ANT", port);
    setCurrentAntennaPort(port);
    useSingleAntenna = true;
  }

  /**
   * @return antenna port
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public int getAntennaPort() throws CommConnectionException, RFIDReaderException {
    // +ANT: 1
    return Integer.parseInt(communicateSynchronized("AT+ANT?").substring(6));
  }

  @Override
  public void setMultiplexAntennas(int antennas) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+MUX", antennas);
    useSingleAntenna = false;
  }

  /**
   * Returns the number of antennas to be switched through (from 1 to ...)
   * 
   * @return Number of antennas to be switched through
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public int getMultiplexAntennas() throws CommConnectionException, RFIDReaderException {
    // +MUX: 1
    return Integer.parseInt(communicateSynchronized("AT+MUX?").substring(6));
  }

  @Override
  public void startInventory(long tagLostTime) throws CommConnectionException, RFIDReaderException {
    if (null == getInternalInventory()) {
      throw new RFIDReaderException(RFIDErrorCodes.TLM, "tag listener missing");
    }
    getInternalInventory().setKeepTime(tagLostTime);
    getInternalInventory().clear();
    getInternalInventory().start();
    communicateSynchronized(useSingleAntenna ? "AT+CINV" : "AT+CMINV");
  }

  @Override
  public List<T> stopInventory() throws CommConnectionException, RFIDReaderException {
    try {
      communicateSynchronized("AT+BINV");
    } catch (RFIDReaderException e) {
      if (!e.getMessage().contains("is not running")) {
        throw e;
      }
    }
    getInternalInventory().stop();
    return getInternalInventory().getInventory();
  }

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @param tagLostTime timeout in milliseconds for tag lost event
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void startInventoryReport(long tagLostTime) throws CommConnectionException, RFIDReaderException {
    if (null == getInternalInventory()) {
      throw new RFIDReaderException(RFIDErrorCodes.TLM, "tag listener missing");
    }
    getInternalInventory().setKeepTime(tagLostTime);
    getInternalInventory().clear();
    getInternalInventory().start();
    communicateSynchronized("AT+CINVR");
  }

  /**
   * Stops the current continues inventory
   * 
   * @return the current inventory
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs
   */
  public List<T> stopInventoryReport() throws CommConnectionException, RFIDReaderException {
    try {
      communicateSynchronized("AT+BINVR");
    } catch (RFIDReaderException e) {
      if (!e.getMessage().contains("is not running")) {
        throw e;
      }
    }
    getInternalInventory().stop();
    return getInternalInventory().getInventory();
  }
}


