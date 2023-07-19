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
import com.metratec.lib.rfidreader.iso.DeskID_ISO;
import com.metratec.lib.rfidreader.iso.ISOReader.MODE;
import com.metratec.lib.rfidreader.iso.ISOReader.SRI;
import com.metratec.lib.tag.HfTag;

/**
 * @author mn
 *
 */
public class SimpleTestDeskIdIso implements RfidReaderEventListener {
  private DeskID_ISO reader;
  private boolean isStarted = false;

  /**
   * @param args program argument - not used
   */
  public static void main(String[] args) {
    SimpleTestDeskIdIso test = new SimpleTestDeskIdIso();
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
    reader = new DeskID_ISO("DeskID Iso", "/dev/ttyUSB0", 115200, 8, 1, 0, 0, MODE.ISO15693,
        SRI.SingleSubcarrier_100percentASK);
    reader.setReaderEventListener(this);
    try {
      // reader.connect(5000);
      reader.start();
      while (!isStarted) {
        Thread.sleep(10);
      }
      reader.setPower(200);
      List<HfTag> inv;
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
    isStarted = event.isConnected();
  }

}
