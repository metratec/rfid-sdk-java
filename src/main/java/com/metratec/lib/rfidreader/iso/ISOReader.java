/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.iso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.ReaderType;
import com.metratec.lib.rfidreader.MetratecReaderGen1;
import com.metratec.lib.tag.HFTagInformation;
import com.metratec.lib.tag.HfTag;

/**
 * Class for the metraTec iso reader
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public class ISOReader extends MetratecReaderGen1<HfTag> {
  private final Logger logger = LoggerFactory.getLogger(ISOReader.class);
  private MODE mode;
  private SRI sri;
  private int afi = 0;
  private static final int RETRY_COUNT = 5;
  private String[] minReaderRevision;

  private List<HfTag> lastInventory = null;
  private RFIDReaderException lastException = null;
  private boolean scanningForTags = false;

  /** enum for the reader mode */
  public enum MODE {
    /** ISO 15693 */
    ISO15693,
  }

  /** enum for the modulation depth and subcarrier mode of the reader */
  public enum SRI {
    /** Single Subcarrier, 10% ASK */
    SingleSubcarrier_10percentASK,
    /** Single Subcarrier, 100% ASK */
    SingleSubcarrier_100percentASK,
    /** Double Subcarrier, 100% ASK */
    DoubleSubcarrier_100percentASK
  }

  /**
   * Creates a new MifareReader with the specified connection, mode and sri
   * 
   * @param identifier reader identifier
   * @param connection connection
   * @param minReaderRevision minimal reader revision
   */
  protected ISOReader(String identifier, ICommConnection connection, String... minReaderRevision) {
    this(identifier, connection, MODE.ISO15693, SRI.SingleSubcarrier_100percentASK, minReaderRevision);
  }

  /**
   * Creates a new MifareReader with the specified connection, mode and sri
   * 
   * @param identifier reader identifier
   * @param connection connection
   * @param mode mode
   * @param sri sri
   * @param minReaderRevision minimal reader revision
   */
  protected ISOReader(String identifier, ICommConnection connection, MODE mode, SRI sri, String... minReaderRevision) {
    super(identifier, connection);
    this.mode = null != mode ? mode : MODE.ISO15693;
    this.sri = null != sri ? sri : SRI.SingleSubcarrier_100percentASK;
    this.minReaderRevision = minReaderRevision;
  }

  /**
   * Use the reader classes ( {@link QuasarMX}, {@link QuasarLR}, {@link DeskID_ISO}, {@link Dwarf15}, {@link QR15}) for
   * instantiate the reader<br>
   * Creates a new ISOReader class for communicate with the specified metraTec usb reader
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb hf reader
   * @param mode mode
   * @param sri sri
   */
  @Deprecated
  public ISOReader(String identifier, String usbDeviceSerialNumber, MODE mode, SRI sri) {
    this(identifier, new UsbConnection(usbDeviceSerialNumber, 115200), mode, sri);
  }

  /**
   * Use the reader classes ( {@link QuasarMX}, {@link QuasarLR}, {@link DeskID_ISO}, {@link Dwarf15}, {@link QR15}) for
   * instantiate the reader<br>
   * Creates a new ISOReader class for communicate with the specified metraTec ethernet reader
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet hf reader
   * @param port port of the ethernet hf reader
   * @param mode mode
   * @param sri sri
   */
  @Deprecated
  public ISOReader(String identifier, String ipAddress, int port, MODE mode, SRI sri) {
    this(identifier, new TcpConnection(ipAddress, port), mode, sri);
  }

  /**
   * Use the reader classes ( {@link QuasarMX}, {@link QuasarLR}, {@link DeskID_ISO}, {@link Dwarf15}, {@link QR15}) for
   * instantiate the reader<br>
   * Creates a new ISOReader class for communicate with the specified metraTec rs232 reader
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 hf reader is connected
   * @param baudrate baudrate of the rs232 hf reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   * @param mode mode
   * @param sri sri
   */
  @Deprecated
  public ISOReader(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl, MODE mode, SRI sri) {
    this(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl), mode, sri);
  }

  private void prepareISOReader() throws CommConnectionException, RFIDReaderException {
    scanningForTags = false;
    ReaderType type = getReaderType();
    if (null != minReaderRevision && minReaderRevision.length > 0) {
      checkReaderType(type, minReaderRevision);
    } else {
      checkReaderType(type, QuasarMX.MIN_READER_REVISION, QuasarMX.MIN_READER_REVISION_OLD,
          QuasarLR.MIN_READER_REVISION, DeskID_ISO.MIN_READER_REVISION, DeskID_ISO.MIN_READER_REVISION_OLD,
          Dwarf15.MIN_READER_REVISION, Dwarf15.MIN_READER_REVISION_OLD, QR15.MIN_READER_REVISION,
          QR15.MIN_READER_REVISION_OLD);
    }

    setVerbosityLevel(2); // default
    setMode(mode);
    setSRI(sri);
    setEndOfFrame(true);
  }

  /**
   * Use {@link ISOReader#stop()} for close the connection
   * 
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  @Deprecated
  public void close() throws CommConnectionException {
    stop();
  }

  /**
   * looks for all tags in range of the reader and get the Tag IDs of all tags as a number of strings back
   * 
   * @param ssl true for single slot
   * @param ont if true only new tags in the field are returned
   * @return List with the Tag IDs of founded tags
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
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
  private List<HfTag> getInventory(boolean ssl, boolean ont) throws CommConnectionException, RFIDReaderException {
    List<HfTag> tags;
    try {
      scanningForTags = true;
      send("INV", afi != 0 ? String.format("AFI %02X", afi) : null, ont ? "ONT" : null, ssl ? "SSL" : null);
      synchronized (getIdentifier()) {
        try {
          getIdentifier().wait(getReceiveTimeout());
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if (null != lastInventory) {
          tags = lastInventory;
        } else if (null != lastException) {
          throw lastException;
        } else {
          throw new CommConnectionException(ICommConnection.RECV_TIMEOUT, "the reader did not respond (INV)");
        }
      }
    } finally {
      scanningForTags = false;
    }
    return tags;
  }

  /**
   * looks for all tags in range of the reader and get the Tag IDs of all tags as a number of strings back
   * 
   * @return List with the Tag IDs of founded tags
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
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
  public List<HfTag> getInventory() throws RFIDReaderException, CommConnectionException {
    return getInventory(false, false);
  }

  /**
   * looks for all tags in range of the reader (used single slot) and get the Tag IDs of all tags as a number of strings
   * back
   * 
   * @return List with the Tag IDs of founded tags
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
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
  public List<HfTag> getInventorySingleSlot() throws RFIDReaderException, CommConnectionException {
    return getInventory(true, false);
  }

  /**
   * looks for all new tags in range of the reader and get the Tag IDs of all tags as a number of strings back
   * 
   * @return List with the Tag IDs of founded tags
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
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
  public List<HfTag> getInventoryUnique() throws RFIDReaderException, CommConnectionException {
    return getInventory(false, true);
  }

  /**
   * looks for all new tags in range of the reader (used single slot) and get the Tag IDs of all tags as a number of
   * strings back
   * 
   * @return List with the Tag IDs of founded tags
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
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
  public List<HfTag> getInventoryUniqueSingleSlot() throws RFIDReaderException, CommConnectionException {
    return getInventory(true, true);
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
  protected String sendRequest(String reqCommand) throws CommConnectionException, RFIDReaderException {
    String[] answers = communicateSynchronized(reqCommand);
    try {
      int index = answers.length - 1;
      if (answers[index].startsWith("NCL")) {
        index--;
        if (answers[index].startsWith("COK")) {
          index--;
          if (answers[index].startsWith("00")) {
            return answers[index].substring(2, answers[index].length() - 4);
          } else {
            throw new RFIDReaderException(RFIDErrorCodes.TEC, answers[index].substring(0, 4));
          }
        } else {
          throw new RFIDReaderException(RFIDErrorCodes.CER, "CRC error");
        }
      } else if (answers[index].startsWith("CDT")) {
        throw new RFIDReaderException(RFIDErrorCodes.CLD, "Collision detect");
      } else if (answers[index].startsWith("TNR")) {
        throw new RFIDReaderException(RFIDErrorCodes.TNR, answers[0]);
      } else if (answers[index].startsWith("RDL")) {
        throw new RFIDReaderException(RFIDErrorCodes.RDL, "The data requested from the tag is too long");
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER, answers[0]);
      }
    } catch (IndexOutOfBoundsException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "wrong data: " + Arrays.toString(answers));
    }
  }

  /**
   * get the data from the Tag with tagID which is stored in the block number
   * 
   * @param blockNumber block number
   * @param tagID tag ID (optional, if null the available tag is used)
   * @return returns the data which is stored in the block number
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>CLD, Collision detect</li>
   *         <li>CER, CRC error</li>
   *         <li>TEC, Tag Error code</li>
   *         <li>WPA, wrong Block number</li>
   *         <li>TNR, Tag not responding</li>
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
  public HfTag getTagData(int blockNumber, String tagID) throws CommConnectionException, RFIDReaderException {
    if (0 > blockNumber || blockNumber > 255) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "wrong Blocknumbers - 0<=blockNumber<256 ");
    }
    StringBuffer command = new StringBuffer();
    // prepare command
    command.append("REQ ");
    switch (sri) {
      case SingleSubcarrier_100percentASK:
      case SingleSubcarrier_10percentASK:
        if (null == tagID) {
          // not addressed mode
          command.append("0220");
        } else {
          // addressed mode
          command.append("2220");
          command.append(tagID);
        }
        break;
      case DoubleSubcarrier_100percentASK:
        if (null == tagID) {
          // not addressed mode
          command.append("0320");
        } else {
          // addressed mode
          command.append("2320");
          command.append(tagID);
        }
        break;
    }
    command.append(String.format("%02X crc", blockNumber));
    HfTag tag = null;
    try {
      String response = sendRequest(command.toString());
      tag = new HfTag(tagID);
      tag.setData(response);
      return tag;
    } catch (RFIDReaderException e) {
      tag = new HfTag(tagID);
      tag.setHasError(true);
      tag.setMessage(e.getLocalizedMessage());
    }
    return tag;

  }

  /**
   * Sends stay quiet command to the tag.
   *
   * @param tagID tag ID (required)
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>CLD, Collision detect</li>
   *         <li>CER, CRC error</li>
   *         <li>TEC, Tag Error code</li>
   *         <li>WPA, wrong Block number</li>
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
  public void setTagQuiet(String tagID) throws CommConnectionException, RFIDReaderException {
    StringBuilder command = new StringBuilder();
    // prepare command
    command.append("WRQ ");
    switch (sri) {
      case SingleSubcarrier_100percentASK:
      case SingleSubcarrier_10percentASK:
        // addressed mode
        command.append("2202");
        command.append(tagID);
        break;
      case DoubleSubcarrier_100percentASK:
        // addressed mode
        command.append("2302");
        command.append(tagID);
        break;
    }
    try {
      sendRequest(command.toString());
    } catch (RFIDReaderException ex) {
      // TNR is the expected response
      if (ex.getErrorCode() != RFIDErrorCodes.TNR) {
        throw ex;
      }
    }

  }

  /**
   * get the data which is stored in the block number
   * 
   * @param blockNumber block number
   * @return returns the data which is stored in the block, null if tag is not responding
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>CLD, Collision detect</li>
   *         <li>CER, CRC error</li>
   *         <li>TEC, Tag Error code</li>
   *         <li>WPA, wrong Block number</li>
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
  public HfTag getTagData(int blockNumber) throws RFIDReaderException, CommConnectionException {
    return getTagData(blockNumber, null);
  }

  /**
   * gets the data which is stored in the tag, get the data from firstblock and the following blocks. Returns the data
   * of readable blocks. If "following blocks" is bigger than the available blocks, only the available blocks are
   * returned. If the tag after reading the data is not available (TNR 5 times) the reading data will be return.
   * 
   * @param firstBlock first block to read
   * @param numberOfFollowingBlocks number of following block
   * @param tagID tag ID (optional, if null the available tag is used)
   * @return returns the data which is stored in the blocks
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>CLD, Collision detect</li>
   *         <li>CER, CRC error</li>
   *         <li>TEC, Tag Error code</li>
   *         <li>WPA, wrong Block number</li>
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
  public HfTag getTagData(int firstBlock, int numberOfFollowingBlocks, String tagID)
      throws RFIDReaderException, CommConnectionException {
    if (0 > firstBlock || firstBlock > 255 || (firstBlock + numberOfFollowingBlocks) > 255) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA,
          "wrong Block number\n0<=fistblock<256  (firstBlock+numberOfFollowingBlocks)<256");
    }
    StringBuffer tagData = new StringBuffer();
    int errorCount = RETRY_COUNT;
    HfTag tag = null;
    for (int i = firstBlock; i <= firstBlock + numberOfFollowingBlocks; i++) {
      HfTag response = getTagData(i, tagID);
      if (response.hasError()) {
        if (errorCount == 0) {
          return response;
        } else {
          i--;
          errorCount--;
        }
      } else {
        errorCount = RETRY_COUNT;
        tagData.append(response.getData());
        if(null == tag){
          tag = response;
        }
      }
    }
    tag.setData(tagData.toString());
    return tag;
  }

  /**
   * Writes Data to the tag
   * 
   * @param blockNumber blocknumber
   * @param data 4Byte data as Hex (8 signs)
   * @return the {@link HfTag}
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, Wrong Block number or wrong data length</li>
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
  public HfTag setTagData(int blockNumber, String data) throws RFIDReaderException, CommConnectionException {
    return setTagData(blockNumber, data, null, false);
  }

  /**
   * Writes Data to the tag
   * 
   * @param blockNumber blocknumber
   * @param data 4Byte data as Hex (8 signs)
   * @param optionFlag Meaning is defined by the tag command description.
   * @return the {@link HfTag}
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, Wrong Block number or wrong data length</li>
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
  public HfTag setTagData(int blockNumber, String data, boolean optionFlag)
      throws RFIDReaderException, CommConnectionException {
    return setTagData(blockNumber, data, null, optionFlag);
  }

  /**
   * Writes Data to the specified Tag
   * 
   * @param blockNumber blocknumber
   * @param data 4Byte data as Hex (8 signs)
   * @param tagID tag id (addressed mode)
   * @param optionFlag Meaning is defined by the tag command description.
   * @return the {@link HfTag}
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, Wrong Block number or wrong data length</li>
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
  public HfTag setTagData(int blockNumber, String data, String tagID, boolean optionFlag)
      throws RFIDReaderException, CommConnectionException {
    // int blocksize=8; //4byte hex
    // data must be an multiply of 8
    if (0 != data.length() % 8) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "Data Size must be an multiply of 8");
    }
    if (0 > blockNumber || blockNumber > 255) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "wrong Blocknumbers - 0<=blockNumber<256 ");
    }
    StringBuffer command = new StringBuffer();
    // prepare command
    command.append("WRQ ");
    switch (sri) {
      case SingleSubcarrier_100percentASK:
      case SingleSubcarrier_10percentASK:
        if (null == tagID) {
          // not addressed mode
          if (optionFlag)
            command.append("4221");
          else
            command.append("0221");
        } else {
          // addressed mode
          if (optionFlag)
            command.append("6221");
          else
            command.append("2221");
          command.append(tagID);
        }
        break;
      case DoubleSubcarrier_100percentASK:
        if (null == tagID) {
          // not addressed mode
          command.append("0321");
        } else {
          // addressed mode
          command.append("2321");
          command.append(tagID);
        }
        break;
    }
    command.append(String.format("%02X%s crc", blockNumber, data));
    HfTag tag = null;
    try {
      sendRequest(command.toString());
      tag = new HfTag(tagID);
      tag.setData(data);
      return tag;
    } catch (RFIDReaderException e) {
      tag = new HfTag(tagID);
      tag.setHasError(true);
      tag.setMessage(e.getLocalizedMessage());
    }
    return tag;
  }

  /**
   * Writes Data to the specified Tag
   * 
   * @param blockNumber blocknumber
   * @param data 4Byte data as Hex (8 signs)
   * @param tagID tag id (addressed mode)
   * @return the {@link HfTag}
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, Wrong Block number or wrong data length</li>
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
  public HfTag setTagData(int blockNumber, String data, String tagID)
      throws RFIDReaderException, CommConnectionException {
    return setTagData(blockNumber, data, tagID, false);
  }

  /**
   * Sets the afi byte to use
   * 
   * @param afi afi byte (0..16)
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, wrong parameter</li>
   *         </ul>
   */
  public void setUseAFI(int afi) throws RFIDReaderException {
    if (afi >= 0 && afi <= 16) {
      this.afi = afi;
    } else {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "afi 0..16");
    }
  }

  /**
   * Gets the used afi byte
   * 
   * @return used afi byte
   */
  public int getUseAFI() {
    return afi;
  }

  /**
   * Sets the reader mode
   * 
   * @param mode mode
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, the specified mode is null</li>
   *         <li>CCE, CRC communication error</li>
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
  public void setMode(MODE mode) throws RFIDReaderException, CommConnectionException {
    if (null == mode)
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "mode is null");
    String[] recvdata = communicateSynchronized("MOD " + ((MODE.ISO15693 == mode) ? "156" : "14B"));
    if (!recvdata[0].startsWith("OK!")) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, recvdata[0]);
    } else {
      this.mode = mode;
    }
  }

  /**
   * Gets the current used mode
   * 
   * @return MODE mode
   */
  public MODE getMODE() {
    return mode;
  }

  /**
   * Set RF Interface
   * 
   * @param sri sri
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, the specified ris is null</li>
   *         <li>CCE, CRC communication error</li>
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
  public void setSRI(SRI sri) throws RFIDReaderException, CommConnectionException {
    if (null == sri)
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "sri is null");
    String command;
    switch (sri) {
      case SingleSubcarrier_10percentASK:
        command = "SRI ss 10";
        break;
      case SingleSubcarrier_100percentASK:
        command = "SRI ss 100";
        break;
      case DoubleSubcarrier_100percentASK:
        command = "SRI ds 100";
        break;
      default:
        throw new RFIDReaderException(RFIDErrorCodes.WPA, "SRI Mode not implemented, " + sri.name());
    }
    String[] receiveData = communicateSynchronized(command);
    if (!receiveData[0].startsWith("OK!")) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, receiveData[0]);
    } else {
      this.sri = sri;
    }
  }

  /**
   * Gets the used modulation depth and subcarrier mode
   * 
   * @return SRI sri
   */
  public SRI getSRI() {
    return sri;
  }

  /**
   * Gets the tag system information
   * 
   * @param tagID tag id
   * @return HFTagInformation
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
  public HFTagInformation getTagSystemInformation(String tagID) throws RFIDReaderException, CommConnectionException {
    /*
     * Hashtable: DSFID_supported --> boolean AFI_supported --> boolean VICC_supported --> boolean
     * IC_reference_supported --> boolean DSFID --> int (if supported) IC_reference --> int (if supported) AFI --> int
     * (if supported) VICC_Block_size --> int (if supported) VICC_Number_of_Blocks --> int (if supported)
     */
    if (null == tagID)
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "tag ID is null");
    String sendData = String.format("REQ 222B%s crc", tagID);
    String answer = sendRequest(sendData);
    boolean isDSFID = false;
    boolean isAFI = false;
    boolean isVICCMS = false;
    boolean isICR = false;
    int DSFID = 0;
    int AFI = 0;
    int VICCNoB = 0;
    int VICCBS = 0;
    int ICR = 0;
    byte infoflag = (byte) Integer.parseInt(answer.substring(0, 2), 16);
    if (1 == (infoflag & 0x01)) {
      isDSFID = true;
      DSFID = Integer.parseInt(answer.substring(18, 20), 16);
    }
    if (2 == (infoflag & 0x02)) {
      isAFI = true;
      AFI = Integer.parseInt(answer.substring(20, 22), 16);
    }
    if (4 == (infoflag & 0x04)) {
      isVICCMS = true;
      VICCNoB = Integer.parseInt(answer.substring(22, 24), 16) + 1;
      VICCBS = (Integer.parseInt(answer.substring(24, 26), 16) & 0x3f) + 1;
    }
    if (8 == (infoflag & 0x08)) {
      isICR = true;
      ICR = Integer.parseInt(answer.substring(26, 28), 16);
    }
    return new HFTagInformation(tagID, isDSFID, DSFID, isAFI, AFI, isVICCMS, VICCNoB, VICCBS, isICR, ICR);
  }

  /**
   * Sets the AFI value of the specified tag
   * 
   * @param tagID tag id
   * @param afi afi value
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
  public void setTagAFI(String tagID, int afi) throws RFIDReaderException, CommConnectionException {
    String sendData = String.format("WRQ 2227%s%02X crc", tagID, afi);
    try {
      sendRequest(sendData);
    } catch (RFIDReaderException e) {
      throw new RFIDReaderException("setTagAFI: " + e.getMessage());
    }
  }

  /**
   * Locks the AFI value of the specified tag
   * 
   * @param tagID tag id
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
  public void lockTagAFI(String tagID) throws RFIDReaderException, CommConnectionException {
    String sendData = String.format("WRQ 2228%s crc", tagID);
    try {
      sendRequest(sendData);
    } catch (RFIDReaderException e) {
      throw new RFIDReaderException("lockTagAFI: " + e.getMessage());
    }
  }

  /**
   * Sets the DSFID (data storage format identifier) value of the specified tag
   * 
   * @param tagID tag id
   * @param dsfid DSFID value
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
  public void setTagDSFID(String tagID, int dsfid) throws RFIDReaderException, CommConnectionException {
    String senddata = String.format("WRQ 2229%s%02X crc", tagID, dsfid);
    try {
      sendRequest(senddata);
    } catch (RFIDReaderException e) {
      throw new RFIDReaderException("setTagDSFID: " + e.getMessage());
    }
  }

  /**
   * Locks the DSFID (data storage format identifier) of the specified tag
   * 
   * @param tagID tag id
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
  public void lockTagDSFID(String tagID) throws RFIDReaderException, CommConnectionException {
    String senddata = String.format("WRQ 222A%s crc", tagID);
    try {
      sendRequest(senddata);
    } catch (RFIDReaderException e) {
      throw new RFIDReaderException("lockTagDSFID: " + e.getMessage());
    }
  }

  /**
   * The reader allows different output power levels to match antenna size, tag size or tag po- sition. The power level
   * is given in milliwatt (mW). The minimum value is 500, the maximum is 4000 with steps of 250.
   * 
   * The second generation ISO 15693 devices with hardware revision &gt;= 02.00 (DeskID ISO, UM15, Dwarf15, QR15 and
   * QuasarMX) allow setting power values of 100 or 200 (mW).
   * 
   * @param value power in mW from 500mW to 4000mW in 250mW steps
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void setPower(int value) throws CommConnectionException, RFIDReaderException {
    String[] recvdata = communicateSynchronized("SET", "PWR", value);
    if (recvdata[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(recvdata[0], "Set power " + value);
  }

  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    String[] recvdata = communicateSynchronized("SAP", port);
    if (recvdata[0].equals(RESPONSE_OK)) {
      setCurrentAntennaPort(port);
      return;
    }
    handleUnexpectedResponse(recvdata[0], "Set antenna port " + port);
  }

  /**
   * In case you want to automatically switch between multiple antennas (e.g. trying to find all tags in a search area
   * that can only be searched using multiple antennas) you can use this automatic switching mode.<br>
   * 
   * Switching always starts with the lowest antenna port (0). Switching to the next antenna port oc- curs automatically
   * with the start of every tag manipulation command. No pin state is changed until the first tag manipulation
   * command.<br>
   * 
   * @param numberOfAntennas number of antennas [1,16], 0 for disable; Please note that for this parameter the number
   *        given is the counted number of participating antennas, not the antenna port numbers, thus stating a number
   *        "X" would stand for "X antennas participating".
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void setMultiplexAntennas(int numberOfAntennas) throws CommConnectionException, RFIDReaderException {
    enableAntennaReport(0 < numberOfAntennas && numberOfAntennas <= 16);
    String[] response = communicateSynchronized("SAP", "AUT",
        0 < numberOfAntennas && numberOfAntennas <= 16 ? numberOfAntennas : "OFF");
    if (response[0].equals(RESPONSE_OK)) {
      return;
    }
    handleUnexpectedResponse(response[0], "switch antenna port " + numberOfAntennas);
  }

  /**
   * 
   * @param enable value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  private void enableAntennaReport(boolean enable) throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SAP", "ARP", enable ? "ON" : "OFF");
    if (response[0].equals(RESPONSE_OK)) {
      return;
    }
    handleUnexpectedResponse(response[0], "enable antenna report " + enable);
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    prepareISOReader();
    return message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#handleInventroy(java.lang.String)
   */
  @Override
  protected void handleInventory(String inventory) {
    long timestamp = System.currentTimeMillis();
    synchronized (getIdentifier()) {
      try {
        String[] values = checkData(inventory);
        lastInventory = parseInventory(values, timestamp);
        addNewInventoryEvent(lastInventory);
      } catch (RFIDReaderException e1) {
        lastInventory = null;
        lastException = e1;
      }
      getIdentifier().notify();
    }
  }

  private List<HfTag> parseInventory(String[] answers, long timestamp) throws RFIDReaderException {
    List<HfTag> tags = new ArrayList<>();
    for (int i = 0; i < answers.length; i++) {
      String s = answers[i];
      if (RESPONSE_ERROR_LENGTH >= s.length()) {
        /*
         * check error codes - if it is a single tag error - ignore the error for this tag Error codes to ignore: CER,
         * FLE, RDL, TCE, TOE (see 'ISO 15693 Protocol Guide', Chapter Error Codes)
         */
        if (s.startsWith("CER") || s.startsWith("RXE") || s.startsWith("TOE") || s.startsWith("FLE")
            || s.startsWith("RDL") || s.startsWith("TCE")) {
          if (logger.isDebugEnabled()) {
            logger.debug("receive inventory error code " + s);
          }
        } else {
          throw new RFIDReaderException(RFIDErrorCodes.NER, s);
        }
      } else if (s.startsWith("ARP")) {
        setCurrentAntennaPort(Integer.parseInt(s.substring(4), 10));
        for (HfTag tag : tags) {
          tag.setAntenna(getCurrentAntennaPort());
        }
        continue;
      } else if (s.startsWith("IVF")) {
        break;
      } else {
        tags.add(new HfTag(s, timestamp, getCurrentAntennaPort()));
      }
    }
    return tags;
  }

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @param ssl true for single slot
   * @param ont if true only new tags in the field are returned
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void scanInventory(boolean ssl, boolean ont) throws CommConnectionException, RFIDReaderException {
    if (scanningForTags) {
      throw new RFIDReaderException(RFIDErrorCodes.BSY, "Reader is already scanning for tags");
    }
    scanningForTags = true;
    send("CNR INV", afi != 0 ? String.format("AFI %02X", afi) : null, ont ? "ONT" : null, ssl ? "SSL" : null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#startInventory()
   */
  @Override
  public void scanInventory() throws CommConnectionException, RFIDReaderException {
    scanInventory(false, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#stopInventory()
   */
  @Override
  public List<HfTag> stopInventory() throws CommConnectionException, RFIDReaderException {
    if (scanningForTags) {
      scanningForTags = false;
      return super.stopInventory();
    } else {
      return new ArrayList<>();
    }
  }

}
