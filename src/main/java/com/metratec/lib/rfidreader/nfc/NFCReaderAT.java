package com.metratec.lib.rfidreader.nfc;

import java.util.ArrayList;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.MetratecReaderAT;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.RFIDTransponderException;
import com.metratec.lib.rfidreader.nfc.NTagMirrorConfig.MirrorMode;
import com.metratec.lib.tag.HfTag;
import com.metratec.lib.tag.ISO14ATag;
import com.metratec.lib.tag.ISO15Tag;

/**
 * Base class for the metratec NFC reader based on the AT protocol
 */
public class NFCReaderAT extends MetratecReaderAT<HfTag> {

  /**
   * NFC Reader modes
   */
  public enum NFCReaderMode {
    /** Automatic mode, detect iso15 and iso14a transponder */
    AUTO,
    /** Iso15 mode, needed for execute iso15 tag commands */
    ISO15,
    /** Iso14a mode, needed for execute iso14a tag commands */
    ISO14A
  }
  /** Used RF interface sub carrier */
  public enum SubCarrier {
    /** Single mode */
    SINGLE,
    /** Double Mode */
    DOUBLE
  }

  /** Used RF interface modulation depth */
  public enum ModulationDepth {
    /** Modulation depth 10 */
    Depth10,
    /** Modulation depth 100 */
    Depth100
  }

  /** Mifare Classic Key Type */
  public enum KeyType {
    /** Key A */
    A,
    /** Key B */
    B
  }

  private NFCInventorySetting currentInventorySettings;
  private NFCReaderMode currentMode;
  private String currentSelectedTag = null;

  /**
   * Create a new instance
   * 
   * @param identifier the reader identifier
   * @param connection the reader connection
   */
  public NFCReaderAT(String identifier, ICommConnection connection) {
    super(identifier, connection);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // RFID Settings
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Enable the rf interface
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void enableRfInterface() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+CW", 1);
  }

  /**
   * Disable the rf interface
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void disableRfInterface() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+CW", 0);
  }

  /**
   * Set the reader mode
   * 
   * @param mode the reader mode
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setReaderMode(NFCReaderMode mode) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+MOD", mode.name());
    currentMode = mode;
  }

  /**
   * Get the reader mode
   * 
   * @return the current reader mode
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public NFCReaderMode getReaderMode() throws CommConnectionException, RFIDReaderException {
    String response = communicateSynchronized("AT+MOD?");
    // +MOD= ISO14A
    NFCReaderMode mode = NFCReaderMode.valueOf(response.substring(6));
    currentMode = mode;
    return mode;
  }

  /**
   * Configure the rf interface
   * 
   * @param subCarrier the sub carrier to use
   * @param modulationDepth the modulation depth to use
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setRfInterfaceConfig(SubCarrier subCarrier, ModulationDepth modulationDepth)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+CRI", subCarrier, modulationDepth == ModulationDepth.Depth10 ? 10 : 100);
  }

  /**
   * Get the current rf interface subcarrier
   * 
   * @return the current rf subcarrier
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public SubCarrier getRfInterfaceSubCarrier() throws CommConnectionException, RFIDReaderException {
    String[] response = splitResponse(communicateSynchronized("AT+CRI?").substring(6));
    // +CRI: SINGLE,100
    return SubCarrier.valueOf(response[0]);
  }

  /**
   * Get the current rf interface modulation depth
   * 
   * @return the current rf interface modulation depth
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public ModulationDepth getRfInterfaceModulationDepth() throws CommConnectionException, RFIDReaderException {
    String[] response = splitResponse(communicateSynchronized("AT+CRI?").substring(6));
    // +CRI: SINGLE,100
    return response[1].contains("100") ? ModulationDepth.Depth100 : ModulationDepth.Depth10;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Tag Operation
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Configure the reader inventory settings
   * 
   * @param settings the {@link NFCInventorySetting}
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setInventorySettings(NFCInventorySetting settings) throws CommConnectionException, RFIDReaderException {
    if (null == settings) {
      return;
    }
    communicateSynchronized("AT+INVS", settings.isWithTagDetails() ? "1" : "0", settings.isOnlyNewTags() ? "1" : "0",
        settings.isSingleSlot() ? "1" : "0");
    currentInventorySettings = settings;
  }

  /**
   * Read data from the card's memory. Depending on the protocol a select and authenticate is needed prior to this
   * command.
   * 
   * @param block
   * @return the block to read
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String readBlock(int block) throws CommConnectionException, RFIDReaderException {
    String response = communicateSynchronized("AT+READ", block);
    // +READ: 01020304
    return response.substring(7);
  }

  /**
   * Read data from the card's memory. Depending on the protocol a select and authenticate is needed prior to this
   * command.
   * 
   * @param startBlock the start block to read
   * @param numberOfBlocks number of blocks to read
   * @return the read data
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String readMultipleBlocks(int startBlock, int numberOfBlocks)
      throws CommConnectionException, RFIDReaderException {
    String response = communicateSynchronized("AT+READM", startBlock, numberOfBlocks);
    String data = "";
    for (String line : splitResponse(response)) {
      data += line.substring(8);
    }
    return data;
  }

  /**
   * Write data to a block of the tags memory.
   * 
   * @param block number of the block to write
   * @param hexData hex data to write to the card
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeBlock(int block, String hexData) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+WRT", block, hexData);
  }

  /**
   * Write data to a block of the tags memory. It is assumed that the block size of the transponder is 4 bytes.
   * 
   * @param startBlock number of the start block to write
   * @param hexData hex data to write to the card
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeMultipleBlocks(int startBlock, String hexData) throws CommConnectionException, RFIDReaderException {
    writeMultipleBlocks(startBlock, hexData, 4);
  }

  /**
   * Write data to a block of the tags memory.
   * 
   * @param startBlock number of the start block to write
   * @param hexData hex data to write to the card
   * @param blockSize block size of the transponder in bytes
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeMultipleBlocks(int startBlock, String hexData, int blockSize)
      throws CommConnectionException, RFIDReaderException {
    int dataSize = blockSize * 2;
    if (hexData.length() % dataSize != 0) {
      throw new RFIDTransponderException("The data must be a multiple of the block size of " + blockSize);
    }
    int numberOfBlocks = hexData.length() / dataSize;
    for (int i = 0; i < numberOfBlocks; i++) {
      int retry = 2;
      for (int n = 0; n < retry; n++) {
        try {
          writeBlock(startBlock + i, hexData.substring(dataSize * i, dataSize * (1 + i)));
          break;
        } catch(RFIDReaderException e) {
          if(n >= retry) {
            throw e;
          }
        } 
      }
    }
  }

  /**
   * Select a tag by its TID
   * 
   * @param tid the transponder tid
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void selectTag(String tid) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+SEL", tid);
    currentSelectedTag = tid;
  }

  /**
   * Deselect the current selected tag.
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void deselectTag() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+DEL");
    currentSelectedTag = null;
  }

  /**
   * Get the current selected Tag or null if not selected
   * 
   * @return the current selected Tag or null
   */
  public String getSelectedTag() {
    return currentSelectedTag;
  }

  /**
   * Detect the type of tags that are in the rf field.
   * 
   * @return List with the detected tags
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public List<HfTag> detectTagTypes() throws CommConnectionException, RFIDReaderException {
    List<HfTag> tags = new ArrayList<>();
    String response = communicateSynchronized("AT+DTT");
    long timestamp = System.currentTimeMillis();
    for (String line : splitResponse(response.substring(6))) {
      // +DTT: E002223504422958,ISO15
      String data[] = splitLine(line);
      if (data.length > 0 && data[0].length() > 0 && data[0].charAt(0) == '<') {
        if (data[0].length() > 1 && data[0].charAt(1) == 'N') {
          // No tags found
          break;
        }
        // error?
        throw new RFIDReaderException(RFIDErrorCodes.NER, "Unexpected Reader response: {answer}");
      }
      if (data[1].contains("ISO15")) {
        tags.add(new ISO15Tag(data[0], timestamp, getAntennaPort()));
      } else {
        tags.add(new ISO14ATag(data[0], timestamp, getAntennaPort(), data[1]));
      }
    }
    return tags;
  }

  /**
   * Get the current inventory settings
   * 
   * @return the current inventory settings
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public NFCInventorySetting getInventorySettings() throws CommConnectionException, RFIDReaderException {
    if (null != currentInventorySettings) {
      return currentInventorySettings.clone();
    }
    String response = communicateSynchronized("AT+INVS?");
    String[] data = splitLine(response);
    // +INVS: 0,0,0
    try {
      currentInventorySettings = new NFCInventorySetting(data[0].equals("1"), data[1].equals("1"), data[2].equals("1"));
      return currentInventorySettings.clone();
    } catch (IndexOutOfBoundsException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, response);
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ISO15693 Commands
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Send an spezial ISO15693 read request with read-alike timing to a card.
   * 
   * @param request the request
   * @return the tag response
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String sendIso15ReadRequest(String request) throws CommConnectionException, RFIDReaderException {
    if (currentMode != NFCReaderMode.ISO15) {
      throw new RFIDReaderException(RFIDErrorCodes.WRM, "Only available in ISO15 mode!");
    }
    String response = communicateSynchronized("AT+RRQ", request);
    return response.isEmpty() ? response : response.substring(6);
  }

  /**
   * Send an spezial ISO15693 write request with write-alike timing to a card.
   * 
   * @param request the request
   * @return the tag response, empty if no response
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String sendIso15WriteRequest(String request) throws CommConnectionException, RFIDReaderException {
    if (currentMode != NFCReaderMode.ISO15) {
      throw new RFIDReaderException(RFIDErrorCodes.WRM, "Only available in ISO15 mode!");
    }
    String response = communicateSynchronized("AT+WRQ", request);
    return response.isEmpty() ? response : response.substring(6);
  }

  /**
   * This command set the "Application Family Identifier" for IOS15693 inventories. An AFI of 0 is treated as no AFI
   * set. If set to non-zero only transponders with the same AFI will respond in a inventory.
   * 
   * @param afi Application Family Identifier [0..255]
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void setAfi(int afi) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+AFI", String.format("%02X", afi));
  }

  /**
   * This command returns "Application Family Identifier" of the reader for IOS15693 inventories.
   * 
   * @return Application Family Identifier [0..255]
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public int getAfi() throws CommConnectionException, RFIDReaderException {
    try {
      return Integer.parseInt(communicateSynchronized("AT+AFI?").substring(6), 16);
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid AFI response format: " + e.getMessage());
    }
  }

  /**
   * Write the "Application Family Identifier" to an ISO15693 transponder.
   * 
   * @param afi Application Family Identifier [0..255]
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeTagAfi(int afi) throws CommConnectionException, RFIDReaderException {
    writeTagAfi(afi, false);
  }

  /**
   * Write the "Application Family Identifier" to an ISO15693 transponder.
   * 
   * @param afi Application Family Identifier [0..255]
   * @param withOptionFlag use the request option flag
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeTagAfi(int afi, boolean withOptionFlag) throws CommConnectionException, RFIDReaderException {
    if (currentMode != NFCReaderMode.ISO15) {
      throw new RFIDReaderException(RFIDErrorCodes.WRM, "Only available in ISO15 mode!");
    }
    communicateSynchronized("AT+WAFI", String.format("%02X", afi), withOptionFlag ? 1 : 0);
  }

  /**
   * Use to permanently lock the AFI of an ISO15693 transponder
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void lockTagAfi() throws CommConnectionException, RFIDReaderException {
    if (currentMode != NFCReaderMode.ISO15) {
      throw new RFIDReaderException(RFIDErrorCodes.WRM, "Only available in ISO15 mode!");
    }
    communicateSynchronized("AT+LAFI");
  }

  /**
   * Write the "Data Storage Format Identifier" to an ISO15693 transponder.
   * 
   * @param dsfid dsfid value
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeTagDsfid(int dsfid) throws CommConnectionException, RFIDReaderException {
    writeTagDsfid(dsfid, false);
  }

  /**
   * Write the "Data Storage Format Identifier" to an ISO15693 transponder.
   * 
   * @param dsfid dsfid value
   * @param withOptionFlag use the request option flag
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeTagDsfid(int dsfid, boolean withOptionFlag) throws CommConnectionException, RFIDReaderException {
    if (currentMode != NFCReaderMode.ISO15) {
      throw new RFIDReaderException(RFIDErrorCodes.WRM, "Only available in ISO15 mode!");
    }
    communicateSynchronized("AT+WDSFID", String.format("%02X", dsfid), withOptionFlag ? 1 : 0);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // ISO14A Commands
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Generic ISO14A Commands
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Send a raw ISO 14A request to a previously selected tag
   * 
   * @param request the request
   * @return the tag response
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String sendIso14Request(String request) throws CommConnectionException, RFIDReaderException {
    if (currentMode != NFCReaderMode.ISO14A) {
      throw new RFIDReaderException(RFIDErrorCodes.WRM, "Only available in ISO14A mode!");
    }
    if (null == currentSelectedTag) {
      throw new RFIDReaderException(RFIDErrorCodes.CNS, "No tag/card selected");
    }
    String response = communicateSynchronized("AT+REQ14", request);
    return response.isEmpty() ? response : response.substring(6);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Mifare Classic Commands
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Authenticate command for Mifare classic cards to access memory blocks. Prior to this command, the card has to be
   * selected.
   * 
   * @param block Block to authenticate
   * @param key Mifare Key to authenticate with (6 bytes as Hex)
   * @param keyType Type of key
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void authenticateMifareClassicBlock(int block, String key, KeyType keyType)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+AUT", block, key.toUpperCase(), keyType.name());
  }

  /**
   * Authenticate command for Mifare classic cards to access memory blocks. Use a stored Key Prior to this command, the
   * card has to be selected.
   * 
   * @param block Block to authenticate
   * @param storedKey Use the stored key instead of key and key type [0..16]
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void authenticateMifareClassicBlock(int block, int storedKey)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+AUTN", block, storedKey);
  }

  /**
   * Store an authenticate key in the reader.
   * 
   * @param keyStore the key store [0..16]
   * @param key Mifare Key to authenticate with (6 bytes as Hex)
   * @param keyType Type of key
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void storeMifareClassicKey(int keyStore, String key, KeyType keyType)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+SIK", keyStore, key.toUpperCase(), keyType.name());
  }

  /**
   * Get the access bits for a given Mifare Classic block. Prior to this command, the card has to be selected and the
   * block has to be authenticated.
   * 
   * @param block Block to read access bits for
   * @return access bits as string, like "001"
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String readMifareClassicAccessBits(int block) throws CommConnectionException, RFIDReaderException {
    return communicateSynchronized("AT+GAB", block).substring(6);
  }

  /**
   * Set the keys for a given block. Prior to this command, the card has to be selected and the block has to be
   * authenticated.
   * 
   * @param block Block to set keys and access bits for
   * @param keyA Mifare KeyA
   * @param keyB Mifare KeyB
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeMifareClassicAccessBits(int block, String keyA, String keyB)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+SKO", block, keyA.toUpperCase(), keyB.toUpperCase());
  }

  /**
   * Set the keys and optional also the access bits for a given block. Prior to this command, the card has to be
   * selected and the block has to be authenticated.
   * 
   * @param block Block to set keys and access bits for
   * @param keyA Mifare KeyA
   * @param keyB Mifare KeyB
   * @param accessBits The Mifare access bits for the block as string
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeMifareClassicAccessBits(int block, String keyA, String keyB, String accessBits)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+SKA", block, keyA.toUpperCase(), keyB.toUpperCase(), accessBits);
  }

  /**
   * Write/Create a mifare classic value block. Prior to this command, the card has to be selected and the block has to
   * be authenticated.
   * 
   * @param block block number
   * @param initialValue initial value
   * @param backupAddress address of the block used for backup
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeMifareClassicValueBlock(int block, int initialValue, int backupAddress)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+WVL", block, initialValue, backupAddress);
  }

  /**
   * Read a mifare classic value block. Prior to this command, the card has to be selected and the block has to be
   * authenticated.
   * 
   * @param block block number
   * @return the value
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public int readMifareClassicValue(int block) throws CommConnectionException, RFIDReaderException {
    // +RVL: 32,5
    try {
      return Integer.parseInt(splitLine(communicateSynchronized("AT+RVL", block).substring(6))[0]);
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid Mifare Classic value response format: " + e.getMessage());
    }
  }

  /**
   * Read a mifare classic value block. Prior to this command, the card has to be selected and the block has to be
   * authenticated.
   * 
   * @param block block number
   * @return the backup address
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public int readMifareClassicValueBackupAddress(int block) throws CommConnectionException, RFIDReaderException {
    // +RVL: 32,5
    try {
      return Integer.parseInt(splitLine(communicateSynchronized("AT+RVL", block).substring(6))[1]);
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid Mifare Classic backup address response format: " + e.getMessage());
    }
  }

  /**
   * Add a value of a Mifare Classic block. Prior to this command, the card has to be selected and the block has to be
   * authenticated.
   * 
   * @param block block number
   * @param value value to add, can be positive or negative
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void addMifareClassicValue(int block, int value) throws CommConnectionException, RFIDReaderException {
    if (value >= 0) {
      incrementMifareClassicValue(block, value);
    } else {
      decrementMifareClassicValue(block, Math.abs(value));
    }
  }

  /**
   * Increment the value of a Mifare Classic block. Prior to this command, the card has to be selected and the block has
   * to be authenticated.
   * 
   * @param block block number
   * @param value value to add
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void incrementMifareClassicValue(int block, int value) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+IVL", block, value);
  }

  /**
   * Decrement the value of a Mifare Classic block. Prior to this command, the card has to be selected and the block has
   * to be authenticated.
   * 
   * @param block block number
   * @param value value to subtract
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void decrementMifareClassicValue(int block, int value) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+DVL", block, value);
  }

  /**
   * Restore the value of a Mifare Classic block. This will load the current value from the block. With the transfer
   * method this value can be stored in a other block. Note that this operation only will have an effect after the
   * transfer command is executed. Prior to this command, the card has to be selected and the block has to be
   * authenticated.
   * 
   * @param block block number
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void restoreMifareClassicValue(int block) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+RSVL", block);
  }

  /**
   * Write all pending transactions to a mifare classic block. Prior to this command, the card has to be selected and
   * the block has to be authenticated.
   * 
   * @param block block number
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void transferMifareClassicValue(int block) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+TXF", block);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NTAG / Mifare Ultralight Commands
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Authenticate command for NTAG / Mifare Ultralight cards. After the authentication password protected pages can be
   * accessed.
   * 
   * @param password 8 sign password hex string
   * @return The password acknowledge - 4 sign password hex string
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public String authenticateNTag(String password) throws CommConnectionException, RFIDReaderException {
    // +NPAUTH: ABCD
    return communicateSynchronized("AT+NPAUTH", password).substring(9);
  }

  /**
   * Set the password and the password acknowledge for NTAG / Mifare Ultralight cards. Prior to this command, the card
   * has to be selected.
   * 
   * @param password 8 sign password hex string
   * @param passwordAcknowledge password acknowledge 4 sign password hex string
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeNTagAuthenticate(String password, String passwordAcknowledge)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NPWD", password, passwordAcknowledge);
  }

  /**
   * Configure the NTAG access.
   * 
   * @param config Access config
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeNTagAccessConfig(NTagAccessConfig config) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NACFG", config.getStartAddress(), config.isReadProtected() ? 1 : 0,
        config.getMaxAttempts());
  }

  /**
   * Read the NTAG access configuration. Prior to this command, the card has to be selected and authenticated.
   * 
   * @return The access configuration
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public NTagAccessConfig readNTagAccessConfig() throws CommConnectionException, RFIDReaderException {
    // +NACFG: 4,1,0
    String[] data = splitLine(communicateSynchronized("AT+NACFG?").substring(8));
    try {
      return new NTagAccessConfig(Integer.parseInt(data[0]), !"0".equals(data[1]), Integer.parseInt(data[2]));
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid NTAG access config response format: " + e.getMessage());
    }
  }

  /**
   * Configure the NTAG mirror configuration. Prior to this command, the card has to be selected and authenticated.
   * 
   * @param config The mirror configuration
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeNTagMirrorConfig(NTagMirrorConfig config) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NMCFG", config.getMode().name(), config.getPage(), config.getOffset());
  }

  /**
   * Read the NTAG mirror configuration. Prior to this command, the card has to be selected and authenticated.
   * 
   * @return The mirror configuration
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public NTagMirrorConfig readNTagMirrorConfig() throws CommConnectionException, RFIDReaderException {
    // +NMCFG: BOTH,4,0
    String[] data = splitLine(communicateSynchronized("AT+NMCFG?").substring(8));
    try {
      return new NTagMirrorConfig(MirrorMode.valueOf(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]));
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid NTAG mirror config response format: " + e.getMessage());
    }
  }

  /**
   * Configure the NTAG counter configuration. Prior to this command, the card has to be selected and authenticated.
   * 
   * @param config The counter configuration
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeNTagCounterConfig(NTagCounterConfig config) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NCCFG", config.isEnable() ? 1 : 0, config.isWithPasswortProtection() ? 1 : 0);
  }

  /**
   * Read the NTAG counter configuration. Prior to this command, the card has to be selected and authenticated.
   * 
   * @return The counter configuration
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public NTagCounterConfig readNTagCounterConfig() throws CommConnectionException, RFIDReaderException {
    // +NCCFG: 1,0
    String[] data = splitLine(communicateSynchronized("AT+NCCFG?").substring(8));
    return new NTagCounterConfig(data[0].equals("1"), data[1].equals("1"));
  }

  /**
   * Enable or Disable the NTAG strong modulation. Prior to this command, the card has to be selected and authenticated
   * 
   * @param enable true for enable the strong modulation
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void writeNTagStrongModulation(boolean enable) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NDCFG", enable ? 1 : 0);
  }

  /**
   * Read if the NTAG strong modulation is enabled. Prior to this command, the card has to be selected and authenticated
   * 
   * @return true if enabled
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public boolean readNTagStrongModulation() throws CommConnectionException, RFIDReaderException {
    return communicateSynchronized("AT+NDCFG?").substring(8).equals("1");
  }

  /**
   * Permanently lock the NTAG configuration. Prior to this command, the card has to be selected and authenticated.
   * 
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void lockNTagConfig() throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NCLK");
  }

  /**
   * Check if the NTAG configuration is locked. Prior to this command, the card has to be selected and authenticated.
   * 
   * @return True if the configuration is locked
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public boolean isNTagConfigLocked() throws CommConnectionException, RFIDReaderException {
    return communicateSynchronized("AT+NCLK?").substring(7).equals("1");
  }

  /**
   * Read the NTAG counter. Prior to this command, the card has to be selected and authenticated
   * 
   * @return The NTAG counter.
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public int readNTagCounter() throws CommConnectionException, RFIDReaderException {
    try {
      return Integer.parseInt(communicateSynchronized("AT+NCNT?").substring(7));
    } catch (NumberFormatException e) {
      throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid NTAG counter response format: " + e.getMessage());
    }
  }

  /**
   * Lock a NTAG page. The lock is irreversible. Prior to this command, the card has to be selected and authenticated
   * 
   * @param page the page number to lock
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void lockNTagPage(int page) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NLK", page);
  }

  /**
   * Set the NTAG block-lock-bits. The block-lock bits are used to lock the lock bits. Refer to the NTAG data sheet for
   * details. Prior to this command, the card has to be selected and authenticated.
   * 
   * @param page the page number to lock the lock bits for.
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   * @throws RFIDTransponderException if an transponder error occurs
   */
  public void lockNTagBlockLock(int page) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+NBLK", page);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Feedback
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Play a preconfigured sequence.
   * 
   * @param feedback 0 for the Startup jingle, 1 for the OK Feedback, 2 for the ERROR feedback
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void playFeedback(int feedback) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+FDB", feedback);
  }

  /**
   * This command is used to play a custom feedback sequence.
   * 
   * @param notes Encoded notes to be played. A note is always encoded by its name written as a capital letter and
   *        octave e.g. C4 or D5. Half-tone steps are encoded by adding a s or b to the note. For example Ds4 or Eb4.
   *        Note that Ds4 and Eb4 are basically the same note. A pause is denoted by a lowercase x.
   * @param repetitions Number of times the sequence should be repeated
   * @param stepLength Step length of a single step in the sequence
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void playNotes(String notes, int repetitions, int stepLength)
      throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+PLY", notes, repetitions, stepLength);
  }

  /**
   * Set the play frequency. The frequency is given in Hertz. To stop playback a frequency of 0 Hz should be issued.
   * 
   * @param frequency The frequency is given in Hertz.
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an reader error occurs
   */
  public void playAFrequency(int frequency) throws CommConnectionException, RFIDReaderException {
    communicateSynchronized("AT+FRQ", frequency);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Overridden methods
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  protected RFIDReaderException parseErrorResponse(String response) {
    switch (response) {
      // Tag Errors:
      case "No Tag selected": // NFC_CORE_ERROR_NOT_SELECTED:
      case "Wrong Tag type": // NFC_CORE_ERROR_TAG_TYPE:
      case "Unexpected Tag response": // NFC_CORE_ERROR_UNEXPECTED_RESPONSE:
      case "Block out of range": // NFC_CORE_ERROR_BLOCK_RANGE:
      case "Not authenticated": // NFC_CORE_ERROR_NOT_AUTHENTICATED:
      case "Access prohibited": // NFC_CORE_ERROR_ACCESS_PROHIBITED:
      case "Wrong block size": // NFC_CORE_ERROR_BLOCK_SIZE:
      case "Tag timeout": // NFC_CORE_ERROR_IO_TIMEOUT:
      case "Collision error": // NFC_CORE_ERROR_COLLISION:
      case "Overflow": // NFC_CORE_ERROR_OVERFLOW:
      case "Parity error": // NFC_CORE_ERROR_PARITY:
      case "Framing error": // NFC_CORE_ERROR_FRAMING:
      case "Protocol violation": // NFC_CORE_ERROR_PROTOCOL_VIOLATION:
      case "Authentication failure": // NFC_CORE_ERROR_AUTHENTICATION:
      case "Length error": // NFC_CORE_ERROR_LENGTH:
      case "Received NAK": // NFC_CORE_ERROR_NAK:
      case "NTAG invalid argument": // NFC_CORE_ERROR_NTAG_INVALID_ARG:
      case "NTAG parity/crc error": // NFC_CORE_ERROR_NTAG_PARITY:
      case "NTAG auth limit reached": // NFC_CORE_ERROR_NTAG_AUTH_LIMIT:
      case "NTAG EEPROM failure (maybe locked?)": // NFC_CORE_ERROR_NTAG_EEPROM:
      case "Mifare NAK 0": // NFC_CORE_ERROR_MIFARE_NAK0:
      case "Mifare NAK 1": // NFC_CORE_ERROR_MIFARE_NAK1:
      case "Mifare NAK 3": // NFC_CORE_ERROR_MIFARE_NAK3:
      case "Mifare NAK 4": // NFC_CORE_ERROR_MIFARE_NAK4:
      case "Mifare NAK 5": // NFC_CORE_ERROR_MIFARE_NAK5:
      case "Mifare NAK 6": // NFC_CORE_ERROR_MIFARE_NAK6:
      case "Mifare NAK 7": // NFC_CORE_ERROR_MIFARE_NAK7:
      case "Mifare NAK 8": // NFC_CORE_ERROR_MIFARE_NAK8:
      case "Mifare NAK 9": // NFC_CORE_ERROR_MIFARE_NAK9:
      case "ISO15 custom command error": // NFC_CORE_ERROR_ISO15_CUSTOM_CMD_ERR:
      case "ISO15 command not supported": // NFC_CORE_ERROR_ISO15_CMD_NOT_SUPPORTED:
      case "ISO15 command not recognized": // NFC_CORE_ERROR_ISO15_CMD_NOT_RECOGNIZED:
      case "ISO15 option not supported": // NFC_CORE_ERROR_ISO15_OPT_NOT_SUPPORTED:
      case "ISO15 no information": // NFC_CORE_ERROR_ISO15_NO_INFO:
      case "ISO15 block not available": // NFC_CORE_ERROR_ISO15_BLOCK_NOT_AVAIL:
      case "ISO15 block locked": // NFC_CORE_ERROR_ISO15_BLOCK_LOCKED:
      case "ISO15 content change failure": // NFC_CORE_ERROR_ISO15_CONTENT_CHANGE_FAIL:
      case "ISO15 block programming failure": // NFC_CORE_ERROR_ISO15_BLOCK_PROGRAMMING_FAIL:
      case "ISO15 block protected": // NFC_CORE_ERROR_ISO15_BLOCK_PROTECTED:
      case "ISO15 cryptographic error": // NFC_CORE_ERROR_ISO15_CRYPTO:
        return new RFIDTransponderException(RFIDErrorCodes.TEC, response);
      // Reader Errors:
      // case "No such protocol": // NFC_CORE_ERROR_NO_SUCH_PROTO:
      // case "No frontend selected": // NFC_CORE_ERROR_NO_FRONTEND:
      // case "Failed to initialize frontend": // NFC_CORE_ERROR_FRONTEND_INIT:
      // case "Wrong operation mode": // NFC_CORE_ERROR_OP_MODE:
      // case "Invalid parameter": // NFC_CORE_ERROR_INVALID_PARAM:
      // case "Command failed": // NFC_CORE_ERROR_COMMAND_FAILED:
      // case "IO error": // NFC_CORE_ERROR_IO:
      // case "Timeout": // NFC_CORE_ERROR_TIMEOUT:
      // case "Temperature error": // NFC_CORE_ERROR_TEMPERATURE:
      // case "Resource error": // NFC_CORE_ERROR_RESOURCE:
      // case "RF error": // NFC_CORE_ERROR_RF:
      // case "Noise error": // NFC_CORE_ERROR_NOISE:
      // case "Aborted": // NFC_CORE_ERROR_ABORTED:
      // case "Authentication delay": // NFC_CORE_ERROR_AUTH_DELAY:
      // case "Unsupported parameter": // NFC_CORE_ERROR_UNSUPPORTED_PARAM:
      // case "Unsupported command": // NFC_CORE_ERROR_UNSUPPORTED_CMD:
      // case "Wrong use condition": // NFC_CORE_ERROR_USE_CONDITION:
      // case "Key error": // NFC_CORE_ERROR_KEY:
      // case "No key at given index": // NFC_CORE_ERROR_KEYSTORE_NO_KEY:
      // case "Could not save key": // NFC_CORE_ERROR_KEYSTORE_SAVE_ERROR:
      // case "Feedback out of range": // NFC_CORE_ERROR_FEEDBACK_OUT_OF_RANGE:
      // case "Invalid feedback string": // NFC_CORE_ERROR_FEEDBACK_PARSING_ERROR:
      // case "Feedback already running": // NFC_CORE_ERROR_FEEDBACK_ALREADY_RUNNING:
      // case "Unknown Error": // NFC_CORE_ERROR_UNKNOWN:
      default:
        return new RFIDTransponderException(RFIDErrorCodes.NER, response);
    }
  }

  @Override
  public void startInventoryReport(long tagLostTime) throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "Inventory report not supported");
  }

  @Override
  public List<HfTag> stopInventoryReport() throws CommConnectionException, RFIDReaderException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "Inventory report not supported");
  }

  @Override
  protected List<HfTag> parseInventoryReport(String[] answers, int prefix_length) throws RFIDReaderException {
    // TODO Not currently supported by the reader
    throw new UnsupportedOperationException("Unimplemented method 'parseInventoryReport'");
  }

  @Override
  protected List<HfTag> parseInventory(String[] answers, int prefix_length, boolean throwError)
      throws RFIDReaderException {
    // +INV: E0040150954F0983,ISO15,01<CR>
    // +INV: 801E837A2ABC04,ISO14A,00,4400<CR><LF>
    // +INV: <NO TAGS FOUND><CR><LF>
    long timestamp = System.currentTimeMillis();
    List<HfTag> tags = new ArrayList<>();
    String error = null;
    Integer antenna = getCurrentAntennaPort();
    for (String tagInfo : answers) {
      if (tagInfo.length() == 0 || tagInfo.charAt(0) != '+') {
        continue;
      }
      String[] split = splitLine(tagInfo.substring(prefix_length));
      if (split[0].length() > 0 && split[0].charAt(0) == '<') {
        // message
        switch (split[0].charAt(1)) {
          case 'R': // Round finished
            if (split.length > 1) {
              // additional antenna port info
              if (split[1].length() < 6) {
                throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid antenna format in response: " + split[1]);
              }
              try {
                antenna = Integer.parseInt(split[1].substring(5, split[1].length() - 1));
                for (HfTag tag : tags) {
                  tag.setAntenna(antenna);
                }
              } catch (NumberFormatException e) {
                throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid antenna value in response: " + split[1] + " - " + e.getMessage());
              }
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
        HfTag tag;
        if (currentInventorySettings.isWithTagDetails()) {
          switch (currentMode) {
            case ISO15:
              try {
                tag = new ISO15Tag(split[0], timestamp, antenna, Integer.parseInt(split[1], 16));
              } catch (NumberFormatException e) {
                throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid ISO15 tag data format: " + e.getMessage());
              }
              break;
            case ISO14A:
              try {
                tag = new ISO14ATag(split[0], timestamp, antenna, Integer.parseInt(split[1], 16),
                    Integer.parseInt(split[2], 16));
              } catch (NumberFormatException e) {
                throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid ISO14A tag data format: " + e.getMessage());
              }
              break;
            case AUTO:
              try {
                if (split[1].equals(NFCReaderMode.ISO15.name())) {
                  tag = new ISO15Tag(split[0], timestamp, antenna, Integer.parseInt(split[2], 16));
                } else {
                  tag = new ISO14ATag(split[0], timestamp, antenna, Integer.parseInt(split[2], 16),
                      Integer.parseInt(split[3], 16));
                }
              } catch (NumberFormatException e) {
                throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid AUTO mode tag data format: " + e.getMessage());
              }
              break;
            default:
              tag = new HfTag(split[0], timestamp, antenna);
              break;
          }
        } else {
          switch (currentMode) {
            case ISO15:
              tag = new ISO15Tag(split[0], timestamp, antenna);
              break;
            case ISO14A:
              tag = new ISO14ATag(split[0], timestamp, antenna);
              break;
            case AUTO:
              try {
                if (split[1].equals(NFCReaderMode.ISO15.name())) {
                  tag = new ISO15Tag(split[0], timestamp, antenna, Integer.parseInt(split[2], 16));
                } else {
                  tag = new ISO14ATag(split[0], timestamp, antenna, Integer.parseInt(split[2], 16),
                      Integer.parseInt(split[3], 16));
                }
              } catch (NumberFormatException e) {
                throw new RFIDReaderException(RFIDErrorCodes.NER, "Invalid AUTO mode tag data format in non-detailed mode: " + e.getMessage());
              }
              break;
            default:
              tag = new HfTag(split[0], timestamp, antenna);
              break;
          }
        }
        tags.add(tag);
      } catch (Exception e) {
        if (null == currentInventorySettings) {
          // not initialised - ignore
          return new ArrayList<>();
        }
        getLogger().warn("Inventory warning {}", tagInfo, getLogger().isDebugEnabled() ? e : null);
      }
    }
    if (null != error) {
      throw new RFIDReaderException(RFIDErrorCodes.ARH, String.format("%s %s", error, null != antenna ? antenna : ""));
    }
    return tags;
  }

  @Override
  protected String prepareDevice() throws CommConnectionException, RFIDReaderException {
    String message = super.prepareDevice();
    stopInventory();
    NFCInventorySetting setting = getInventorySettings();
    if(!setting.isWithTagDetails()) {
      setting.setWithTagDetails(true);
      setInventorySettings(setting);
    }
    getReaderMode();
    return message;
  }

}
