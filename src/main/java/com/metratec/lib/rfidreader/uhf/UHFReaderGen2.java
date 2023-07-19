package com.metratec.lib.rfidreader.uhf;

import java.util.ArrayList;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.MetratecReaderGen2;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.tag.UhfTag;

public class UHFReaderGen2 extends MetratecReaderGen2<UhfTag> {

  private UHFInventorySetting inventorySetting = null;

  /**
   * the available uhf regions
   */
  public enum REGION {
    ETSI, ETSI_HIGH, FCC
  }

  /** Enum for the UHF Tag membank */
  public enum MEMBANK {
    /** The EPC membank. Contains CRC, PC and EPC. */
    EPC,
    /**
     * The tag ID of the tag (sometimes contains a unique ID, sometimes only a manufacturer code, depending on the tag
     * type)
     */
    TID,
    /** The optional user memory some tags have */
    USR,
    /** Protocol control */
    PC,
  }

  public UHFReaderGen2(String identifier, ICommConnection connection) {
    super(identifier, connection);
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    stopInventory();
    stopInventoryReport();
    getInventorySettings();
    // try {
    // checkAntennas();
    // } catch (RFIDReaderException e) {
    // return "Warning: " + e.getMessage();
    // }
    return message;
  }

  /**
   * 
   * @param duration the report execution duration
   * @return the founded tags as list
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * 
   */
  public List<UhfTag> getInventoryReport(Integer duration) throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+INVR", duration);
    // #+INVR:
    List<UhfTag> tags = parseInventoryReport(splitResponse(resp), 7);
    addNewInventoryEvent(tags);
    return tags;
  }

  /**
   * parse the inventory response
   * 
   * @param answers reader answers
   * @param timestamp timestamp
   * @return a {@link List} with {@link UhfTag}s
   * @throws RFIDReaderException if an error occurs
   */
  @Override
  protected List<UhfTag> parseInventory(String[] answers, int prefix_length, boolean throwError)
      throws RFIDReaderException {
    return parseInventory(answers, prefix_length, false, throwError);
  }

  /**
   * parse the inventory response
   * 
   * @param answers reader answers
   * @param prefix_length answer prefix length
   * @param isReport true if the inventory is an report (with tag count)
   * @param throwError true to throw an error if an antenna has a problem
   * @return a {@link List} with {@link UhfTag}s
   * @throws RFIDReaderException if an error occurs
   */
  protected List<UhfTag> parseInventory(String[] answers, int prefix_length, boolean isReport, boolean throwError)
      throws RFIDReaderException {
    // +CINV: 3034257BF468D480000003EC,E200600311753E33,1755 +CINV: <ROUND FINISHED, ANT=2>
    // +INV: 0209202015604090990000145549021C,E200600311753F23,1807
    // available messages: <Antenna Error> <NO TAGS FOUND> <ROUND FINISHED, ANT=2>
    long timestamp = System.currentTimeMillis();
    List<UhfTag> tags = new ArrayList<>();
    String error = null;
    Integer antenna = null;
    for (String tagInfo : answers) {
      if (tagInfo.charAt(0) != '+') {
        continue;
      }
      String[] split = splitLine(tagInfo.substring(prefix_length));
      if (split[0].charAt(0) == '<') {
        // message
        switch (split[0].charAt(1)) {
          case 'R': // Round finished
            antenna = Integer.parseInt(split[1].substring(5, split[1].length() - 1));
            for (UhfTag tag : tags) {
              tag.setAntenna(antenna);
            }
            break;
          case 'N': // No Tags
            break;
          default:
            if (throwError) {
              error = split[0].substring(1, split[0].length() - 1);
            }
        }
        continue;
      }
      try {
        UhfTag tag = new UhfTag(split[0], timestamp, getCurrentAntennaPort());
        if (inventorySetting.withTid()) {
          tag.setTid(split[1]);
        }
        if (inventorySetting.withRssi()) {
          tag.setRssi(Integer.parseInt(split[inventorySetting.withTid() ? 2 : 1]));
        }
        if (isReport) {
          tag.setSeenCount(Integer.parseInt(split[split.length - 1]));
        }
        tags.add(tag);
      } catch (Exception e) {
        if (null == inventorySetting) {
          // not initialised - ignore
          return new ArrayList<>();
        }
        getLogger().warn("Inventory warning {}", tagInfo, getLogger().isDebugEnabled() ? e : null);
      }

    }
    if (null != error) {
      throw new RFIDReaderException(RFIDErrorCodes.ARH, String.format("%s %s", error, null != antenna ? antenna : ""));
    }
    if (isReport) {
      for (UhfTag tag : tags) {
        tag.setAntenna(null);
      }
    }
    return tags;
  }


  /**
   * parse the inventory response
   * 
   * @param answers reader answers
   * @param timestamp timestamp
   * @return a {@link List} with {@link UhfTag}s
   * @throws RFIDReaderException if an error occurs
   */
  @Override
  protected List<UhfTag> parseInventoryReport(String[] answers, int prefix_length) throws RFIDReaderException {
    return parseInventory(answers, prefix_length, true, false);
  }

  /**
   * 
   * @return the current ufh inventory settings {@link} {@link UHFInventorySetting}}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public UHFInventorySetting getInventorySettings() throws CommConnectionException, RFIDReaderException {
    if (null != inventorySetting) {
      return inventorySetting;
    }
    String data = communicateSynchronized("AT+INVS?");
    // +INVS: 0,0,0
    try {
      inventorySetting = new UHFInventorySetting(data.charAt(7) == '1', data.charAt(9) == '1', data.charAt(11) == '1');
      return inventorySetting;
    } catch (IndexOutOfBoundsException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, data);
    }
  }

  /**
   * Set the uhf inventory settings
   * 
   * @param settings {@link UHFInventorySetting}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setInventorySettings(UHFInventorySetting settings) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+INVS", settings.onlyNewTag() ? "1" : "0", settings.withRssi() ? "1" : "0",
        settings.withTid() ? "1" : "0");
    this.inventorySetting = settings;
  }

  // /**
  // * @return the rfid power
  // * @throws CommConnectionException if an communication error occurs
  // * @throws RFIDReaderException if an reader error occurs
  // */
  // public int getPower() throws CommConnectionException, RFIDReaderException {
  // String power = communicateSynchronized("AT+PWR?");
  // // +PWR: 12
  // return Integer.parseInt(power.substring(6));
  // }

  // /**
  // *
  // * @param power the rfid power to set
  // * @throws CommConnectionException if an communication error occurs
  // * @throws RFIDReaderException if an reader error occurs
  // */
  // public void setPower(int power) throws CommConnectionException, RFIDReaderException {
  // // if(0 > power || power > 30){
  // // throw new RFIDReaderException(RFIDErrorCodes.WPA, "power [0..30]");
  // // }
  // communicateSynchronized("AT+PWR", power);
  // }

  /**
   * Sets the uhf region
   * 
   * @param region {@link REGION}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setRegion(REGION region) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+REG", region.name());
  }

  /**
   * @return the current ufh region
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public REGION getRegion() throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+REG?");
    // +REG: ETSI_LOWER
    return REGION.valueOf(resp.substring(6));
  }

  /**
   * 
   * @return the current tag size settings {@link} {@link UHFTagSizeSetting}}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public UHFTagSizeSetting getTagSize() throws CommConnectionException, RFIDReaderException {

    String data = communicateSynchronized("AT+Q?");
    // +Q: 4,2,15
    try {
      String[] values = splitLine(data.substring(4));

      UHFTagSizeSetting setting = new UHFTagSizeSetting((int) Math.pow(2, Integer.valueOf(values[0])));
      if (values.length > 1) {
        setting.setMin((int) Math.pow(2, Integer.valueOf(values[1])));
        setting.setMax((int) Math.pow(2, Integer.valueOf(values[2])));
      }
      return setting;
    } catch (IndexOutOfBoundsException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, data);
    }
  }

  /**
   * Set the tag size
   * 
   * @param setting {@link UHFTagSizeSetting}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setTagSize(UHFTagSizeSetting setting) throws CommConnectionException, RFIDReaderException {

    // int value = 1;
    // Integer start = 0;
    // while (setting.getStart() > value) {
    // start++;
    // value <<= 1;
    // }
    Integer start = 0;
    while (setting.getStart() >= Math.pow(2, start)) {
      start++;
    }
    start--;

    Integer min = 0;
    if (null != setting.getMin()) {
      while (setting.getMin() >= Math.pow(2, min)) {
        min++;
      }
      min--;
    } else {
      min = null;
    }
    Integer max = 0;
    if (null != setting.getMax()) {
      while (setting.getMax() > Math.pow(2, max)) {
        max++;
      }
    } else {
      max = null;
    }
    communicateSynchronized("AT+Q", start, min, max);
  }

  /**
   * Reads the TID from the tag.<br>
   * 
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> getTagTIDs() throws CommConnectionException, RFIDReaderException {
    return getTagTIDs(4, null);
  }

  /**
   * Reads the TID from the tag with a predefined length.<br>
   * 
   * @param length length of TID words
   * @param epcMask the epc mask
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> getTagTIDs(int length, String epcMask) throws CommConnectionException, RFIDReaderException {
    return getTagData(MEMBANK.TID, 0, length, epcMask);
  }

  /**
   * Reads the data from the tags.
   * 
   * @param startAddress start address
   * @param length number of words to read (0 for all) - word == 4 byte
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> getTagData(int startAddress, int length) throws CommConnectionException, RFIDReaderException {
    return getTagData(startAddress, length, null);
  }

  /**
   * Reads the data from the tags.
   * 
   * @param startAddress start address
   * @param length number of words to read (0 for all) - word == 4 byte
   * @param epcMask the epc mask
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> getTagData(int startAddress, int length, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    return getTagData(MEMBANK.USR, startAddress, length, epcMask);
  }

  /**
   * Reads a memory from the tags.<br>
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param length number of words to read (0 for all) - word == 4 byte
   * @param epcMask epc mask, ascii encode n*4 length
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> getTagData(MEMBANK membank, int startAddress, int length, String epcMask)
      throws RFIDReaderException, CommConnectionException {
    if (null == membank) {
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "membank is null");
    }
    String resp = communicateSynchronized("AT+READ", membank.name(), startAddress, length, epcMask);
    // +READ: 3034257BF468D480000003EE,OK,0000
    List<UhfTag> tags = new ArrayList<>();
    for (String tagInfo : splitResponse(resp)) {
      String[] values = splitLine(tagInfo.substring(7));
      UhfTag tag = new UhfTag(values[0]);
      if (values[1].equals("OK")) {
        switch (membank) {
          case USR:
            tag.setData(values[2]);
            break;
          case TID:
            tag.setTid(values[2]);
            break;
          case EPC:
          default:
            break;
        }
      } else {
        tag.setHasError(true);
        tag.setMessage(values[1]);
      }
      tags.add(tag);
    }
    return tags;
  }

  /**
   * Set the tag epc
   * 
   * @param tid tag id
   * @param startAddress start address
   * @param epc new epc, length must by multiple of 4
   * @return List with processed transponders - contains the old EPCs
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> setTagEpc(String tid, int startAddress, String epc)
      throws RFIDReaderException, CommConnectionException {
    try {
      setMask(MEMBANK.TID, 0, tid);
      return setTagData(MEMBANK.EPC, startAddress, epc, null);
    } finally {
      resetMask();
    }
  }

  /**
   * Set the tag epc
   * 
   * @param startAddress start address
   * @param epc new epc, length must by multiple of 4
   * @return List with processed transponders - contains the old EPCs
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> setTagEpc(int startAddress, String epc) throws RFIDReaderException, CommConnectionException {
    return setTagData(MEMBANK.EPC, startAddress, epc, null);
  }

  /**
   * Set the tag data
   * 
   * @param startAddress start address
   * @param data hex data to write, length must by multiple of 4
   * @param epcMask Optional, the epc mask to use
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> setTagData(int startAddress, String data, String epcMask)
      throws RFIDReaderException, CommConnectionException {
    return setTagData(MEMBANK.USR, startAddress, data, epcMask);
  }

  /**
   * Set the tag data
   * 
   * @param startAddress start address
   * @param data hex data to write, length must by multiple of 4
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> setTagData(int startAddress, String data) throws RFIDReaderException, CommConnectionException {
    return setTagData(startAddress, data, null);
  }

  /**
   * Write a memory from the tags.<br>
   * 
   * @param membank MEMBANK (EPC,USR,TID,RES)
   * @param startAddress start address
   * @param data hex data to write, length must by multiple of 4
   * @param epcMask Optional, the epc mask to use
   * @return List with processed transponders
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> setTagData(MEMBANK membank, int startAddress, String data, String epcMask)
      throws RFIDReaderException, CommConnectionException {
    if (null == membank) {
      throw new RFIDReaderException(RFIDErrorCodes.NUL, "membank is null");
    }
    if (membank.equals(MEMBANK.TID)) {
      throw new RFIDReaderException(RFIDErrorCodes.WPA, "TID is not writeable");
    }
    String resp = communicateSynchronized("AT+WRT", membank.name(), startAddress, data, epcMask);
    return parseTagResponse(resp, 6, System.currentTimeMillis());
  }

  /**
   * Parsing the transponder responses. Used when the response list contains only the epc and the response code
   * 
   * @param response reader response
   * @param prefixLength response command prefix length - len("+PREFIX=")
   * @param timestamp response timestamp
   * @return {@link List} with handled {@link UhfTag}s
   */
  private List<UhfTag> parseTagResponse(String response, int prefixLength, Long timestamp) {
    List<UhfTag> tags = new ArrayList<>();
    for (String tagInfo : splitResponse(response)) {
      if (response.charAt(prefixLength) == '<') {
        if (response.charAt(prefixLength + 1) == 'N') {
          // No tags found
          continue;
        }
        continue;
      }
      String[] values = splitLine(tagInfo.substring(prefixLength));
      UhfTag tag = new UhfTag(values[0], timestamp);
      if (!values[1].equals("OK")) {
        tag.setHasError(true);
        tag.setMessage(values[1]);
      }
      tags.add(tag);
    }
    return tags;
  }

  /**
   * Set a reader mask
   * 
   * @param membank the memory to check
   * @param startAddress the start address
   * @param mask the mask
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void setMask(MEMBANK membank, int startAddress, String mask)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+MSK", membank, startAddress, mask);
  }

  /**
   * Set a reader epc mask
   * 
   * @param mask the epc mask
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void setEpcMask(String mask) throws CommConnectionException, RFIDReaderException {
    setMask(MEMBANK.EPC, 0, mask);
  }

  /**
   * Resets the reader mask
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void resetMask() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+MSK", "OFF");
  }

  /**
   * Set a reader mask
   * 
   * @param membank the memory to check
   * @param startAddress the start address
   * @param mask the binary mask, e.g. '0110'
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void setBitmask(MEMBANK membank, int startAddress, String mask)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+BMSK", membank, startAddress, mask);
  }

  /**
   * Resets the reader bitmask
   * 
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public void resetBitmask() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+BMSK", "OFF");
  }

  /**
   * Kill tags
   * 
   * @param password the kill password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the kill was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> killTag(String password, String epcMask) throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+KILL", password, epcMask);
    // +KILL: ABCD01237654321001234567,ACCESS ERROR<CR><LF>
    return parseTagResponse(resp, 7, System.currentTimeMillis());
  }

  /**
   * Kill tags
   * 
   * @param password the kill password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the kill was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> killTag(String password) throws CommConnectionException, RFIDReaderException {
    return killTag(password, null);
  }

  /**
   * Lock a tag memory
   * 
   * @param membank the memory to lock
   * @param password the memory password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTag(MEMBANK membank, String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+LCK", membank.name(), password, epcMask);
    // +LCK: ABCD01237654321001234567,ACCESS ERROR<CR><LF>
    return parseTagResponse(resp, 6, System.currentTimeMillis());
  }

  /**
   * Lock a tag data
   * 
   * @param password the data password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagData(String password, String epcMask) throws CommConnectionException, RFIDReaderException {
    return lockTag(MEMBANK.USR, password, epcMask);
  }

  /**
   * Lock a tag data
   * 
   * @param password the data password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagData(String password) throws CommConnectionException, RFIDReaderException {
    return lockTagData(password, null);
  }

  /**
   * Lock a tag epc
   * 
   * @param password the password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagEpc(String password, String epcMask) throws CommConnectionException, RFIDReaderException {
    return lockTag(MEMBANK.EPC, password, epcMask);
  }

  /**
   * Lock a tag epc
   * 
   * @param password the password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagEpc(String password) throws CommConnectionException, RFIDReaderException {
    return lockTagEpc(password, null);
  }

  /**
   * Lock a tag memory permament
   * 
   * @param membank the memory to lock
   * @param password the memory password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagPermament(MEMBANK membank, String password)
      throws CommConnectionException, RFIDReaderException {
    return lockTagPermament(membank, password, null);
  }

  /**
   * Lock a tag memory permament
   * 
   * @param membank the memory to lock
   * @param password the memory password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagPermament(MEMBANK membank, String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+PLCK", membank.name(), password, epcMask);
    // +PLCK: ABCD01237654321001234567,ACCESS ERROR<CR><LF>
    return parseTagResponse(resp, 7, System.currentTimeMillis());
  }

  /**
   * Lock a tag user data permament
   * 
   * @param password the memory password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagMemoryPermament(String password) throws CommConnectionException, RFIDReaderException {
    return lockTagMemoryPermament(password, null);
  }

  /**
   * Lock a tag user data permament
   * 
   * @param password the memory password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagMemoryPermament(String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    return lockTagPermament(MEMBANK.USR, password, epcMask);
  }

  /**
   * Lock a tag epc permament
   * 
   * @param password the memory password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagEpcPermament(String password) throws CommConnectionException, RFIDReaderException {
    return lockTagEpcPermament(password, null);
  }

  /**
   * Lock a tag epc permament
   * 
   * @param password the memory password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the lock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> lockTagEpcPermament(String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    return lockTagPermament(MEMBANK.EPC, password, epcMask);
  }

  /**
   * Unlock a tag memory
   * 
   * @param membank the memory to unlock
   * @param password the memory password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the unlock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> unlockTag(MEMBANK membank, String password) throws CommConnectionException, RFIDReaderException {
    return unlockTag(membank, password, null);
  }

  /**
   * Unlock a tag memory
   * 
   * @param membank the memory to unlock
   * @param password the memory password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the unlock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> unlockTag(MEMBANK membank, String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+ULCK", membank.name(), password, epcMask);
    // +ULCK: ABCD01237654321001234567,ACCESS ERROR<CR><LF>
    return parseTagResponse(resp, 7, System.currentTimeMillis());
  }

  /**
   * Unlock a tag data
   * 
   * @param password the data password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the unlock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> unlockTagData(String password) throws CommConnectionException, RFIDReaderException {
    return unlockTagData(password, null);
  }

  /**
   * Unlock a tag data
   * 
   * @param password the data password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the unlock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> unlockTagData(String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    return unlockTag(MEMBANK.USR, password, epcMask);
  }

  /**
   * Unlock a tag epc
   * 
   * @param password the memory password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the unlock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> unlockTagEpc(String password) throws CommConnectionException, RFIDReaderException {
    return unlockTagEpc(password, null);
  }

  /**
   * Unlock a tag epc
   * 
   * @param password the memory password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the unlock was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> unlockTagEpc(String password, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    return unlockTag(MEMBANK.EPC, password, epcMask);
  }

  /**
   * Change/Set the kill password
   * 
   * @param oldPassword the old password
   * @param newPassword the new password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the password change was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> changeKillPassword(String oldPassword, String newPassword)
      throws CommConnectionException, RFIDReaderException {
    return changeKillPassword(oldPassword, newPassword, null);
  }

  /**
   * Change/Set the kill password
   * 
   * @param oldPassword the old password
   * @param newPassword the new password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the password change was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> changeKillPassword(String oldPassword, String newPassword, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+PWD", "KILL", oldPassword, newPassword, epcMask);
    // +PWD: ABCD01237654321001234567,ACCESS ERROR<CR><LF>
    return parseTagResponse(resp, 6, System.currentTimeMillis());
  }

  /**
   * Change/Set the lock password
   * 
   * @param oldPassword the old password
   * @param newPassword the new password
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the password change was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> changeLockPassword(String oldPassword, String newPassword)
      throws CommConnectionException, RFIDReaderException {
    return changeLockPassword(oldPassword, newPassword, null);
  }

  /**
   * Change/Set the lock password
   * 
   * @param oldPassword the old password
   * @param newPassword the new password
   * @param epcMask optional, the epc mask to use
   * @return {@link List} with handled {@link UhfTag}s. If the tag has error, the password change was not successful
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public List<UhfTag> changeLockPassword(String oldPassword, String newPassword, String epcMask)
      throws CommConnectionException, RFIDReaderException {
    String resp = communicateSynchronized("AT+PWD", "LCK", oldPassword, newPassword, epcMask);
    // +PWD: ABCD01237654321001234567,ACCESS ERROR<CR><LF>
    return parseTagResponse(resp, 6, System.currentTimeMillis());
  }

  /**
   * Send a custom command
   * 
   * @param command the reader command
   * @param parameters the command parameters
   * @return the reader response
   * @throws CommConnectionException if an communication exception occurs
   * @throws RFIDReaderException if an protocol exception occurs (e.g. CRC error, value out of range, ..)
   */
  public String executeCommand(String command, Object... parameters)
      throws CommConnectionException, RFIDReaderException {
    return communicateSynchronized(command, parameters);
  }
}
