package com.metratec.lib.rfidreader;

/** */
public interface ReceiveListener {
  /**
   * called if data received
   * @param data the received data
   */
  void dataReceived(String data);
}
