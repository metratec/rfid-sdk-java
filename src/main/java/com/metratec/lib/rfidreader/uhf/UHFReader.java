/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader.uhf;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.ReaderType;
import com.metratec.lib.rfidreader.MetratecReaderGen1;
import com.metratec.lib.rfidreader.UHFProfileParameter;
import com.metratec.lib.rfidreader.UHFProfileParameter.UHFReaderType;
import com.metratec.lib.tag.UhfTag;

/**
 * Class for the metraTec uhf reader
 * 
 * @author Matthias Neumann (neumann@metratec.com)
 * 
 */
public class UHFReader extends MetratecReaderGen1<UhfTag> {

  private static Logger logger = LoggerFactory.getLogger(UHFReader.class);
  /** fifo length error */
  public static final String RESPONSE_ERROR_FLE = "FLE";
  /** fifo length error */
  public static final String RESPONSE_ERROR_HBE = "HBE";
  /** Preamble Detect Error */
  protected static final String RESPONSE_ERROR_PDE = "PDE";
  /** Response Length notes Expected Error */
  protected static final String RESPONSE_ERROR_RXE = "RXE";
  /** Read Data too Long */
  protected static final String RESPONSE_ERROR_RDL = "RDL";
  /** Antenna report - antenna port changed */
  protected static final String RESPONSE_ANTENNA_REPORT = "ARP";

  private READER_MODE readerMode = READER_MODE.ETS;
  private boolean addEPC = true;
  private boolean addTRS = true;

  private String[] minReaderRevision;

  private List<UhfTag> lastInventory = null;
  private RFIDReaderException lastException = null;
  private MEMBANK lastInventoryCall = null;
  private int dataStartAddress;
  private boolean inventoryIsEvent = true;

  /** Enum for the UHF Tag membank */
  public enum MEMBANK {
    /** The EPC membank. Contains CRC, PC and EPC. */
    EPC,
    /**
     * The tag ID of the tag (sometimes contains a unique ID, sometimes only a manufacturer code,
     * depending on the tag type)
     */
    TID,
    /** The optional user memory some tags have */
    USR,
    /** Reserved membank. Contains Kill password and Access password */
    RES,
    /** Access password */
    ACP,
    /** Kill password */
    KLP,
  }

  /** Enum for profile parameter */
  public enum PROFILE_PARAMETER {
    /** hysteresis value of the digitizer in 3dB steps [0..7] */
    DigitizerHysteresis,
    /**
     * high pass frequency. Ideal value depends on Link Frequency. For 320 kHz values from 0 to 4
     * work best for most tags
     */
    HighPassFrequency,
    /**
     * link frequency defined in the EPC Gen2 standard. The following values can be set: 0: 40kHz,
     * 6: 160kHz, 9: 256kHz, 12: 320kHz, 15: 640kHz
     */
    LinkFrequency,
    /**
     * low pass frequency. Ideal value depends on Link Frequency. Suggested values are 0 for 640kHz,
     * 4 for 320 kHz (default), 6 for 256 kHz and 7 for 160kHz and 40kHz
     */
    LowPassFrequency,
    /** the encoding (0: FMO, 1: MILLER2, 2: MILLER4, 3: MILLER8) */
    Encoding,
    /** no response wait time in 25,6&micro;s steps [0,255] */
    NoResponseWaitTime,
    /** transmitter power, allowed values depends on the reader type */
    TransmitterPower,
    /** time to wait before the receiver is activated. Multiplier is 6.4&micro;s [0,255] */
    RXWaitTime,
    /** receiver gain value, internally multiplied by 3 dB */
    ReceiverGain,
    /** tari value from EPC Gen2: 1 for 12.5&micro;s, 2 for 25&micro;s */
    Tari,
    /** the TRcal value from the EPC Gen2. Default is 66.7&micro;s (value = 667) */
    TRcal,
    /** true if 10dB Gain */
    DifferentialMixerGain,
    /** true if device ratio is 8, else the device ratio is 64/3 */
    DeviceRatio8,
    /** state of the mixer input attenuation */
    MixerInputAttenuation,
    /**
     * if true the Activate Phase Reversal Amplitude Shift Keyed (PR-ASK) modulation is activated
     * else Double Sideband Amplitude Shift Keyed (DSB-ASK)
     */
    PRASK,
    /** if true, the receiver reading a bit faster for most tags */
    SettlingSpeedUp,
  }

  /** Enum for the RFID communication standards */
  public enum READER_MODE {
    /** ETSI Mode */
    ETS,
    /** Israel Mode */
    ISR,
    /** FCC USA */
    FCC
  }

  /**
   * Creates a new UHFReader with the specified connection and ETSI Mode
   * 
   * @param identifier reader identifier
   * @param connection connection
   * @param minReaderRevision minimal reader revision
   */
  protected UHFReader(String identifier, ICommConnection connection, String... minReaderRevision) {
    this(identifier, connection, null, minReaderRevision);
  }

  /**
   * Creates a new UHFReader with the specified connection and reader mode
   * 
   * @param identifier reader identifier
   * @param connection connection
   * @param mode the used RFID communication standart. ({@link READER_MODE})
   * @param minReaderRevision minimal reader revision
   */
  protected UHFReader(String identifier, ICommConnection connection, READER_MODE mode,
      String... minReaderRevision) {
    super(identifier, connection);
    this.readerMode = mode;
    this.minReaderRevision = minReaderRevision;
    if (this.readerMode == null) {
      readerMode = READER_MODE.ETS;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#followUp()
   */
  @Override
  protected void followUp() throws RFIDReaderException, CommConnectionException {
    enableAdditionalEPC(false);
    enableAdditionalTRS(false);
    setRFInterface(false);
    super.followUp();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#prepareDevice()
   */
  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    lastInventoryCall = null;
    ReaderType type = getReaderType();
    if (null != minReaderRevision && minReaderRevision.length > 0) {
      checkReaderType(type, minReaderRevision);
    } else {
      checkReaderType(type, PulsarMX.MIN_READER_REVISION, PulsarLR.MIN_READER_REVISION,
          DeskID_UHF.MIN_READER_REVISION, DwarfG2.MIN_READER_REVISION);
    }
    setVerbosityLevel(1); // default
    setMode(readerMode);
    enableAdditionalEPC(true);
    enableAdditionalTRS(true);
    setRFInterface(true);
    if (type.equals(ReaderType.DESKID_UHF)) {
      setCurrentAntennaPort(1);
    } else {
      setCurrentAntennaPort(getAntennaPort());
    }
    // setSavePowerMode(true); <-- some tags are not reading with save power mode
    return message;
  }

  /**
   * Use {@link UHFReader#stop()} for close the connection
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

  // @Override
  // public void stop() throws CommConnectionException {
  //   // try {
  //   //   setRFInterface(false);
  //   // } catch (Exception e) {
  //   //   if (logger.isDebugEnabled()) {
  //   //     logger.debug(getIdentifier() + " error disconnect " + e.getLocalizedMessage());
  //   //   }
  //   // }
  //   super.stop();
  // }

  /**
   * Set the reader mode.
   * 
   * @param mode see {@link READER_MODE}
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setMode(READER_MODE mode) throws RFIDReaderException, CommConnectionException {
    if (null == mode)
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "mode is null");
    String[] receiveData = communicateSynchronized("STD", mode.name());
    if (receiveData[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(receiveData[0], "Set reader mode to " + mode.name());
  }



  /**
   * Set the etsi channel.
   * 
   * @param channel the channel number starting at zero [0..3]
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setCommunicationChannel(int channel)
      throws CommConnectionException, RFIDReaderException {
    String[] receiveData = communicateSynchronized("SRI CHA", channel);
    if (receiveData[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(receiveData[0], "Set communication channel " + channel);
  }

  /**
   * Read the reflected power value from the reader front-end
   * 
   * @return complex number as two decimal coded values (A-Channel and B-Channel) separated by a
   *         whitespace
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public String readReflectedPower() throws CommConnectionException, RFIDReaderException {
    String[] receiveData = communicateSynchronized("RRP");
    if (RESPONSE_ERROR_LENGTH < receiveData[0].length())
      return receiveData[0];
    handleUnexpectedResponse(receiveData[0], "Read Reflected Power");
    return ""; // <-- Can not happen! But eclipse Java parser does not recognize this
  }

  /**
   * Looks for all tags in range of the reader and get the EPCs of all tags as a number of strings
   * back<br>
   * 
   * @return List with the EPCs of founded tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  @Override
  public List<UhfTag> getInventory() throws RFIDReaderException, CommConnectionException {
    return getInventory(false, false, false);
  }

  /**
   * Looks for all tags in range of the reader with single slot and get the EPCs of all tags as a
   * number of strings back<br>
   * 
   * @return List with the EPCs of founded tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> getInventorySingleSlot() throws RFIDReaderException, CommConnectionException {
    return getInventory(true, false, false);
  }

  /**
   * Looks for all tags in range of the reader and sends their EPCs as as hex-coded numbers one EPC
   * per line back to the host. To detect multiple tags at once it uses the anti collision algorithm
   * defined by the EPC UHF Class 1 Gen 2 specification. This is the most common command for almost
   * any UHF RFID application. Using the CNR prefix this command can be used to search for tags
   * continuously. The length of the answer (the length of the EPC) is defined by the Protocol
   * Control (PC) data field on the tag as defined by the EPC Class 1 Gen 2 Protocol.
   * 
   * @param singSlot Single Slot (sets Q and IR values to zero for this round)
   * @param onlyNewTag This flag causes the reader to not reset the state of tags via a select
   *        command. Under normal conditions, this causes the tags to be found only once as long as
   *        they are not depowered or reset.
   * @param secure This flag causes the reader to bring the tag to secured / open mode. It may help
   *        if you need to be sure to find no tag more than once (as long as the tags stay pow-
   *        ered). If this flag is not set it is possible under certain circumstances that tags are
   *        found multiple times within one inventory cycle.
   * @return List with the EPCs of founded tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> getInventory(boolean singSlot, boolean onlyNewTag, boolean secure)
      throws RFIDReaderException, CommConnectionException {
    if (null != lastInventoryCall) {
      throw new RFIDReaderException(RFIDErrorCodes.BSY, "Reader is already scanning for tags");
    }
    return getTransponder(MEMBANK.EPC, "INV", singSlot ? "SSL" : null, onlyNewTag ? "ONT" : null,
        secure ? "SEC" : null);
  }

  /**
   * Reads the TID from the tag.<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags. If addEPC and/or addTRS is set, the List with data contains also
   * the EPC and/or the TRS (first entry data, second entry EPC, third entry TRS, and so on).
   * 
   * @return List with the TIDs
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> getTagTIDs() throws CommConnectionException, RFIDReaderException {
    return getTagData(MEMBANK.TID, 0, 0, false);
  }

  /**
   * Reads the TID from the tag with a predefined length.<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags. If addEPC and/or addTRS is set, the List with data contains also
   * the EPC and/or the TRS (first entry data, second entry EPC, third entry TRS, and so on).
   * 
   * @param length length of TID words
   * @return List with the TIDs, can include HBE xx for error from the tag
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> getTagTIDs(int length) throws CommConnectionException, RFIDReaderException {
    return getTagData(MEMBANK.TID, 0, length, false);
  }

  /**
   * Reads the data from the tags.<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags. If addEPC and/or addTRS is set, the List with data contains also
   * the EPC and/or the TRS (first entry data, second entry EPC, third entry TRS, and so on).
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param words number of words to read (0 for all)
   * @return List with data, can include HBE xx for error from the tag
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> getTagData(MEMBANK membank, int startAddress, int words)
      throws RFIDReaderException, CommConnectionException {
    return getTagData(membank, startAddress, words, false);
  }

  /**
   * Reads the data from the tags.<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags. If addEPC and/or addTRS is set, the List with data contains also
   * the EPC and/or the TRS (first entry data, second entry EPC, third entry TRS, and so on).
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param words number of words to read (0 for all) - word == 4 byte
   * @param ssl true for use single slot
   * @return List with data, can include HBE xx for error from the tag
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> getTagData(MEMBANK membank, int startAddress, int words, boolean ssl)
      throws RFIDReaderException, CommConnectionException {
    if (null == membank) {
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "membank is null");
    }
    if (null != lastInventoryCall) {
      throw new RFIDReaderException(RFIDErrorCodes.BSY, "Reader is already scanning for tags");
    }
    dataStartAddress = startAddress;

    return getTransponder(MEMBANK.EPC.equals(membank) ? MEMBANK.USR : membank, "RDT",
        ssl ? "SSL" : null, membank.name(), Integer.toHexString(startAddress),
        Integer.toHexString(words));
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
        if(inventoryIsEvent){
          addNewInventoryEvent(lastInventory);
        }
      } catch (RFIDReaderException e1) {
        lastInventory = null;
        lastException = e1;
      }
      getIdentifier().notify();
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
  private List<UhfTag> parseInventory(String[] answers, long timestamp) throws RFIDReaderException {
    List<UhfTag> tags = new ArrayList<>();
    UhfTag tag = null;
    for (int i = 0; i < answers.length; i++) {
      String s = answers[i];
      if (RESPONSE_ERROR_LENGTH >= s.length() || s.charAt(RESPONSE_ERROR_LENGTH) == ' ') {
        // System.err.println(s);
        switch (s.charAt(0)) {
          case '-':
            // should not happen
            tag.setRssi(Integer.valueOf(s));
            continue;
          case 'A':
            if (s.startsWith(RESPONSE_ANTENNA_REPORT)) {
              setCurrentAntennaPort(Integer.parseInt(s.substring(4), 10));
              for (UhfTag uhfTag : tags) {
                uhfTag.setAntenna(getCurrentAntennaPort());
              }
              continue;
            }
            break;
          case 'C':
            if (s.startsWith(RESPONSE_ERROR_CER)) {
              logger.trace("Get Inventory: CRC error");
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'F':
            if (s.startsWith(RESPONSE_ERROR_FLE)) {
              // FLE per tag, all other tags not korrupt
              logger.trace("Get Inventory: Fifo length error");
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'H':
            if (s.startsWith(RESPONSE_ERROR_HBE)) {
              // FLE per tag, all other tags not korrupt
              logger.trace("Get Inventory: HBE");
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'I':
            if (s.startsWith("IVF")) {
              return tags;
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'O':
            if (s.startsWith("OK!")) {
              // was a write data command...and this tag was ok...replace the OK! with a string
              // greater than three and rerun
              answers[i] = "WDTOK!";
              --i;
              continue;
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'P':
            if (s.startsWith(RESPONSE_ERROR_PDE)) {
              logger.trace("Get Inventory: PDE error");
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'R':
            if (s.startsWith(RESPONSE_ERROR_RXE)) {
              logger.trace("Get Inventory: RXE error");
            } else if (s.startsWith(RESPONSE_ERROR_RDL)) {
              logger.trace("Get Inventory: RDL error");
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          case 'T': // TCE TOE TOR
            if (s.startsWith(RESPONSE_ERROR_TOE)) {
              logger.trace("Get Inventory: Time out error");
            } else if (s.startsWith(RESPONSE_ERROR_TCE)) {
              logger.trace("Get Inventory: Tag Communication Error");
            } else if (s.startsWith(RESPONSE_ERROR_TOR)) {
              logger.trace("Get Inventory: Tag Out of Range");
            } else {
              handleUnexpectedResponse(s, "inventory");
            }
            break;
          default:
            handleUnexpectedResponse(s, "inventory");
            break;
        }
        // ignore following EPC and TRS
        if (addEPC) {
          i++;
        }
        if (addTRS) {
          i++;
        }
      } else {
        tag = new UhfTag(timestamp, getCurrentAntennaPort());
        if (addEPC) {
          tag.setEpc(answers[++i]);
        }
        if (addTRS) {
          tag.setRssi(Integer.parseInt(answers[++i]));
        }
        tags.add(tag);
        switch (lastInventoryCall) {
          case EPC:
            break;
          case USR:
            tag.setData(s);
            tag.setDataStartAddress(dataStartAddress);
            break;
          case TID:
            tag.setTid(s);
            break;
          case ACP:
            break;
          case KLP:
            break;
          case RES:
            break;
          default:
            throw new RFIDReaderException(RFIDErrorCodes.UE3,
                "Inventory response but no membank are set");
        }
      }
    }
    return tags;
  }

  private List<UhfTag> getTransponder(MEMBANK membank, String command, Object... parameters)
      throws RFIDReaderException, CommConnectionException {
    lastInventoryCall = membank;
    try {
      lastInventory = null;
      lastException = null;
      send(command, parameters);
      synchronized (getIdentifier()) {
        try {
          getIdentifier().wait(getReceiveTimeout());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (null != lastInventory) {
          return lastInventory;
        } else if (null != lastException) {
          throw lastException;
        } else {
          throw new CommConnectionException(ICommConnection.RECV_TIMEOUT,
              "the reader did not respond (" + command + ")");
        }
      }
    } finally {
      lastInventoryCall = null;
    }
  }

  /**
   * Writes data to the tag<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags.
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param hexData data to write * @return List with "OK!" for success
   * @return list with written tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> setTagData(MEMBANK membank, String hexData, int startAddress)
      throws CommConnectionException, RFIDReaderException {
    return setTagData(membank, hexData, startAddress, false);
  }

  /**
   * Writes data to the tag<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags.
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param hexData data to write
   * @param ssl true for use single slot
   * @return List with "OK!" for success
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> setTagData(MEMBANK membank, String hexData, int startAddress, boolean ssl)
      throws CommConnectionException, RFIDReaderException {
    if (null == membank) {
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "membank is null");
    }
    List<UhfTag> tags;
    dataStartAddress = startAddress;
    inventoryIsEvent = false;
    try {
      tags = getTransponder(membank, "WDT", ssl ? "SSL" : null, membank.name(),
          -1 < startAddress ? Integer.toHexString(startAddress) : null, hexData);
    } finally {
      inventoryIsEvent = true;
    }
    // update the tag with the data...
    switch (membank) {
      case EPC:
        try {
          if (startAddress < 2) {
            // remove words from new epc
            String newEpc = hexData.substring(startAddress == 0 ? 8 : 4);
            for (UhfTag tag : tags) {
              tag.setEpc(newEpc);
            }
          } else if (startAddress > 2) {
            // replace words from old epc..but length doesn't changed
            int indexOfChangedEpc = (startAddress - 2) * 4;
            for (UhfTag tag : tags) {
              int epcLength = tag.getEpc().length();
              tag.setEpc(tag.getEpc().substring(0, indexOfChangedEpc) + hexData);
              if (epcLength < tag.getEpc().length()) {
                tag.setEpc(tag.getEpc().substring(0, epcLength));
              }
            }
          }
        } catch (IndexOutOfBoundsException e) {
          logger.debug("Error update the epc of the tags - " + e.getMessage());
        }
        break;
      case USR:
        for (UhfTag tag : tags) {
          tag.setData(hexData);
          tag.setDataStartAddress(dataStartAddress);
        }
        break;
      // case TID:
      // break;
      // case ACP:
      // break;
      // case KLP:
      // break;
      // case RES:
      // break;
      default:
        break;
    }
    return tags;
  }

  /**
   * Set a EPC mask
   * 
   * @param mask mask
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setEPCMask(String mask) throws CommConnectionException, RFIDReaderException {
    setMask(MEMBANK.EPC, mask, 32);
  }

  /**
   * Most tags manipulation instruction can be limited to a population of tags with certain data
   * values, e.g. tags that start with a certain EPC, a certain TID or even contain certain data in
   * the user memory. This is done via a mask given with each command. Using this feature you can
   * address certain tags in the field with directly accessing each tag via its TID or EPC. <br>
   * <i>Please Note: the tag epc in the tag storage start on bit position 32, if you want filter the
   * tag epc please use the method setMask(MEMBANK.EPC, epc mask in hex, 32) </i>
   * 
   * @param membank membank from the chip
   * @param hexMaskValue mask value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setMask(MEMBANK membank, String hexMaskValue)
      throws CommConnectionException, RFIDReaderException {
    setMask(membank, hexMaskValue, -1, -1);
  }

  /**
   * Most tags manipulation instruction can be limited to a population of tags with certain data
   * values, e.g. tags that start with a certain EPC, a certain TID or even contain certain data in
   * the user memory. This is done via a mask given with each command. Using this feature you can
   * address certain tags in the field with directly accessing each tag via its TID or EPC. <br>
   * <i>Please Note: the tag epc in the tag storage start on bit position 32, if you want filter the
   * tag epc please use the method setMask(MEMBANK.EPC, epc mask in hex, 32) </i>
   * 
   * @param membank membank from the chip
   * @param hexMaskValue mask value
   * @param start start address in bit, default = 0
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setMask(MEMBANK membank, String hexMaskValue, int start)
      throws CommConnectionException, RFIDReaderException {
    setMask(membank, hexMaskValue, start, -1);
  }

  /**
   * Most tags manipulation instruction can be limited to a population of tags with certain data
   * values, e.g. tags that start with a certain EPC, a certain TID or even contain certain data in
   * the user memory. This is done via a mask given with each command. Using this feature you can
   * address certain tags in the field with directly accessing each tag via its TID or EPC.<br>
   * <i>For setting the a epc mask, the start should be 32.</i>
   * 
   * @param membank membank from the chip
   * @param hexMaskValue mask value
   * @param start start address in bit, default = 0
   * @param bitLength length in bit (max is 248 Bits (31 Byte)). Default = Length of Mask Value
   *        (full Nibbles)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setMask(MEMBANK membank, String hexMaskValue, int start, int bitLength)
      throws CommConnectionException, RFIDReaderException {
    String subCommand;
    switch (membank) {
      case EPC: // falls through
      case TID: // falls through
      case USR:
        subCommand = membank.name();
        break;
      default:
        throw new RFIDReaderException(RFIDErrorCodes.NOS, "Not supported");
    }
    String response[] = communicateSynchronized("SET", "MSK", subCommand, hexMaskValue,
        start >= 0 ? Integer.toHexString(start) : null,
        bitLength >= 0 ? Integer.toHexString(bitLength) : null);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set " + membank.name() + " Mask ");
  }

  /**
   * Disable the mask using
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void resetMask() throws CommConnectionException, RFIDReaderException {
    String response[] = communicateSynchronized("SET", "MSK", "OFF");
    if (response[0].equals(RESPONSE_OK)) {
      return;
    }
    handleUnexpectedResponse(response[0], "Disable mask");
  }

  /**
   * Disable the mask using
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setNoMask() throws CommConnectionException, RFIDReaderException {
    resetMask();
  }

  /**
   * Enable or disable the RF Field
   * 
   * @param state true enable, false disable the RF field
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setRFInterface(boolean state) throws RFIDReaderException, CommConnectionException {
    String[] answer = communicateSynchronized("SRI", state ? "ON" : "OFF");
    if (answer[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(answer[0], "Set RF interface " + state);
  }

  /**
   * Timer controlled RF field reset. Turns the field off, waits for the specified number of ms and
   * then turns the field back on. Can be useful to reset all tags in the field without managing
   * everything
   * 
   * @param milliseconds milliseconds
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void resetRFInterface(int milliseconds)
      throws CommConnectionException, RFIDReaderException {
    String[] answer = communicateSynchronized("SRI", "TIM", milliseconds);
    if (answer[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(answer[0], "Reset RF interface (" + milliseconds + " ms)");
  }

  /**
   * If enable the reader will switch off the power amplifier automatically after every tag
   * operation starts (either directly user called or CNR-mode) reducing the power consumption
   * nearly as much as STB (StandyBy need about 3/4 the power of only disabled amplifier) but does
   * not need to be woke up by WAK command. Also all reader commands are usable with disabled
   * amplifier.
   * 
   * @param state true for enable and false for disable the power save mode
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setSavePowerMode(boolean state) throws CommConnectionException, RFIDReaderException {
    String[] answer = communicateSynchronized("SRI", "SPM", state ? "ON" : "OFF");
    if (answer[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(answer[0], "Save Power Mode " + (state ? "ON" : "OFF"));
  }

  /**
   * Sets the given parameter
   * 
   * @param parameter parameter
   * @param value value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setProfileParameter(PROFILE_PARAMETER parameter, Object value)
      throws CommConnectionException, RFIDReaderException {
    String subCommand;
    switch (parameter) {
      case DigitizerHysteresis:
        subCommand = "DHS";
        break;
      case HighPassFrequency:
        subCommand = "HPF";
        break;
      case LinkFrequency:
        subCommand = "LKF";
        break;
      case LowPassFrequency:
        subCommand = "LPF";
        break;
      case Encoding:
        subCommand = "MIL";
        break;
      case NoResponseWaitTime:
        subCommand = "NRW";
        break;
      case TransmitterPower:
        subCommand = "PWR";
        break;
      case RXWaitTime:
        subCommand = "RWT";
        break;
      case ReceiverGain:
        subCommand = "RXG";
        break;
      case Tari:
        subCommand = "TAR";
        break;
      case TRcal:
        subCommand = "TRC";
        break;
      case DifferentialMixerGain:
        subCommand = "DMG";
        break;
      case DeviceRatio8:
        subCommand = "DR8";
        break;
      case MixerInputAttenuation:
        subCommand = "MIA";
        break;
      case PRASK:
        subCommand = "PAS";
        break;
      case SettlingSpeedUp:
        subCommand = "SSU";
        break;
      default:
        throw new RFIDReaderException(RFIDErrorCodes.WPA, parameter.name() + " unhandled");
    }
    String[] answer = communicateSynchronized("CFG", subCommand,
        value instanceof Boolean ? ((Boolean) value) ? "ON" : "OFF" : value);
    if (answer[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(answer[0], "Set profile parameter " + parameter.name() + " " + value);
  }

  /**
   * @param power reader power value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setPower(int power) throws CommConnectionException, RFIDReaderException {
    setProfileParameter(PROFILE_PARAMETER.TransmitterPower, power);
  }

  /**
   * @return the current reader power
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an reader exception occurs
   */
  public int getPower() throws CommConnectionException, RFIDReaderException {
    Object value = getProfilParameter(PROFILE_PARAMETER.TransmitterPower);
    try {
      return Integer.parseInt(getProfilParameter(PROFILE_PARAMETER.TransmitterPower).toString());
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, value.toString());
    }
  }

  @Override
  public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {
    String[] receiveData = communicateSynchronized("SAP MAN", port);
    if (receiveData[0].equals(RESPONSE_OK)) {
      setCurrentAntennaPort(port);
      return;
    }
    handleUnexpectedResponse(receiveData[0], "Set antenna port " + port);
  }

  /**
   * Set the antenna port.
   * 
   * @return the current set antenna port
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public int getAntennaPort() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("RAP");
    if (response[0].length() > 4) {
      try {
        if (response[0].length() > 6) {
          // automatic antenna change is enable (AUT 3 4 ARP)
          String value = response[0].substring(4);
          value = value.substring(0, value.indexOf(' '));
          return Integer.parseInt(value, 10);
        } else {
          // automatic antenna change is not enable (AUT 3)
          return Integer.parseInt(response[0].substring(4), 10);
        }
      } catch (NumberFormatException e) {
        throw new RFIDReaderException(RFIDErrorCodes.NER, response[0]);
      }
    }
    handleUnexpectedResponse(response[0], "Get antenna port");
    return -1; // <-- Can not happen! Eclipse Java parser does not recognize this
  }

  /**
   * In case you want to automatically switch between multiple antennas (e.g. trying to find all
   * tags in a search area that can only be searched using multiple antennas) you can use this
   * automatic switching mode.<br>
   * 
   * Switching always starts with the lowest antenna port (0). Switching to the next antenna port
   * oc- curs automatically with the start of every tag manipulation command. No pin state is
   * changed until the first tag manipulation command.<br>
   * 
   * @param numberOfAntennas number of antennas [1,16], 0 for disable; Please note that for this
   *        parameter the number given is the counted number of participating antennas, not the
   *        antenna port numbers, thus stating a number "X" would stand for "X antennas
   *        participating".
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  @Override
  public void setMultiplexAntennas(int numberOfAntennas)
      throws CommConnectionException, RFIDReaderException {
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
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  private void enableAntennaReport(boolean enable)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SAP", "ARP", enable ? "ON" : "OFF");
    if (response[0].equals(RESPONSE_OK)) {
      return;
    }
    handleUnexpectedResponse(response[0], "enable antenna report " + enable);
  }

  /**
   * use {@link #getProfilParameter(PROFILE_PARAMETER)} instead<br>
   * Gets the specific profile parameter
   * 
   * @param parameter parameter
   * @return integer or boolean with the value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  @Deprecated
  public Object getHardwareCommunicationParameter(PROFILE_PARAMETER parameter)
      throws CommConnectionException, RFIDReaderException {
    return getProfilParameter(parameter);
  }

  /**
   * Gets the specific profile parameter
   * 
   * @param parameter parameter
   * @return integer or boolean with the value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public Object getProfilParameter(PROFILE_PARAMETER parameter)
      throws CommConnectionException, RFIDReaderException {
    UHFProfileParameter profile = getProfileParameter();
    switch (parameter) {
      case DigitizerHysteresis:
        return profile.getDigitizerHysteresis();
      case HighPassFrequency:
        return profile.getHighPassFrequency();
      case LinkFrequency:
        return profile.getLinkFrequency();
      case LowPassFrequency:
        return profile.getLowPassFrequency();
      case Encoding:
        return profile.getEncoding();
      case NoResponseWaitTime:
        return profile.getNoResponseWaitTime();
      case TransmitterPower:
        return profile.getTransmitterPower();
      case RXWaitTime:
        return profile.getRxWaitTime();
      case ReceiverGain:
        return profile.getReceiverGain();
      case Tari:
        return profile.getTari();
      case TRcal:
        return profile.getTrcal();
      case DifferentialMixerGain:
        return profile.isDifferentialMixerGain();
      case DeviceRatio8:
        return profile.isDeviceRatio8();
      case MixerInputAttenuation:
        return profile.isMixerInputAttenuation();
      case PRASK:
        return profile.isPRASK();
      case SettlingSpeedUp:
        return profile.isSettlingSpeedUp();
      default:
        throw new RFIDReaderException(RFIDErrorCodes.WPA, "wrong communication parameter");
    }
  }

  /**
   * Gets the profile parameter from the reader
   * 
   * @return the profile parameter
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public UHFProfileParameter getProfileParameter()
      throws CommConnectionException, RFIDReaderException {
    UHFProfileParameter profile = null;
    ReaderType type = getReaderType();
    UHFReaderType readerType;
    switch (type) {
      case PULSAR_MX:
      case PULSAR_LR:
        readerType = UHFReaderType.PULSAR;
        break;
      case DESKID_UHF:
        readerType = UHFReaderType.DESKID;
        break;
      case DWARFG2:
        readerType = UHFReaderType.DWARFG2;
      case DWARFG2_MINI:
        readerType = UHFReaderType.DWARFG2;
        break;
      default:
        readerType = UHFReaderType.DESKID;
        break;
    }
    profile = new UHFProfileParameter(readerType);
    String[] answer = communicateSynchronized("CFG", "PRP");
    if (!answer[0].startsWith(RESPONSE_OK))
      handleUnexpectedResponse(answer[0], "Get profile parameter");
    String callInfo = "get profile parameter";
    for (int i = answer.length; --i > 0;) {
      String param = answer[i].replace(" ", "").toLowerCase();
      switch (param.charAt(0)) {
        case 'd': // DHS DMG DR8
          switch (param.charAt(1)) {
            case 'h': // DHS 0
              profile.setDigitizerHysteresis(Integer.parseInt(param.substring(3)));
              break;
            case 'm': // DMG OFF
              profile.setDifferentialMixerGain(param.substring(3).contains("on"));
              break;
            case 'r': // DR8 ON
              profile.setDeviceRatio8(param.substring(3).contains("on"));
              break;
            default:
              handleUnexpectedResponse(param, callInfo);
              break;
          }
          break;
        case 'h': // HPF 0
          profile.setHighPassFrequency(Integer.parseInt(param.substring(3)));
          break;
        case 'l': // LKF LPF
          switch (param.charAt(1)) {
            case 'k':// LKF 12
              profile.setLinkFrequency(Integer.parseInt(param.substring(3)));
              break;
            case 'p':// LPF 4
              profile.setLowPassFrequency(Integer.parseInt(param.substring(3)));
              break;
            default:
              handleUnexpectedResponse(param, callInfo);
              break;
          }
          break;
        case 'm': // MIA MIL
          switch (param.charAt(2)) {
            case 'a': // MIA OFF
              profile.setMixerInputAttenuation(param.substring(3).contains("on"));
              break;
            case 'l': // MIL 3
              profile.setEncoding(Integer.parseInt(param.substring(3)));
              break;
            default:
              handleUnexpectedResponse(param, callInfo);
              break;
          }
          break;
        case 'n':// NRW 11
          profile.setNoResponseWaitTime(Integer.parseInt(param.substring(3)));
          break;
        case 'p':// PAS PWR
          switch (param.charAt(1)) {
            case 'a': // PAS OFF
              profile.setPRASK(param.substring(3).contains("on"));
              break;
            case 'w': // PWR 15
              profile.setTransmitterPower(Integer.parseInt(param.substring(3)));
              break;
            default:
              handleUnexpectedResponse(param, callInfo);
              break;
          }
          break;
        case 'r':// RWT RXG
          switch (param.charAt(1)) {
            case 'w': // RWT 1
              profile.setRxWaitTime(Integer.parseInt(param.substring(3)));
              break;
            case 'x': // RXG -2
              if (ReaderType.DWARFG2_MINI.equals(type)) {
                profile.setReceiverGain(0);
              } else {
                profile.setReceiverGain(Integer.parseInt(param.substring(3)));
              }
              break;
            default:
              handleUnexpectedResponse(param, callInfo);
              break;
          }
          break;
        case 's':// SSU ON
          profile.setSettlingSpeedUp(param.substring(3).contains("on"));
          break;
        case 't':// TAR TRC
          switch (param.charAt(1)) {
            case 'a': // TAR 1
              profile.setTari(Integer.parseInt(param.substring(3)));
              break;
            case 'r': // TRC 667
              profile.setTrcal(Integer.parseInt(param.substring(3)));
              break;
            default:
              handleUnexpectedResponse(param, callInfo);
              break;
          }
          break;
        default:
          handleUnexpectedResponse(param, callInfo);
          break;
      }

    }
    return profile;
  }

  /**
   * Any tag command (INV, RDT etc.) starts a global timeout. If the function does not terminate -
   * either successful or returning an error � the function will be killed and TOE error code
   * printed. SET GTO changes the timeout value. It is given in decimal milliseconds. Default: 250ms
   * 
   * @param milliseconds timeout in milliseconds (250ms default)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setGlobalTimeOut(int milliseconds)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "GTO", milliseconds);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set global timeout to: " + milliseconds);
  }

  /**
   * By setting this parameter the user activates the EPC-ADD mode. If active, every successful tag
   * command and every error code will add the EPC after the answer (read data, "OK!", etc.) of the
   * function and before the optional RSSI values (SET TRS ON/OFF). This makes it easier to identify
   * which tag is actually responding to a command like e.g. Write Data or Read Data.
   * 
   * @param state true for activate, false deactivate
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void addEPC(boolean state) throws CommConnectionException, RFIDReaderException {
    addEPC = state;
    enableAdditionalEPC(state);
  }

  /**
   * By setting this parameter the user activates the EPC-ADD mode. If active, every successful tag
   * command and every error code will add the EPC after the answer (read data, "OK!", etc.) of the
   * function and before the optional RSSI values (SET TRS ON/OFF). This makes it easier to identify
   * which tag is actually responding to a command like e.g. Write Data or Read Data.
   * 
   * @param state true for activate, false deactivate
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  protected void enableAdditionalEPC(boolean state)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "EPC", state ? "ON" : "OFF");
    if (response[0].equals(RESPONSE_OK)) {
      return;
    }
    handleUnexpectedResponse(response[0], "Set adding EPC state: " + (state ? "ON" : "OFF"));
  }

  /**
   * Sometimes you want to know the received signal strength when communicating with a transponder.
   * With the TRS setting the reader will automatically add the RSSI to responses from a tag. The
   * value is always negative in a range from -25 to -70 with -25 being the best case. The value
   * will be printed in a new line following the answer of the tag operation and following the EPC
   * if "SET EPC ON" command is used.
   * 
   * @param state true for activate, false deactivate
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void addTRS(boolean state) throws CommConnectionException, RFIDReaderException {
    addTRS = state;
    enableAdditionalTRS(addTRS);
  }

  /**
   * Sometimes you want to know the received signal strength when communicating with a transponder.
   * With the TRS setting the reader will automatically add the RSSI to responses from a tag. The
   * value is always negative in a range from -25 to -70 with -25 being the best case. The value
   * will be printed in a new line following the answer of the tag operation and following the EPC
   * if "SET EPC ON" ommand is used.
   * 
   * @param state true for activate, false deactivate
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void enableAdditionalTRS(boolean state)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "TRS", state ? "ON" : "OFF");
    if (response[0].equals(RESPONSE_OK)) {
      return;
    }
    handleUnexpectedResponse(response[0],
        "Show transponder receive strength: " + (state ? "ON" : "OFF"));
  }

  /**
   * Disable the access password
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void disableAccessPassword() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "ACP", "OFF");
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Disable access password");
  }

  /**
   * Sets the access password
   * 
   * @param password value of 32bit access code (8 characters long hexadecimal string)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setAccessPassword(String password)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "ACP", password);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set access password");
  }

  /**
   * Saves an access password
   * 
   * @param password Value of 32bit Access code (4 byte hexadecimal string)
   * @param slotNumber slot number [1,7]
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void saveAccessPassword(String password, int slotNumber)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "APS", password, slotNumber);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0],
        "Save access passort " + password + " to slot " + slotNumber);
  }

  /**
   * Loads an access password
   * 
   * @param slotNumber slot number
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void loadAccessPassword(int slotNumber)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "APL", slotNumber);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Load access password from slot " + slotNumber);
  }

  /**
   * Sets the kill password
   * 
   * @param password value of 32bit access code (4 byte hexadecimal string)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setKillPassword(String password) throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "KLP", password);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set kill password: " + password);
  }

  /**
   * Saves the kill password
   * 
   * @param password password
   * @param slotNumber slot number
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void saveKillPassword(String password, int slotNumber)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "KPS", password, slotNumber);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0],
        "Save kill password " + password + " on slot " + slotNumber);
  }

  /**
   * Loads a kill password
   * 
   * @param slotNumber slot number
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void loadKillPassword(int slotNumber) throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "KPL", slotNumber);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Load kill password from slot " + slotNumber);
  }

  /**
   * The Q-Value indicates the starting number of Slots for the tag searching used in every tag
   * command. The number of slots is 2^Q, so Q=0 is used for only one tag (see also SSL, which will
   * set Q=0 temporarily). The maximal value is 15, even though this will result in a timeout error
   * normally. The default value is four (16 Slots), which is fine for up to 8 tags. For more tags
   * change Q to five. Lower Q-Values will fasten the metraTec UHF Protocol Guide Page 20 of
   * 41search, so for 2 Tags Q=2 will be fine (in general: the number of channels should be much
   * bigger (about two times bigger) than the expected number of tags.
   * 
   * @param value value 0 &lt;= Q &lt;= 15
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setQValue(int value) throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SQV", value);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set Q value to " + value);
  }

  /**
   * The Q-Value indicates the starting number of Slots for the tag searching used in every tag
   * command. The number of slots is 2^Q, so Q=0 is used for only one tag (see also SSL, which will
   * set Q=0 temporarily). The maximal value is 15, even though this will result in a timeout error
   * normally. The default value is four (16 Slots), which is fine for up to 8 tags. For more tags
   * change Q to five. Lower Q-Values will fasten the metraTec UHF Protocol Guide Page 20 of
   * 41search, so for 2 Tags Q=2 will be fine (in general: the number of channels should be much
   * bigger (about two times bigger) than the expected number of tags.
   * 
   * @return the Q-Value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public int getQValue() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("RQV");
    if (response[0].length() <= 2)
      try {
        return Integer.parseInt(response[0], 10);
      } catch (NumberFormatException e) {
        throw new RFIDReaderException(RFIDErrorCodes.NER, response[0]);
      }
    handleUnexpectedResponse(response[0], "Get Q value");
    return -1; // <-- Can not happen! Eclipse Java parser does not recognize this
  }

  /**
   * Sets the number of retries in tag searching algorithm. Depending on the number of tags in the
   * field and the number of slots (Q-Value) there is the chance of a tag collision. Also, the tag
   * detection communication might be corrupted. Both cases leave a tag undetected but in detectable
   * state (arbitrate). In a new round the tag might be found. IR-Value sets how often a new round
   * can is started (it will not if there is no sign of a missing tag at all). The default value is
   * 2 for DwarfG2 and DeskID_UHF and it's 3 for Pulsar_MX.
   * 
   * @param value value, 0 &lt;= IR &lt;= 10
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setInventoryRetry(int value) throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SIR", value);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set inventory retry to " + value);
  }

  /**
   * Gets the number of retries in tag searching algorithm. Depending on the number of tags in the
   * field and the number of slots (Q-Value) there is the chance of a tag collision. Also, the tag
   * detection communication might be corrupted. Both cases leave a tag undetected but in detectable
   * state (arbitrate). In a new round the tag might be found. IR-Value sets how often a new round
   * can is started (it will not if there is no sign of a missing tag at all). The default value is
   * 2 for DwarfG2 and DeskID_UHF and it's 3 for Pulsar_MX.
   * 
   * @return the inventory retry value
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public int getInventoryRetry() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("RIR");
    if (response[0].length() <= 2)
      try {
        return Integer.parseInt(response[0], 10);
      } catch (NumberFormatException e) {
        throw new RFIDReaderException(RFIDErrorCodes.NER, response[0]);
      }
    handleUnexpectedResponse(response[0], "Get Q value");
    return -1; // <-- Can not happen! Eclipse Java parser does not recognize this
  }

  /**
   * The direct command is used to access tags with optional or manufacturer specific commands. It
   * is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags.
   * 
   * @param data Data
   * @param isSingleSlot single slot
   * @param withHandle true for with handel
   * @param withCRC true for with crc
   * @return List with the answers
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> directCommand(String data, boolean isSingleSlot, boolean withHandle,
      boolean withCRC) throws CommConnectionException, RFIDReaderException {
    return directCommand(data, -1, null, -1, isSingleSlot, withHandle, withCRC);
  }

  /**
   * The direct command is used to access tags with optional or manufacturer specific commands. It
   * is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags.
   * 
   * @param data data
   * @param bitLengthData Length of Data in Bit
   * @param isSingleSlot single slot
   * @param withHandle true for with handel
   * @param withCRC true for with crc
   * @return List with the answers
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> directCommand(String data, int bitLengthData, boolean isSingleSlot,
      boolean withHandle, boolean withCRC) throws CommConnectionException, RFIDReaderException {
    return directCommand(data, bitLengthData, null, -1, isSingleSlot, withHandle, withCRC);
  }

  /**
   * The direct command is used to access tags with optional or manufacturer specific commands. It
   * is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags.
   * 
   * @param data1 Data1
   * @param bitLengthData1 Length of Data 1 in Bit
   * @param data2 Data 2
   * @param bitLengthData2 Length of Data 2 in Bit
   * @param isSingleSlot single slot
   * @param withHandle true for with handel
   * @param withCRC true for with crc
   * @return List with the answers
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> directCommand(String data1, int bitLengthData1, String data2,
      int bitLengthData2, boolean isSingleSlot, boolean withHandle, boolean withCRC)
      throws CommConnectionException, RFIDReaderException {
    if (null == data1 || data1.isEmpty()) {
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "data 1 is null or empty");
    }
    inventoryIsEvent = false;
    try {
      return getTransponder(MEMBANK.USR, "DRC", isSingleSlot ? "SSL" : null, data1,
          bitLengthData1 != -1 ? bitLengthData1 : null, withHandle ? "H" : null,
          null != data2 && !data2.isEmpty() ? data2 : null,
          null != data2 && !data2.isEmpty() && bitLengthData1 != -1 ? bitLengthData1 : null);
    } finally {
      inventoryIsEvent = true;
    }
  }

  /**
   * The Kill Command can be used to disable UHF Gen2 Tags forever. To do this the kill password is
   * necessary (four bytes). The password is given via setKillPasswort(..) or is loaded from eeprom
   * via set loadKillPassword(..). It is possible to mask this command (using the setMask(...)
   * method) to limit this command to a certain population of tags.
   * 
   * @param isSingleSlot use Single Slot or not
   * @return List with an "OK!" for every successful Kill, can include Headerbit error (HBE) and
   *         Accesserror (ACE)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> killTag(boolean isSingleSlot)
      throws CommConnectionException, RFIDReaderException {
    inventoryIsEvent = false;
    try {
      return getTransponder(MEMBANK.EPC, "KIL", isSingleSlot ? "SSL" : null);
    } finally {
      inventoryIsEvent = true;
    }
  }



  /**
   * Used to set the access rights of the different data blocks, including the access password
   * itself and the kill password. <br>
   * To use this command you have to be in the secured state. It is possible to mask this command
   * (using the setMask(...) method) to limit this command to a certain population of tags.
   * 
   * @param membank memory type (EPC,TID,USR,ACP,KLP)
   * @param mode mode
   * @param ssl if true use single Slot
   * @return List with an "OK!" for every successful Lock, can include Headerbit error (HBE) and
   *         Accesserror (ACE)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> lockTag(MEMBANK membank, int mode, boolean ssl)
      throws CommConnectionException, RFIDReaderException {
    if (null == membank) {
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "membank is null");
    }

    inventoryIsEvent = false;
    try {
      return getTransponder(MEMBANK.EPC, "LCK", ssl ? "SSL" : null, membank.name(), mode);
    } finally {
      inventoryIsEvent = true;
    }
  }

  /**
   * Sets a new epc for a tag, tid is needed
   * 
   * @param newEPC new epc
   * @param tid TID
   * @return a list with the updated tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> setTagEPC(String newEPC, String tid)
      throws RFIDReaderException, CommConnectionException {
    if (null == newEPC || null == tid)
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "newEPC or tid is null");
    setMask(MEMBANK.TID, tid);
    try {
      return setTagEPC(newEPC);
    } catch (RFIDReaderException e) {
      throw e;
    } finally {
      setNoMask();
    }
  }

  /**
   * Sets a new EPC for the tag in the field
   * 
   * @param newEPC epc as hex
   * @return a list with the updated tags
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public List<UhfTag> setTagEPC(String newEPC) throws RFIDReaderException, CommConnectionException {
    // check if only single tag is in field
    if (0 != newEPC.length() % 4) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "EPC length must be multiple 4");
    }
    // prepare new data block 01 with the epc length
    int tagIDWords = newEPC.length() / 4;
    int block01 = tagIDWords / 2 << 12;
    if (1 == tagIDWords % 2) {
      block01 |= 0x0800;
    }
    // get the old block 01
    inventoryIsEvent = false;
    List<UhfTag> tags;
    try {
      tags = getTagData(MEMBANK.EPC, 1, 1);
    } finally {
      inventoryIsEvent = true;
    }
    if (tags.isEmpty()) {
      // no tags in field
      return new ArrayList<>();
    }
    // check if the oldBlock01 is all the same for the tags in field
    Integer oldBlock01 = null;
    for (UhfTag tag : tags) {
      int data = Integer.parseInt(tag.getData(), 16) & 0x7ff;
      if (null == oldBlock01) {
        oldBlock01 = data;
      } else if (!oldBlock01.equals(data)) {
        throw new RFIDReaderException(RFIDErrorCodes.CLD,
            "Different tags are in the field, which would result in data loss when writing. Please edit individually.");
      }
    }
    // copy old block data into the new block 01 data
    block01 |= oldBlock01;
    String block01Hex = Integer.toHexString(block01);
    if (4 > block01Hex.length()) {
      block01Hex = "0" + block01Hex;
    }
    tags = setTagData(MEMBANK.EPC, block01Hex + newEPC, 1, false);
    if (!tags.isEmpty()) {
      // the epc is written by at least one tag, reset the RF field to also reset the tags
      resetRFInterface(50);
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
      }
    }
    return tags;
  }

  /**
   * This method is only usable on DwarfG2, DwarfG2_XR and PulsarMX (a DeskID has no IOs). It makes
   * the reader set and reset an output if at least one tag is found a round (always when there is a
   * IVF &gt;= 1). This works for INV, RDT and so on. Keep in mind: In case of a write it shows NOT
   * a successful writing but just the tag found. The used IO is: GPIO 7 for DwarfG2 and GPO 0 for
   * PulsarMX.
   * 
   * @param highTimeMS pin high time in milliseconds
   * @param lowTimeMS pin low time in milliseconds
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void enableHighOnTag(int highTimeMS, int lowTimeMS)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "HOT", highTimeMS, lowTimeMS);
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0],
        "Enable high pin on tag. High time: " + highTimeMS + " Low Time: ");
  }

  /**
   * Disable the high pin on tag mode
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void disableHighOnTag() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "HOT", "OFF");
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Disable high pin on tag");
  }

  /**
   * This method is only usable on DwarfG2 and PulsarMX (a DeskID has no IOs). It makes the reader
   * call commands on a falling edge of an IO. Supported are pins 0 and 1 (GPI 0/1 on Pulsar, GPIO
   * 0/1 on DwarfG2) with independent state and buffer.
   * 
   * @param pin pin to use
   * @param commands list with commands to execute (ASCII protocol commands)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setExecuteCommandsOnInput(int pin, String... commands)
      throws CommConnectionException, RFIDReaderException {
    StringBuilder tmpBuffer = new StringBuilder();
    for (String command : commands) {
      tmpBuffer.append(command);
      tmpBuffer.append(';');
    }
    tmpBuffer.setLength(tmpBuffer.length() - 1);
    String[] response = communicateSynchronized("SET", "IHC", pin, tmpBuffer.toString());
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set execute commands on pin");
  }

  /**
   * Enable the execute commands on input pin
   * 
   * @param pin pin
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void enableExecuteCommandsOnInput(int pin)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "IHC", pin, "ON");
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Enable execute commands on pin " + pin);
  }

  /**
   * Disable the execute commands on input pin
   * 
   * @param pin pin
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void disableExecuteCommandsOnInput(int pin)
      throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SET", "IHC", pin, "OFF");
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Disable execute commands on pin " + pin);
  }

  /**
   * Get the configured commands on input
   * 
   * @param pin pin
   * @return list with the commands * @throws CommConnectionException if an communication exception
   *         occurs
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public String[] getExecuteCommandsOnInput(int pin)
      throws CommConnectionException, RFIDReaderException {
    return communicateSynchronized("SET", "IHC", pin, "SHW");
  }

  /**
   * Sets a number of start up commands. The commands are persistant saved in non volatile memory.
   * The commands are loaded, then parsed and executed as descriped. Even though the executed
   * commands send no answer of any kind. Errors in the commands normally causing UCO or UPA are not
   * detected. So please check the commands for right spelling. To activate please call
   * {@link UHFReader#enableStartUpCommands()}.
   * 
   * @param commands list with commands to execute (ASCII protocol commands)
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void setStartUpCommands(String... commands)
      throws CommConnectionException, RFIDReaderException {
    StringBuilder tmpBuffer = new StringBuilder();
    for (String command : commands) {
      tmpBuffer.append(command);
      tmpBuffer.append(';');
    }
    tmpBuffer.setLength(tmpBuffer.length() - 1);
    String[] response = communicateSynchronized("SUC", tmpBuffer.toString());
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Set start up commands");
  }

  /**
   * Enable the start up commands
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void enableStartUpCommands() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SUC", "ON");
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Enable start up commands");
  }

  /**
   * Disable the start up commands
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void disableStartUpCommands() throws CommConnectionException, RFIDReaderException {
    String[] response = communicateSynchronized("SUC", "OFF");
    if (response[0].equals(RESPONSE_OK))
      return;
    handleUnexpectedResponse(response[0], "Disable start up commands on pin");
  }

  /**
   * Get the configured start up commands
   * 
   * @return list with the commands * @throws CommConnectionException if an communication exception
   *         occurs
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public String[] getStartUpCommands() throws CommConnectionException, RFIDReaderException {
    return communicateSynchronized("RSC");
  }

  /**
   * calculate the crc
   * 
   * @param data data (one byte)
   * @param length lenght of used bits
   * @param crc initial crc
   * @return new crc
   */
  private int getCRC16(byte data, int length, int crc) {
    for (int i = 0; i < length; i++) {
      crc <<= 1;
      if (0 != (((data >>> (7 - i)) ^ (crc >>> 16)) & 0x01)) {
        crc ^= 0x1021; // polynom 0001 0000 0010 0001 (0, 5, 12)
      }
    }
    return crc & 0xFFFF;
  }

  /**
   * checks the crc16 from the DirectCommand answer. The CRC16 is not check by the reader because
   * the data length are not known.
   * 
   * @param data hex data from the direct command
   * @param dataBitLength bitlength of the data (include headerbit,data,handle and crc)
   * @return true if the crc is correct, else false
   * @throws RFIDReaderException possible RFIDErrorCodes:
   *         <ul>
   *         <li>WPA, data are not hex data</li>
   *         <li>WDL, data bit length is bigger then the data size or the hex data have a wrong
   *         length</li>
   *         </ul>
   */
  public boolean checkCRC16FromDirectCommand(String data, int dataBitLength)
      throws RFIDReaderException {
    byte[] arr;
    int length = dataBitLength / 8;
    int rest = dataBitLength % 8;
    arr = getByteFromHexString(data);
    if (arr.length < (length + ((rest != 0) ? 1 : 0))) {
      throw new RFIDReaderException(RFIDErrorCodes.WDL, "dataBitLenght bigger then data size");
    }
    int crc = 0xFFFF;
    for (int i = 0; i < length; i++) {
      crc = getCRC16(arr[i], 8, crc);
    }
    if (rest != 0) {
      crc = getCRC16(arr[length], rest, crc);
    }
    crc &= 0xFFFF;
    logger.trace("CRC16-CCITT = " + Integer.toHexString(crc));
    if (crc == 0x1d0f) {
      // CRC16 is correct
      return true;
    } else if (crc == 0xd2e) {
      // CRC16 ist maybe correct...is correct only the last bit of the received crc is wrong...
      return true;
    } else {
      // CRC16 is wrong
      return false;
    }

  }

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  @Override
  public void scanInventory() throws CommConnectionException, RFIDReaderException {
    scanInventory(false, false, false);
  }

  /**
   * Looks for all tags in range of the reader and call events with founded tags.
   * 
   * @param singSlot Single Slot (sets Q and IR values to zero for this round)
   * @param onlyNewTag This flag causes the reader to not reset the state of tags via a select
   *        command. Under normal conditions, this causes the tags to be found only once as long as
   *        they are not depowered or reset.
   * @param secure This flag causes the reader to bring the tag to secured / open mode. It may help
   *        if you need to be sure to find no tag more than once (as long as the tags stay pow-
   *        ered). If this flag is not set it is possible under certain circumstances that tags are
   *        found multiple times within one inventory cycle.
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void scanInventory(boolean singSlot, boolean onlyNewTag, boolean secure)
      throws CommConnectionException, RFIDReaderException {
    if (null != lastInventoryCall) {
      throw new RFIDReaderException(RFIDErrorCodes.BSY, "Reader is already scanning for tags");
    }
    lastInventoryCall = MEMBANK.EPC;
    send("CNR INV", singSlot ? "SSL" : null, onlyNewTag ? "ONT" : null, secure ? "SEC" : null);
  }

  /**
   * Reads the data from the tags.<br>
   * It is possible to mask this command (using the setMask(...) method) to limit this command to a
   * certain population of tags. If addEPC and/or addTRS is set, the List with data contains also
   * the EPC and/or the TRS (first entry data, second entry EPC, third entry TRS, and so on).
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param words number of words to read (0 for all)
   * @param ssl true for use single slot
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of
   *         range, ..)
   */
  public void scanTagData(MEMBANK membank, int startAddress, int words, boolean ssl)
      throws RFIDReaderException, CommConnectionException {
    if (null != lastInventoryCall) {
      throw new RFIDReaderException(RFIDErrorCodes.BSY, "Reader is already scanning for tags");
    }
    lastInventoryCall = membank;
    dataStartAddress = startAddress;
    send("CNR RDT", ssl ? "SSL" : null, membank.name(), Integer.toHexString(startAddress),
        Integer.toHexString(words));

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.metratec.lib.rfidreader.StandardReader#stopInventory()
   */
  @Override
  public List<UhfTag> stopInventory() throws CommConnectionException, RFIDReaderException {
    if (null != lastInventory) {
      List<UhfTag> tags = super.stopInventory();
      lastInventoryCall = null;
      return tags;
    } else {
      return new ArrayList<>();
    }
  }


}
