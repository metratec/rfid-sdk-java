package com.metratec.lib.rfidreader.nfc;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.Rs232Connection;
import com.metratec.lib.connection.UsbConnection;
import com.metratec.lib.rfidreader.RFIDErrorCodes;
import com.metratec.lib.rfidreader.RFIDReaderException;

/**
 * With the DeskID NFC Metratec presents its first multi-protocol RFID reader/writer device. The slim housing looks good on every desktop and
can read and write any 13.56 MHz RFID transponder. This includes ISO15693 tags (NFC Type 5) and all ISO14443-based transponders including
all products from the NXP Mifare® series. This includes not only Mifare Classic and Ultralight® but also NTAG transponders as well as the
very secure Mifare DESFire® tags.
 */
public class DeskID_NFC extends NFCReaderAT{
  /**
   * Minimal reader revison
   */
  protected final static String MIN_READER_REVISION = "DeskID_NFC       0204";

  /**
   * Creates a new PulsarLR class for communicate with the specified metraTec usb PulsarLR
   * 
   * @param identifier reader identifier
   * @param usbDeviceSerialNumber serial number of the usb reader
   */
  public DeskID_NFC(String identifier, String usbDeviceSerialNumber) {
    super(identifier, new UsbConnection(usbDeviceSerialNumber, 115200));
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
  public DeskID_NFC(String identifier, String portName, int baudrate, int dataBit, int stopBit, int parity,
      int flowControl) {
    super(identifier, new Rs232Connection(portName, baudrate, dataBit, stopBit, parity, flowControl));
  }

  @Override
  public int getAntennaPort() throws CommConnectionException, RFIDReaderException {
    return 1;
  }

  @Override
  public boolean getInput(int pin) throws RFIDReaderException, CommConnectionException {
    throw new RFIDReaderException(RFIDErrorCodes.NOS, "Input not supported for DeskID Readers");
  }

  @Override
  public void reset() throws RFIDReaderException, CommConnectionException {
    super.reset(5000);
  }

}
