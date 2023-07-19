/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.mf;

import java.util.ArrayList;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.TcpConnection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.ReaderType;
import com.metratec.lib.rfidreader.MetratecReaderGen1;
import com.metratec.lib.tag.MfTag;

/**
 * Class for the metraTec mifare reader
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public class MFReader extends MetratecReaderGen1<MfTag> {
  /** Card type mifare 1k */
  public final static int CARD_TYPE_MIFARE_1K = 0x08;
  /** Card type mifare 4k */
  public final static int CARD_TYPE_MIFARE_4K = 0x18;
  /** Card type mifare desfire */
  public final static int CARD_TYPE_MIFARE_DESFIRE = 0x2420;
  /** Card type mifare ultralight */
  public final static int CARD_TYPE_MIFARE_ULTRALIGHT = 0x400;

  private String tmpKey = "";
  private String keyType = "";
  private int authenticatedSector = -1;
  private String usedCard = "";
  private int selectedCardType;
  private String[] minReaderRevision;

  private List<MfTag> lastInventory = null;
  private RFIDReaderException lastException = null;
  private boolean scanningForTags = false;

  /**
   * Creates a new MifareReader with the specified connection
   * 
   * @param identifier reader identifier
   * @param connection connection
   * @param minReaderRevision min reader revision
   */
  protected MFReader(String identifier, ICommConnection connection, String... minReaderRevision) {
    super(identifier, connection);
    this.minReaderRevision = minReaderRevision;
  }

  /**
   * Use the reader classes ( {@link QuasarMF}, {@link DeskID_MF}, {@link Dwarf14}, {@link QR14})
   * for instantiate the reader<br>
   * Creates a new MifareReader class for communicate with the specified metraTec usb mifare reader
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb mifare reader
   */
  @Deprecated
  public MFReader(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200));
  }

  /**
   * Use the reader classes ( {@link QuasarMF}, {@link DeskID_MF}, {@link Dwarf14}, {@link QR14})
   * for instantiate the reader<br>
   * Creates a new UHFReader class for communicate with the specified metraTec ethernet mifare
   * reader
   * 
   * @param identifier reader identifier
   * @param ipAddress ip address of the ethernet mifare reader
   * @param port port of the ethernet uhf reader
   */
  @Deprecated
  public MFReader(String identifier, String ipAddress, int port) {
    super(identifier, new TcpConnection(ipAddress, port));

  }

  /**
   * Use the reader classes ( {@link QuasarMF}, {@link DeskID_MF}, {@link Dwarf14}, {@link QR14})
   * for instantiate the reader<br>
   * Creates a new UHFReader class for communicate with the specified metraTec ethernet mifare
   * reader
   * 
   * @param identifier reader identifier
   * @param portName port on which the rs232 mifare reader is connected
   * @param baudrate baudrate of the rs232 mifare reader
   * @param dataBit rs232 databits
   * @param stopBit rs232 stopbit
   * @param parity rs232 parity
   * @param flowControl rs232 flowcontrol
   */
  @Deprecated
  public MFReader(String identifier, String portName, int baudrate, int dataBit, int stopBit,
      int parity, int flowControl) {
    super(identifier,
        new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl));
  }

  private void prepareMifareReader() throws CommConnectionException, RFIDReaderException {
    scanningForTags = false;
    ReaderType type = getReaderType();
    if (null != minReaderRevision && minReaderRevision.length > 0) {
      checkReaderType(type, minReaderRevision);
    } else {
      checkReaderType(type, QuasarMF.MIN_READER_REVISON, DeskID_MF.MIN_READER_REVISON,
          Dwarf14.MIN_READER_REVISON, QR14.MIN_READER_REVISON);
    }

    setVerbosityLevel(1);
    setEndOfFrame(true);
  }

  /**
   * Use {@link MFReader#stop()} for close the connection
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
   * Enable the RF Field
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  public void enableRF() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("SRF", "ON");
  }

  /**
   * Disbale the RF Field
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  public void disableRF() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("SRF", "OFF");
  }

  /**
   * returns all UIDs from ISO/IEC 14443-1 to 3 compatible transponders, which are in the read range
   * of the reader.
   * 
   * @return List with the tag UIDs
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>NER, not expected response</li>
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
  public List<MfTag> getInventory() throws RFIDReaderException, CommConnectionException {
    return getInventory(false);
  }

  /**
   * returns all UIDs from ISO/IEC 14443-1 to 3 compatible transponders, which are new in the read
   * range of the reader.
   * 
   * @return List with the tag UIDs
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>NER, not expected response</li>
   *         </ul>
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public List<MfTag> getInventoryOnlyNewTags() throws RFIDReaderException, CommConnectionException {
    return getInventory(true);
  }

  /**
   * returns all UIDs from ISO/IEC 14443-1 to 3 compatible transponders, which are in the read range
   * of the reader.
   * 
   * @param onlyNewTags if true return only newly found tags
   * @return List with the tag UIDs
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>CCE, CRC communication error</li>
   *         <li>NER, no expected response</li>
   *         </ul>
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  private List<MfTag> getInventory(boolean onlyNewTags)
      throws RFIDReaderException, CommConnectionException {
    List<MfTag> tags;
    try {
      scanningForTags = true;
      send("INV", onlyNewTags ? "ONT" : null);
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
          throw new CommConnectionException(ICommConnection.RECV_TIMEOUT,
              "the reader did not respond (INV)");
        }
      }
    } finally {
      scanningForTags = false;
    }
    return tags;
  }

  /**
   * Reads data from a tag
   * 
   * @param blockNumber blocknumber
   * @return tag data
   * @throws RFIDReaderException possible Error codes:
   *         <ul>
   *         <li>AUTHENTICATION_ERROR</li>
   *         <li>CARD_NOT_AUTHENTICATED</li>
   *         <li>BLOCK_NOT_AUTHENTICATED</li>
   *         <li>NO_KEY_SELECTED</li>
   *         <li>TAG_NOT_RESPONSE</li>
   *         <li>NOT_EXPECTED_RESPONSE</li>
   *         <li>WRONG_PARAMETER</li>
   *         </ul>
   * @throws CommConnectionException possible Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public String getTagData(int blockNumber) throws RFIDReaderException, CommConnectionException {
    return getTagData(blockNumber, null);
  }

  /**
   * Reads data from the given tag
   * 
   * @param firstBlock first block
   * @param numberOfFollowingBlocks number of following blocks
   * @return tag data
   * @throws RFIDReaderException possible Error codes:
   *         <ul>
   *         <li>AUTHENTICATION_ERROR</li>
   *         <li>CARD_NOT_AUTHENTICATED</li>
   *         <li>BLOCK_NOT_AUTHENTICATED</li>
   *         <li>NO_KEY_SELECTED</li>
   *         <li>TAG_NOT_RESPONSE</li>
   *         <li>NOT_EXPECTED_RESPONSE</li>
   *         <li>WRONG_PARAMETER</li>
   *         </ul>
   * @throws CommConnectionException possible Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public String getTagData(int firstBlock, int numberOfFollowingBlocks)
      throws RFIDReaderException, CommConnectionException {
    StringBuffer data = new StringBuffer();
    for (int i = firstBlock; i <= firstBlock + numberOfFollowingBlocks; i++) {
      data.append(getTagData(i));
    }
    return data.toString();
  }

  /**
   * Reads data from the given tag and authenticated automatically
   * 
   * @param blockNumber blocknumber
   * @param TagID tag id
   * @return tag data
   * @throws RFIDReaderException possible Error codes:
   *         <ul>
   *         <li>AUTHENTICATION_ERROR</li>
   *         <li>CARD_NOT_AUTHENTICATED</li>
   *         <li>BLOCK_NOT_AUTHENTICATED</li>
   *         <li>NO_KEY_SELECTED</li>
   *         <li>TAG_NOT_RESPONSE</li>
   *         <li>NOT_EXPECTED_RESPONSE</li>
   *         <li>WRONG_PARAMETER</li>
   *         </ul>
   * @throws CommConnectionException possible Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public String getTagData(int blockNumber, String TagID)
      throws RFIDReaderException, CommConnectionException {
    if (null != TagID && (blockNumber > authenticatedSector * 4 + 3 || blockNumber < authenticatedSector * 4)) {
      selectedCardType = selectCard(TagID);
      switch (selectedCardType) {
        case CARD_TYPE_MIFARE_1K:
        case CARD_TYPE_MIFARE_4K:
          if (tmpKey.isEmpty() || keyType.isEmpty())
            throw new RFIDReaderException(RFIDErrorCodes.NKS,
                "no key and/or key type is selected");
          authenticatedSector(blockNumber / 4, tmpKey, keyType);
          break;
        case CARD_TYPE_MIFARE_DESFIRE:
          break;
        case CARD_TYPE_MIFARE_ULTRALIGHT:
          break;
      }
    }
    String[] receiveData = communicateSynchronized("RDT " + blockNumber);
    if (3 == receiveData[0].length()) {
      if (receiveData[0].startsWith("UPA")) {
        throw new RFIDReaderException(RFIDErrorCodes.UPA, "Unknown parameter ");
      } else if (receiveData[0].startsWith("EDX")) {
        throw new RFIDReaderException(RFIDErrorCodes.EDX,
            "A decimal parameter includes non decimal characters");
      } else if (receiveData[0].startsWith("BNA")) {
        throw new RFIDReaderException(RFIDErrorCodes.BNA, "Block not authenticated (any more)");
      } else if (receiveData[0].startsWith("BAE")) {
        throw new RFIDReaderException(RFIDErrorCodes.BAE, "Block not authenticated (any more)");
      } else if (receiveData[0].startsWith("NMA")) {
        throw new RFIDReaderException(RFIDErrorCodes.NMA,
            "No MiFare chip 1k or 4k authenticated (only ALL-Mode)");
      } else if (receiveData[0].startsWith("NOR")) {
        throw new RFIDReaderException(RFIDErrorCodes.NOR,
            "Number of blocks to Read is out of range");
      } else if (receiveData[0].startsWith("TNR")) {
        throw new RFIDReaderException(RFIDErrorCodes.TNR, "Tag " + usedCard + " not responding ");
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER, "Unkown reader answer (RDT): " + receiveData[0]);
      }
    } else {
      if (selectedCardType == CARD_TYPE_MIFARE_ULTRALIGHT) {
        if (receiveData[0].length() > 7)
          return receiveData[0].substring(0, 8);
        else
          throw new RFIDReaderException(RFIDErrorCodes.NER,
              "Unknown reader answer(RDT Ultralite): " + receiveData[0]);
      }
      return receiveData[0];
    }
  }

  /**
   * Reads data from the given tag and authenticated automatically
   * 
   * @param firstBlock first block
   * @param numberOfFollowingBlocks number of following blocks
   * @param TagID tag id
   * @return tag data
   * @throws RFIDReaderException possible Error codes:
   *         <ul>
   *         <li>AUTHENTICATION_ERROR</li>
   *         <li>CARD_NOT_AUTHENTICATED</li>
   *         <li>BLOCK_NOT_AUTHENTICATED</li>
   *         <li>NO_KEY_SELECTED</li>
   *         <li>TAG_NOT_RESPONSE</li>
   *         <li>NOT_EXPECTED_RESPONSE</li>
   *         <li>WRONG_PARAMETER</li>
   *         </ul>
   * @throws CommConnectionException possible Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public String getTagData(int firstBlock, int numberOfFollowingBlocks, String TagID)
      throws RFIDReaderException, CommConnectionException {
    StringBuffer data = new StringBuffer();
    for (int i = firstBlock; i <= firstBlock + numberOfFollowingBlocks; i++) {
      data.append(getTagData(i, TagID));
    }
    return data.toString();
  }

  // @Override
  // public void setOutput(int pin, boolean state) throws RFIDReaderException,
  // CommConnectionException
  // {
  //
  // }
  //
  // @Override
  // public boolean getInput(int pin) throws RFIDReaderException, CommConnectionException
  // {
  // return false;
  // }
  /**
   * Write Data to the Tag
   * 
   * @param blocknumber block number
   * @param data data
   * @param TagID tag id
   * @throws RFIDReaderException possible Error codes:
   *         <ul>
   *         <li>NO_KEY_SELECTED</li>
   *         <li>NO_CARD_SELECTED</li>
   *         <li>BLOCK_NOT_AUTHENTICATED</li>
   *         <li>CARD_NOT_AUTHENTICATED</li>
   *         <li>TAG_NOT_RESPONSE</li>
   *         <li>NOT_EXPECTED_RESPONSE</li>
   *         <li>WRONG_PARAMETER</li>
   *         </ul>
   * @throws CommConnectionException possible Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public void setTagData(int blocknumber, String data, String TagID)
      throws RFIDReaderException, CommConnectionException {
    if (null == data)
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "data is null");


    if (blocknumber > authenticatedSector * 4 + 3 || blocknumber < authenticatedSector * 4) {
      int cardType;
      if (null != TagID)
        cardType = selectCard(TagID);
      else
        cardType = selectCard(usedCard);
      switch (cardType) {
        case CARD_TYPE_MIFARE_1K:
        case CARD_TYPE_MIFARE_4K:
          if (tmpKey.isEmpty() || keyType.isEmpty())
            throw new RFIDReaderException(RFIDErrorCodes.NKS, "no key and/or key type is selected");
          authenticatedSector(blocknumber / 4, tmpKey, keyType);
          break;
        case CARD_TYPE_MIFARE_DESFIRE:
          break;
        case CARD_TYPE_MIFARE_ULTRALIGHT:
          break;
      }
    }
    String command;
    if (selectedCardType == CARD_TYPE_MIFARE_ULTRALIGHT) {
      command = "WDT W4 " + data + " " + blocknumber;
    } else {
      command = "WDT " + data + " " + blocknumber;
    }
    String[] receiveData = communicateSynchronized(command);
    if (!receiveData[0].endsWith("OK!")) {
      // error
      if (receiveData[0].startsWith("UPA")) {
        throw new RFIDReaderException(RFIDErrorCodes.UPA, "Unknown parameter ");
      } else if (receiveData[0].startsWith("EHX")) {
        throw new RFIDReaderException(RFIDErrorCodes.EHX,
            "The string cannot be interpreted as valid data or contains non hex characters");
      } else if (receiveData[0].startsWith("BNA")) {
        throw new RFIDReaderException(RFIDErrorCodes.BNA, "Block not authenticated (any more)");
      } else if (receiveData[0].startsWith("BAE")) {
        throw new RFIDReaderException(RFIDErrorCodes.BAE, "Block not authenticated (any more)");
      } else if (receiveData[0].startsWith("NMA")) {
        throw new RFIDReaderException(RFIDErrorCodes.NMA,
            "No MiFare chip 1k or 4k authenticated (only ALL-Mode)");
      } else if (receiveData[0].startsWith("WDL")) {
        throw new RFIDReaderException(RFIDErrorCodes.WDL,
            "The hex string does not have the correct length (i.e. 16 bytes in normal mode)");
      } else if (receiveData[0].startsWith("TNR")) {
        throw new RFIDReaderException(RFIDErrorCodes.TNR, "Tag " + usedCard + " not responding ");
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER, "Unkown reader answer (WDT): " + receiveData[0]);
      }
    }
  }

  /**
   * select a specific transponder
   * 
   * @param cardUID card id
   * @return Card type (SAK Code, SAK length is 1 byte for short UIDs (4Bytes) and 2 bytes for
   *         double length UID (7 bytes))
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>TNR, tag not responding</li>
   *         <li>EHX, hex expected</li>
   *         <li>CCE, CRC communication error</li>
   *         <li>NER, not expected response</li>
   *         </ul>
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public int selectCard(String cardUID) throws CommConnectionException, RFIDReaderException {
    usedCard = cardUID;
    String[] answer = communicateSynchronized("SEL MTS " + usedCard);

    try {
      selectedCardType = Integer.parseInt(answer[0], 16);
      return selectedCardType;
    } catch (NumberFormatException e) {
      // errorcode
      if (answer[0].startsWith("TNR")) {
        throw new RFIDReaderException(RFIDErrorCodes.TNR, "Tag " + cardUID + " not responding ");
      } else if (answer[0].startsWith("EHX")) {
        throw new RFIDReaderException(RFIDErrorCodes.EHX,
            "The string cannot be interpreted as a valid UID or includes non hex characters "
                + cardUID);
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER,
            "Unknown reader answer (SEL MTS): " + answer[0]);
      }
    }
  }

  /**
   * set the key to use for authentication
   * 
   * @param key key to use
   * @param type key type (A or B)
   * @throws RFIDReaderException possible Error codes:
   *         <ul>
   *         <li>NOT_EXPECTED_RESPONSE</li>
   *         <li>WRONG_PARAMETER</li>
   *         </ul>
   * @throws CommConnectionException possible Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public void setKeyToUse(String type, String key)
      throws CommConnectionException, RFIDReaderException {
    tmpKey = key;
    keyType = type;
    String[] receiveData = communicateSynchronized("STK " + tmpKey);
    if (!receiveData[0].startsWith("OK!")) {
      if (receiveData[0].startsWith("UPA")) {
        throw new RFIDReaderException(RFIDErrorCodes.UPA, "Unknown parameter " + key);
      } else if (receiveData[0].startsWith("EHX")) {
        throw new RFIDReaderException(RFIDErrorCodes.EHX,
            "Key-Parameter is missing or other characters than 0-9 and A-F " + key);
      } else if (receiveData[0].startsWith("WDL")) {
        throw new RFIDReaderException(RFIDErrorCodes.WDL, "Key is not 6 bytes long " + key);
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER, "Unknown reader answer (SKT): " + receiveData[0]);
      }
    }
    receiveData = communicateSynchronized("SKU TEMP");
    if (!receiveData[0].startsWith("OK!")) {
      // if(recv.equals("UPA\r"))
      // {
      // throw new RFIDReaderException(RFIDErrorCodes.WRONG_PARAMETER, "Unknown parameter");
      // }
      // else if(recv.equals("EDX\r"))
      // {
      // throw new RFIDReaderException(RFIDErrorCodes.WRONG_PARAMETER,
      // "Location parameter missing or other characters than 0-9 given");
      // }
      // else if(recv.equals("NOR\r"))
      // {
      // throw new RFIDReaderException(RFIDErrorCodes.WRONG_PARAMETER, "Location given is higher
      // than 23 ");
      // }
      // else if(recv.equals("KNS\r"))
      // {
      // throw new RFIDReaderException(RFIDErrorCodes.WRONG_PARAMETER,
      // "Key Not Set (if temporary key is selected, but not set before) ");
      // }
      // else
      // {
      throw new RFIDReaderException(RFIDErrorCodes.NER,
          "Unkown reader answer (SKU TEMP): " + receiveData[0]);
      // }
    }
  }

  /**
   * authenticate a MiFare sector
   * 
   * @param sector MiFare sector
   * @param key key to use
   * @param type key type (A or B)
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>BIH, Block is too high</li>
   *         <li>ATE, Authentication Error</li>
   *         <li>NKS, No Key Selected</li>
   *         <li>CNS, Card is not selected</li>
   *         <li>TNR, Tag not responding</li>
   *         <li>CCE, CRC communication error</li>
   *         <li>NER, no expected response</li>
   *         </ul>
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */
  public void authenticatedSector(int sector, String key, String type)
      throws CommConnectionException, RFIDReaderException {
    String[] receiveData = communicateSynchronized("AUT DRT " + key + " " + type + " " + sector * 4);
    if (!receiveData[0].startsWith("OK!")) {
      if (receiveData[0].startsWith("BIH")) {
        throw new RFIDReaderException(RFIDErrorCodes.BIH,
            "Block no. is too high (i.e. bigger than 63 at MiFare 1k) (sector*4) " + sector);
      } else if (receiveData[0].startsWith("ATE")) {
        throw new RFIDReaderException(RFIDErrorCodes.ATE,
            "Authentication Error (i.e. wrong key) " + key);
      } else if (receiveData[0].startsWith("NKS")) {
        throw new RFIDReaderException(RFIDErrorCodes.NKS,
            "No Key Select, select a temporary or a static key (use setKeyToUse) " + key);
      } else if (receiveData[0].startsWith("CNS")) {
        throw new RFIDReaderException(RFIDErrorCodes.CNS, "Card is Not Selected (use selectTag)");
      } else if (receiveData[0].startsWith("TNR")) {
        throw new RFIDReaderException(RFIDErrorCodes.TNR,
            "Tag not responding. Most time chip was deselected after an error or while leaving the field. Use SEL (select) before authenticating again");
      } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER,
            "Unknown reader answer (AUT DRT): " + receiveData[0]);
      }
    }
    authenticatedSector = sector;
  }

  /**
   * authenticate a Mifare sector
   * 
   * @param sector Mifare sector
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>BIH, Block is too high</li>
   *         <li>ATE, Authentication Error</li>
   *         <li>NKS, No Key Selected</li>
   *         <li>CNS, Card is not selected</li>
   *         <li>TNR, Tag Not Responding</li>
   *         <li>CCE, CRC communication error</li>
   *         <li>NER, no expected response</li>
   *         </ul>
   * @throws CommConnectionException possible ICommConnection Error codes:
   *         <ul>
   *         <li>NOT_INITIALISE</li>
   *         <li>CONNECTION_LOST</li>
   *         <li>RECV_TIMEOUT</li>
   *         <li>UNHANDLED_ERROR</li>
   *         </ul>
   */

  public void authenticatedSector(int sector) throws CommConnectionException, RFIDReaderException {
    if (tmpKey.isEmpty())
      throw new RFIDReaderException(RFIDErrorCodes.NKS, "no key selected");
    if (keyType.isEmpty())
      throw new RFIDReaderException(RFIDErrorCodes.NKS, "no key type selected");
    authenticatedSector(sector, tmpKey, keyType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#prepareDevice()
   */
  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    prepareMifareReader();
    return message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#handleInventory(java.lang.String)
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

  private List<MfTag> parseInventory(String[] answers, long timestamp) throws RFIDReaderException {
    List<MfTag> tags = new ArrayList<>();
    for (int i = 0; i < answers.length; i++) {
      String s = answers[i];
      if (RESPONSE_ERROR_LENGTH >= s.length()) {
        /*
         * check error codes - if it is a single tag error - ignore the error for this tag Error
         */
//        if (s.startsWith("CER") || s.startsWith("RXE") || s.startsWith("TOE") || s.startsWith("FLE")
//            || s.startsWith("RDL") || s.startsWith("TCE")) {
//          if (logger.isDebugEnabled()) {
//            logger.debug("receive inventory error code " + s);
//          }
//        } else {
        throw new RFIDReaderException(RFIDErrorCodes.NER, s);
//        }
      } else if (s.startsWith("IVF")) {
        break;
      } else {
        tags.add(new MfTag(s, timestamp, getCurrentAntennaPort()));
      }
    }
    return tags;
  }

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @param onlyNewTags if true only new tags in the field are returned
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void scanInventory(boolean onlyNewTags)
      throws CommConnectionException, RFIDReaderException {
    if (scanningForTags) {
      throw new RFIDReaderException(RFIDErrorCodes.BSY, "Reader is already scanning for tags");
    }
    scanningForTags = true;
    send("CNR INV", onlyNewTags ? "ONT" : null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#startInventory()
   */
  @Override
  public void scanInventory() throws CommConnectionException, RFIDReaderException {
    scanInventory(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#stopInventory()
   */
  @Override
  public List<MfTag> stopInventory() throws CommConnectionException, RFIDReaderException {
    if (scanningForTags) {
      scanningForTags = false;
      return super.stopInventory();
    } else {
      return new ArrayList<>();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#setAntennaPort(int)
   */
  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setPower(int power) throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "not available for mf reader");
  }

  @Override
  public void setMultiplexAntennas(int numberOfAntennas)
      throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "not available for mf reader");
  }
}
