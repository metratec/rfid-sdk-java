/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib;

import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.rfidreader.uhf.DeskID_UHF;
import com.metratec.lib.rfidreader.uhf.UHFReader.MEMBANK;
import com.metratec.lib.rfidreader.uhf.UHFReader.READER_MODE;
import com.metratec.lib.tag.UhfTag;

/**
 * @author mn
 *
 */
public class SimpleTestDeskIdUhf implements RfidReaderEventListener {
  private DeskID_UHF reader;

  /**
   * @param args program argument - not used
   */
  public static void main(String[] args) {
    SimpleTestDeskIdUhf test = new SimpleTestDeskIdUhf();
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
    reader = new DeskID_UHF("DeskID Iso", "/dev/ttyUSB1", 115200, 8, 1, 0, 0, READER_MODE.ETS);
    reader.setReaderEventListener(this);
    try {
      // use this if you will wait until the reader is connected
      reader.startAndWait(5000);

      // if you want events, use this and wait for the connection state event or
      // until the reader is connected
      // reader.connect();
      // while (!reader.isConnected()) {
      // Thread.sleep(100);
      // }


      List<UhfTag> inv;
      while ((inv = reader.getInventory()).isEmpty()) {
        System.out.println("Wait for Tag");
        Thread.sleep(1000);
      }
      System.out.println("Tags found: " + inv);
      System.out.println(reader.getTagData(MEMBANK.USR, 0, 0));


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
