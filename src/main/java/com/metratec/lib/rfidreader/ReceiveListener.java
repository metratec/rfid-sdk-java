package com.metratec.lib.rfidreader;

/**
 * Interface for listening to data received from RFID readers.
 * Implementations of this interface can be registered with readers
 * to receive notifications when data is received from the reader.
 */
public interface ReceiveListener {
  /**
   * called if data received
   * @param data the received data
   */
  void dataReceived(String data);
}
