/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib;

import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.rfidreader.mf.DeskID_MF;
import com.metratec.lib.tag.MfTag;

/**
 * @author mn
 *
 */
public class SimpleTestDeskIdMf implements RfidReaderEventListener {
  private DeskID_MF reader;

  /**
   * @param args program argument - not used
   */
  public static void main(String[] args) {
    SimpleTestDeskIdMf test = new SimpleTestDeskIdMf();
    try {
      test.firstTest();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @throws CommConnectionException if an error occurs
   * 
   */
  private void firstTest() throws Exception {
    reader = new DeskID_MF("DeskID Iso", "/dev/ttyUSB0", 115200, 8, 1, 0, 0);
    reader.setReaderEventListener(this);
    try {
      reader.startAndWait(5000);
      List<MfTag> inv;
      while ((inv = reader.getInventory()).isEmpty()) {
        System.out.println("Wait for Tag");
        Thread.sleep(1000);
      }
      System.out.println("Tags found: " + inv);
    } finally {
      if (null != reader) {
        reader.stop();
      }
    }
  }

  @Override
  public void inputChange(RfidReaderInputChange event) {
    System.out.println(
        "Input changed: " + event.getIdentifier() + " " + event.getPin() + " " + event.getState());

  }

  @Override
  public void connectionState(RfidReaderConnectionState event) {
    System.out.println("Reader state: " + event.getIdentifier() + " " + event.isConnected() + " "
        + event.getMessage());
  }

}
