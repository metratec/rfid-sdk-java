/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.tag.RfidTag;

/**
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * @param <T> rfid tag instance
 * 
 */
public abstract class MetratecReaderGen1<T extends RfidTag> extends MetratecReader<T> {

  

  private boolean wasCRC = false;
  private boolean wasEOF = false;
  private boolean isCRC = false;

  private int currentAntennaPort;

  
  // private InventoryEventHandler inventoryHandler = new InventoryEventHandler(getIdentifier());

  private Pattern splitPattern = Pattern.compile("\r");



  /**  */
  protected static final int RESPONSE_ERROR_LENGTH = 3;
  /** not supported standard */
  protected static final String RESPONSE_ERROR_NSS = "NSS";
  /** Response ok tag communication error */
  protected static final String RESPONSE_ERROR_TCE = "TCE";
  /** Response crc error */
  protected static final String RESPONSE_ERROR_CER = "CER";
  /** Response tag out of range */
  protected static final String RESPONSE_ERROR_TOR = "TOR";
  /** Response time out error */
  protected static final String RESPONSE_ERROR_TOE = "TOE";
  /** Response ok */
  protected static final String RESPONSE_OK = "OK!";
  /** Number out of range */
  protected static final String RESPONSE_ERROR_NOR = "NOR";
  /** No rf field active */
  protected static final String RESPONSE_ERROR_NRF = "NRF";

  /**
   * Construct a new StandardReader instance with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection connection
   */
  public MetratecReaderGen1(String identifier, ICommConnection connection) {
    super(identifier, connection);
    
  }

  /**
   * @param inventory the inventory string from the reader
   */
  protected abstract void handleInventory(String inventory);

  /**
   * Sends an command to the connected reader (appends crc and command end sign automatically)
   * 
   * @param command command
   * @param parameters command parameters
   * @return the reader answer as a string array
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  protected String[] communicateSynchronized(String command, Object... parameters)
      throws CommConnectionException, RFIDReaderException {
    if (receiveHandler.isConnected() && receiveHandler.isAlive()) {
      communicateLock.lock();
      try {
        clearResponseBuffer();
        receiveHandler.sendCommand(prepareCommand(command, parameters));
        return checkData(receiveData());
      } finally {
        communicateLock.unlock();
      }
    } else {
      throw new CommConnectionException(ICommConnection.CONNECTION_LOST, "not connected");
    }
  }

  /**
   * Reset the reader
   * 
   * @throws RFIDReaderException if a error occurs, see {@link RFIDReaderException#getErrorCode()}
   *         and {@link RFIDReaderException#getMessage()} for more details
   * @throws CommConnectionException if a error occurs, see
   *         {@link CommConnectionException#getErrorCode()} and
   *         {@link CommConnectionException#getMessage()} for more details
   */
  public void reset() throws RFIDReaderException, CommConnectionException {
    prepareStandardReader();
    setEndOfFrame(false);
    String[] receive = communicateSynchronized("RST");
    if (!receive[0].startsWith("OK!")) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, receive[0]);
    }
    try {
      TimeUnit.MILLISECONDS.sleep(200);
    } catch (InterruptedException e) {
    }
    prepareDevice();
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    prepareStandardReader();
    return "connected";
  }

  /**
   * prepare the reader communication
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  private void prepareStandardReader() throws CommConnectionException, RFIDReaderException {
    receiveHandler.setEOF(false);
    configureConnection();
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
    sendCommand.setLength(0);
    sendCommand.append(command);
    for (Object parameter : parameters) {
      if (null == parameter)
        continue;
      String value = String.valueOf(parameter);
      if (value.isEmpty())
        continue;
      sendCommand.append(' ');
      sendCommand.append(value);
    }
    if (isCRC) {
      sendCommand.append(' ');
      sendCommand.append(getCRC(sendCommand.toString()));
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
  protected String[] checkData(String string) throws RFIDReaderException {
    String data[];
    // if (receiveHandler.isEOF()) {
    // data = splitPattern.split(string.substring(0, string.length() - 1), 0);
    // } else {
    // data = splitPattern.split(string, 0);
    // }
    data = splitPattern.split(string, 0);
    if (0 == data.length)
      data = new String[] {""};
    if (isCRC) {
      if (data[0].startsWith("CCE"))
        throw new RFIDReaderException(RFIDErrorCodes.CCE, "CRC Error");
      String tmpCrc;
      String tmpData;
      for (int i = data.length; --i >= 0;) {
        try {
          tmpCrc = data[i].substring(data[i].length() - 4, data[i].length());
          tmpData = data[i].substring(0, data[i].length() - 4);
          if (tmpCrc.equals(getCRC(tmpData))) {
            data[i] = tmpData.substring(0, tmpData.length() - 1);
          } else {
            throw new RFIDReaderException(RFIDErrorCodes.CCE, "CRC Error - expected: \"" + tmpData
                + getCRC(tmpData) + "\"  receive: \"" + data[i]);
          }
        } catch (IndexOutOfBoundsException e) {
          throw new RFIDReaderException(RFIDErrorCodes.CCE, "CRC not active! (" + data[i] + ")");
        }
      }
      return data;
    } else {
      return data;
    }
  }

  @Override
  protected boolean handleResponse(String response) {
    switch (response.charAt(0)) {
      // case '\r':
      // return false;
      case '\n':
        if (response.substring(1, 3).equals("HBT")) {
          return false;
        }
        break;
      case 'H':
        if (response.startsWith("HBT")) {
          return false;
        }
        break;
      case 'I':
        boolean state = response.contains("HI!");
        if (response.startsWith("IN0")) {
          // if (input0 != state) {
          if (getInputDebounceTime() == 0) {
            setInput0(state);
            RfidReaderInputChange event =
                new RfidReaderInputChange(getIdentifier(), System.currentTimeMillis(), 0, isInput0());
            getEventHandler().inputChange(event);
          } else {
            startNewInputThread(0);
          }
          // }
          return false;
        }
        if (response.startsWith("IN1")) {
          // if (input1 != state) {
          if (getInputDebounceTime() == 0) {
            setInput1(state);
            RfidReaderInputChange event =
                new RfidReaderInputChange(getIdentifier(), System.currentTimeMillis(), 1, isInput1());
            getEventHandler().inputChange(event);
          } else {
            startNewInputThread(1);
          }
          // }
          return false;
        }
        if (response.startsWith("IVF")) {
          handleInventory(response);
          return false;
        }
        break;
      case 'S':
        if(response.startsWith("SRT")){
          getLogger().info("Soft reset from reader received - reinitialize the reader");
          Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
              try {
                reset();
              } catch (Exception e) {
                getLogger().warn("Error during reinitialize the reader - {}", e.toString());
              }
            }
            
          },getIdentifier()+"-reset");
          t.setDaemon(true);
          t.start();
        }
        return true;
    }
    // check if the message is an inventory
    if (response.length() >= 14 && response.substring(response.length() - 14).contains("IVF")) {
      handleInventory(response);
      return false;
    }
    return true;
  }



  // @Override
  // protected RFIDResponseChecker getResponseChecker() {
  // return new RFIDResponseChecker() {
  // @Override
  // public boolean isResponseInteresting(String response) {
  // //check first char of message
  // switch (response.charAt(0)) {
  // // case '\r':
  // // return false;
  // case '\n':
  // if (response.substring(1, 3).equals("HBT")) {
  // return false;
  // }
  // break;
  // case 'H':
  // if (response.startsWith("HBT")) {
  // return false;
  // }
  // break;
  // case 'I':
  // boolean state = response.contains("HI!");
  // if (response.startsWith("IN0")) {
  // // if (input0 != state) {
  // if (inputDebounceTime == 0) {
  // input0 = state;
  // RfidReaderInputChange event = new RfidReaderInputChange(getIdentifier(),
  // System.currentTimeMillis(), 0, input0);
  // eventHandler.inputChange(event);
  // } else {
  // startNewInputThread(0);
  // }
  // // }
  // return false;
  // }
  // if (response.startsWith("IN1")) {
  // // if (input1 != state) {
  // if (inputDebounceTime == 0) {
  // input1 = state;
  // RfidReaderInputChange event = new RfidReaderInputChange(getIdentifier(),
  // System.currentTimeMillis(), 1, input1);
  // eventHandler.inputChange(event);
  // } else {
  // startNewInputThread(1);
  // }
  // // }
  // return false;
  // }
  // if (response.startsWith("IVF")) {
  // handleInventory(response);
  // return false;
  // }
  // break;
  // }
  // //check if the message is an inventory
  // if (response.length() >= 14 && response.substring(response.length() - 14).contains("IVF")) {
  // handleInventory(response);
  // return false;
  // }
  // return true;
  // }
  // };
  // }


  /**
   * add a new rfid tag event to the event handler
   * 
   * @param tagEvent the event
   */
  protected void addNewInventoryEvent(List<T> tags) {
    getInternalInventory().updateInventory(tags);
  }

  /**
   * check the current reader state (standby, continues read, crc, ..) and prepare the communication
   * 
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if error occurs (timeout)
   */
  private void configureConnection() throws CommConnectionException, RFIDReaderException {
    wasCRC = false;
    wasEOF = false;
    isCRC = false;
    receiveHandler.setEOF(false);
    int configuredReceiveTimeout = getReceiveTimeout();
    setReceiveTimeout(3000); // Timeout for configure the connection
    // check CNR MODE and CRC MODE
    try {
      communicateLock.lock();
      clearResponseBuffer();
      send("BRK");
      String receive = "";
      boolean next = true;
      boolean checkSleeping = false;
      while (next) {
        try {
          receive = receiveRaw();
          // System.out.println("Receive: "+receive);
          if (receive.contains("BRA")) {
            next = false;
          } else if (receive.contains("NCM")) {
            next = false;
          } else if (receive.contains("CCE")) {
            isCRC = true;
            wasCRC = true;
            if (checkSleeping)
              send("WAK");
            // send("WAK 5E70");
            else
              send("BRK");
            // send("BRK 9977");
          } else if (receive.contains("GMO")) {
            next = false;
          } else if (receive.contains("DNS")) {
            next = false;
          } else if (receive.contains("UCO")) {
            throw new CommConnectionException(ICommConnection.NO_DEVICES_FOUND,
                "device is not a metraTec rfid reader");
          }
        } catch (CommConnectionException e) {
          if (e.getErrorCode() == ICommConnection.RECV_TIMEOUT) {
            // reader sleeping?
            if (!checkSleeping) {
              checkSleeping = true;
              send("WAK");
            } else {
              throw e;
            }
          } else {
            throw e;
          }
        }
      }
      // data available don't work with usb in the moment...so try to receive the line feed sign...
      receiveHandler.setEOF(true);
      send("EOF");
      String rec = receiveRaw();
      while (rec.contains("HBT")) {
        rec = receiveRaw();
      }
      if (!rec.contains(RESPONSE_OK)) {
        handleUnexpectedResponse(rec, "Enable end of frame");
      }
    } finally {
      communicateLock.unlock();
      // reset the receive timeout
      setReceiveTimeout(configuredReceiveTimeout);
    }
    // set crc and eof
    setEndOfFrame(true);
    setCRC(true);
    setHeartbeatInterval(this.heartBeatInterval);
    try {
      setInput0(getInput(0));
      setInput1(getInput(1));
    } catch (RFIDReaderException e) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug(getIdentifier() + " configure getInput - " + e.getLocalizedMessage());
      }
    }
  }

  @Override
  public void stop() throws CommConnectionException {
    if (isConnected()) {
      try {
        stopInventory();
      } catch (Exception e) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug(getIdentifier() + " error stop scanning " + e.getLocalizedMessage());
        }
      }
      try {
        followUp();
      } catch (Exception e) {
        if (getLogger().isDebugEnabled()) {
          getLogger()
              .debug(getIdentifier() + " error reset configuration " + e.getLocalizedMessage());
        }
      }
    }
    super.stop();
  }

  /**
   * Follow up the reader, set eof and crc mode back
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  protected void followUp() throws RFIDReaderException, CommConnectionException {
    setCRC(wasCRC);
    setEndOfFrame(wasEOF);
  }

  /**
   * Sets the reader in a power saving mode
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void standby() throws RFIDReaderException, CommConnectionException {
    String[] receiveData = communicateSynchronized("STB");
    if (receiveData[0].equals("GN8")) {
      receiveHandler.setReaderStandby(true);
      return;
    }
    throw new RFIDReaderException(RFIDErrorCodes.NER, receiveData[0]);
  }

  /**
   * Ends the power saving mode
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void wakeUp() throws RFIDReaderException, CommConnectionException {
    String[] receiveData = communicateSynchronized("WAK");
    if (receiveData[0].equals("GMO")) {
      receiveHandler.setReaderStandby(false);
      return;
    }
    if (receiveData[0].equals("DNS")) {
      return;
    }
    throw new RFIDReaderException(RFIDErrorCodes.NER, receiveData[0]);
  }



  /**
   * Gets the firmware revision
   * 
   * @return the device type, hardware architecture and firmware revision of the reader
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public String getRevision() throws RFIDReaderException, CommConnectionException {
    String[] receiveData = communicateSynchronized("REV");
    if (RESPONSE_ERROR_LENGTH < receiveData[0].length())
      return receiveData[0];
    throw new RFIDReaderException(RFIDErrorCodes.NER, receiveData[0]);
  }

  @Override
  public ReaderType getReaderType() throws RFIDReaderException, CommConnectionException {
    String revision = getRevision();
    String firmwareName;
    int firmwareRevision;
    try {
      firmwareRevision = Integer.parseInt(revision.substring(revision.length() - 4));
    } catch (NumberFormatException e) {
      String fRevision = revision.substring(revision.length() - 4);
      if (fRevision.charAt(0) == 'c' || fRevision.charAt(0) == 'b') {
        // Release Candidate or Beta Version
        try {
          firmwareRevision = Integer.parseInt(fRevision.substring(1));
        } catch (NumberFormatException e1) {
          throw new RFIDReaderException(RFIDErrorCodes.NER,
              "unexpected revision response: " + revision);
        }
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER,
            "unexpected revision response: " + revision);
      }
    }
    if (23 == revision.length() || 24 == revision.length()) {
      // description (15-16 Byte name, 4 byte Hardware Requirement, 4 Byte Firmware Revision)
      firmwareName = revision.substring(0, revision.length() - 8);
    } else {
      // new description (16 Byte name, 4 Byte Firmware Revision)
      firmwareName = revision.substring(0, revision.length() - 4);
    }
    firmwareName = firmwareName.trim().toLowerCase();
    for (ReaderType typ : ReaderType.values()) {
      if (typ.name().toLowerCase().startsWith(firmwareName)) {
        typ.setFirmwareRevision(firmwareRevision);
        return typ;
      }
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
        throw new RFIDReaderException(RFIDErrorCodes.WFR,
            "Reader " + readerType.getFirmwareRevision() + " found, but min Revision 0" + minVersion
                + " needed. Please Update the Reader Firmware.");
      }
    }
    throw new RFIDReaderException(RFIDErrorCodes.WRT, "Reader " + allowedReaderVersions.get(0)
        + " expected but " + readerType.getFirmwareName() + " connected");
  }

  @Override
  public String getFirmwareRevision() throws RFIDReaderException, CommConnectionException {
    String revision = getRevision();
    return revision.substring(revision.length() - 4);
  }

  @Override
  public String getSerialNumber() throws RFIDReaderException, CommConnectionException {
    String response[] = communicateSynchronized("RSN");
    if (RESPONSE_ERROR_LENGTH < response[0].length())
      return response[0];
    handleUnexpectedResponse(response[0], "Get serial number");
    return null; // <-- Can not happen! Eclipse Java parser does not recognize this
  }

  @Override
  public String getHardwareRevision() throws RFIDReaderException, CommConnectionException {
    String[] response = communicateSynchronized("RHR");
    if (RESPONSE_ERROR_LENGTH < response[0].length())
      return response[0];
    handleUnexpectedResponse(response[0], "Get hardware revision");
    return null; // <-- Can not happen! Eclipse Java parser does not recognize this
  }

  /**
   * Sets the verbosity level of the reader The different levels are:
   * <ul>
   * <li>0: Only necessary data (EPC, User Data, etc.) is returned. Tag errors are suppressed.
   * Inventory only returns an answer if any tag was found. The number of tags found is not send.
   * Hardware triggered errors ( BOF CCE CRT PLE SRT UER URE ) and parsing errors ( DNS EDX EHF EHX
   * NCM NOR NOS NRF NSS UCO UPA WDL ) are send.</li>
   * <li>1: Default, most tag communication errors added. For multiple profiles active for every tag
   * only one error occurs.</li>
   * <li>2: All tag communication errors including RXE and CRE normally indicating a collision are
   * send. Errors on every tag are shown as often as they occur.</li>
   * </ul>
   * 
   * @param level level (0-2)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  protected void setVerbosityLevel(int level) throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("VBL", level);
    if (response[0].startsWith(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set verbosity level to " + level);
  }

  /**
   * <b>For internal use!</b><br>
   * <b>Be careful, the parser for the getInventory and getTagData methods needs an active End Of
   * Frame!</b><br>
   * This command turns on the End of Frame Delimiter (EOF). This means that after every complete
   * message (frame) the last CR will be followed by an additional line feed (LF, 0x0A). This allows
   * the user to build simpler parsers since it is clear when no to expect any further message from
   * the reader. The EOF returns on the end of any Instruction indifferent to actions done or answer
   * and on any CNR mode answer. CNR INV itself gives no EOF answer of its own. It comes with the
   * first Inventory.
   * 
   * @param state true for enable and false for disable the end of frame mode
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  protected void setEndOfFrame(boolean state) throws CommConnectionException, RFIDReaderException {
    if (state) {
      receiveHandler.setEOF(true);
      try {
        String[] answer = communicateSynchronized("EOF");
        if (answer[0].equals(RESPONSE_OK)) {
          return;
        }
        receiveHandler.setEOF(false);
        handleUnexpectedResponse(answer[0], "Enable end of frame");
      } catch (CommConnectionException e) {
        if (e.getErrorCode() == ICommConnection.RECV_TIMEOUT) {
          receiveHandler.setEOF(false);
        }
        throw e;
      }
    } else {
      receiveHandler.setEOF(false);
      String answer[] = communicateSynchronized("NEF");
      if (answer[0].equals(RESPONSE_OK)) {
        return;
      }
      handleUnexpectedResponse(answer[0], "Disable end of frame");
    }
  }

  /**
   * End of Frame State
   * 
   * @return end of frame ('\n') is active state
   */
  public boolean getEndOfFrameState() {
    return receiveHandler.isEOF();
  }

  /**
   * Enables or disables the Cyclic Redundancy Check (CRC) of the computer-to-reader communication.
   * 
   * @param state true for enable and false for disable the CRC Mode
   ** @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  protected void setCRC(boolean state) throws RFIDReaderException, CommConnectionException {
    if (state) {
      isCRC = false;
      // send("COF");
      communicateSynchronized("COF 4F5E");
      try {
        communicateLock.lock();
        send("CON");
        isCRC = true;
        String response[] = receive();
        if (response[0].startsWith(RESPONSE_OK))
          return;
        handleUnexpectedResponse(response[0], "Enable CRC");
      } finally {
        communicateLock.unlock();
      }
    } else {
      isCRC = false;
      String[] response = communicateSynchronized("COF 4F5E");
      if (response[0].startsWith(RESPONSE_OK))
        return;
      handleUnexpectedResponse(response[0], "Disable CRC");
    }
  }

  @Override
  protected void setHeartbeatInterval(int interval) throws RFIDReaderException, CommConnectionException {
    if (interval > 0) {
      heartBeatInterval = Math.min(interval, 300);
      String response[] = communicateSynchronized("HBT " + heartBeatInterval);
      if (response[0].startsWith(RESPONSE_OK)) {
        receiveHandler.setHeartBeatInterval(heartBeatInterval);
        return;
      } else {
        heartBeatInterval = -1;
        receiveHandler.setHeartBeatInterval(heartBeatInterval);
        if (response[0].startsWith("UCO")) {
          return;
        } else {
          getLogger().warn("Reader did not respond accurately");
          handleUnexpectedResponse(response[0], "Enable HBT");
        }
      }
    } else {
      String response[] = communicateSynchronized("HBT OFF");
      if (response[0].startsWith(RESPONSE_OK) || response[0].startsWith("UCO")) {
        return;
      }
      handleUnexpectedResponse(response[0], "Disable HBT");
    }
  }

  /**
   * Gets the state of the Cyclic Redundancy Check (CRC)
   * 
   * @return the CRC State
   */
  public boolean getCRCState() {
    return isCRC;
  }

  @Override
  public boolean getInput(int pin) throws RFIDReaderException, CommConnectionException {
    String[] receiveData = communicateSynchronized(String.format("RIP %02X", pin));
    if (receiveData[0].startsWith("HI"))
      return true;
    if (receiveData[0].equals("LOW"))
      return false;
    handleUnexpectedResponse(receiveData[0], "Get Input " + pin);
    return false; // <-- Can not happen! Eclipse Java parser does not recognize this
  }

  @Override
  public void setOutput(int pin, boolean state)
      throws RFIDReaderException, CommConnectionException {
    String[] receiveData =
        communicateSynchronized(String.format("WOP %02X %s", pin, state ? "HI" : "LOW"));
    if (receiveData[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(receiveData[0], "Set Output " + pin + " " + (state ? "HI" : "LOW"));
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
  public void ping() throws CommConnectionException {
    try {
      String[] receiveData = communicateSynchronized("BRK");
      if (receiveData[0].equals("NCM"))
        return; // OK
      if (receiveData[0].equals("BKA"))
        return;// OK Mifare
    } catch (RFIDReaderException e) {
      // CRC Error...but alive ;)
      if (getLogger().isDebugEnabled()) {
        getLogger().debug(getIdentifier() + " crc error " + e.getLocalizedMessage());
      }
    }
    // if here...not expected response...but alive ;)
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
  protected void handleUnexpectedResponse(String response, String callInfo)
      throws RFIDReaderException {
    if (response.equals("UPA"))
      throw new RFIDReaderException(RFIDErrorCodes.UPA, callInfo + " - Wrong parameter");
    if (response.equals("NOR"))
      throw new RFIDReaderException(RFIDErrorCodes.NOR, callInfo + " - Number out of Range");
    if (response.equals("EDX"))
      throw new RFIDReaderException(RFIDErrorCodes.EDX, callInfo + " - Error decimal expected");
    if (response.equals("EHX"))
      throw new RFIDReaderException(RFIDErrorCodes.EHX, callInfo + " - Error hex expected");
    if (response.equals("WDL"))
      throw new RFIDReaderException(RFIDErrorCodes.WDL, callInfo + " - Wrong data length");
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
    throw new RFIDReaderException(RFIDErrorCodes.NER,
        callInfo + " - not expected response: " + response);
  }

  /**
   * Calculate the CRC for the specific data
   * 
   * @param data data
   * @return crc
   */
  private String getCRC(String data) {
    int crc16;
    int Byte_Counter, Bit_Counter;
    byte[] dataTmp = data.getBytes(Charset.defaultCharset());

    crc16 = 0xffff;
    int polynomial = 0x8408;

    for (Byte_Counter = 0; Byte_Counter < dataTmp.length; Byte_Counter++) {
      crc16 ^= dataTmp[Byte_Counter] & 0x00FF;
      for (Bit_Counter = 0; Bit_Counter < 8; Bit_Counter++) {
        if (0 == (crc16 & 1))
          crc16 >>= 1;
        else
          crc16 = (crc16 >> 1) ^ polynomial;
      }
    }
    return String.format("%04X", crc16);
  }

  /**
   * prepare the input for event handling
   * 
   * @throws CommConnectionException if an error occurs
   * @throws RFIDReaderException if an error occurs
   */
  protected void prepareInputsForEventHandling()
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SEC COMM 0 #SPIN0#RIP 0");
    if (!response[0].equals(RESPONSE_OK))
      handleUnexpectedResponse(response[0], "Setting PreCommand Input0");
    response = communicateSynchronized("SEC EDGE 0 BOTH");
    if (!response[0].equals(RESPONSE_OK))
      handleUnexpectedResponse(response[0], "Setting Edge Input0");
    response = communicateSynchronized("SEC COMM 1 #SPIN1#RIP 1");
    if (!response[0].equals(RESPONSE_OK))
      handleUnexpectedResponse(response[0], "Setting PreCommand Input1");
    response = communicateSynchronized("SEC EDGE 1 BOTH");
    if (!response[0].equals(RESPONSE_OK))
      handleUnexpectedResponse(response[0], "Setting Edge Input1");
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

  /**
   * @param power reader power value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public abstract void setPower(int power) throws CommConnectionException, RFIDReaderException;

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public abstract void scanInventory() throws CommConnectionException, RFIDReaderException;
  
  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @param tagKeepTime tag keep time
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void scanInventory(long tagKeepTime) throws CommConnectionException, RFIDReaderException{
    getInternalInventory().setKeepTime(tagKeepTime);
    getInternalInventory().clear();
    getInternalInventory().start();
    scanInventory();
  }

  @Override
  public void startInventory(long tagLostTime) throws CommConnectionException, RFIDReaderException {
    scanInventory(tagLostTime);
  }

  @Override
  public List<T> stopInventory() throws CommConnectionException, RFIDReaderException {
    String[] receiveData = communicateSynchronized("BRK");
    if (receiveData[0].equals("BRA") || receiveData[0].equals("NCM")) {
      getInternalInventory().stop();
      return getInternalInventory().getInventory();
    }
    throw new RFIDReaderException(RFIDErrorCodes.NER, receiveData[0]);

  }
}


