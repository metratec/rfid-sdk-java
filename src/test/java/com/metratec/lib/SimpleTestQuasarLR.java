/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib;

import java.util.Date;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.inventory.SimpleInventory;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.rfidreader.iso.ISOReader.MODE;
import com.metratec.lib.rfidreader.iso.ISOReader.SRI;
import com.metratec.lib.rfidreader.iso.QuasarLR;
import com.metratec.lib.tag.HfTag;

/**
 * @author mn
 *
 */
public class SimpleTestQuasarLR implements RfidReaderEventListener {

  /**
   * @param args program argument - not used
   */
  public static void main(String[] args) {
    SimpleTestQuasarLR test = new SimpleTestQuasarLR();
    try {
      test.firstTest("192.168.2.47");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @throws CommConnectionException if an error occurs
   * 
   */
  private void firstTest(String ipAddress) throws Exception {
    QuasarLR reader = new QuasarLR("QuasarLR", ipAddress, 10001, MODE.ISO15693,
        SRI.SingleSubcarrier_100percentASK);
    reader.setReaderEventListener(this);
    SimpleInventory inventory = new SimpleInventory(); // simple inventory for check the changes
                                                       // between two scans
    try {
      reader.start();
      // wait until the event RfidEventListener#connectionState(RfidReaderConnectionState event) is
      // called or wait until the reader.isConnected() mehtod return true
      while (!reader.isConnected()) {
        Thread.sleep(10);
      }
      System.in.read();
      reader.reset();
      while (true) {
        if (reader.isConnected()) {
          // get the current tags trom reader
          List<HfTag> tags = reader.getInventory();
          // update the inventory
          inventory.updateInventory(tags);
          // check if new tags found
          List<String> addedTags = inventory.getAddedTags();
          if (!addedTags.isEmpty()) {
            System.out.println(new Date() + " Tags added: " + addedTags);
          }
          // check if tags removed
          List<String> removedTags = inventory.getRemovedTags();
          if (!removedTags.isEmpty()) {
            System.out.println(new Date() + " Tags removed: " + removedTags);
          }

          if (!addedTags.isEmpty() || !removedTags.isEmpty()) {
            System.out.println(new Date() + " current Inventory: " + inventory.getInventory());
          }

        } else {
          // reader is not connected, the reader will reconnect automatically...so wait for it
          Thread.sleep(100);
        }
      }
    } finally {
      if (null != reader) {
        reader.stop();
      }
    }
  }

  @Override
  public void inputChange(RfidReaderInputChange event) {
    // current QuasarLR firmware don't support the input events
  }

  @Override
  public void connectionState(RfidReaderConnectionState event) {
    System.out.println("Reader state: " + event.getIdentifier() + " " + event.isConnected() + " "
        + event.getMessage());
  }

}
