/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib;

import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.RFIDReaderException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.rfidreader.iso.ISOReader.MODE;
import com.metratec.lib.rfidreader.iso.ISOReader.SRI;
import com.metratec.lib.rfidreader.iso.QuasarMX;
import com.metratec.lib.tag.HfTag;

/**
 * @author mn
 *
 */
public class SimpleTestQuasarMX implements RfidReaderEventListener {
  private QuasarMX reader;
  private boolean isStarted = false;

  /**
   * @param args program argument - not used
   */
  public static void main(String[] args) {
    SimpleTestQuasarMX test = new SimpleTestQuasarMX();
    try {
      // test.firstTest();
      test.writeData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void writeData() throws InterruptedException, CommConnectionException, RFIDReaderException {
    reader = new QuasarMX("QuasarMx", "192.168.2.205", 10001, MODE.ISO15693, SRI.SingleSubcarrier_100percentASK);
    reader.setReaderEventListener(this);
    try {
      // reader.connect(5000);
      reader.start();
      while (!isStarted) {
        Thread.sleep(10);
      }
      // reader.setPower(200);
      List<HfTag> inv;
      while ((inv = reader.getInventory()).isEmpty()) {
        System.out.println("Wait for Tag");
        Thread.sleep(1000);
      }
      String tagId = inv.get(0).getId();
      System.out.println("Tag found: " + tagId);

      String data = getTestData01();
      int block = 0;
      boolean notFinish = true;
      int blockSizeHex = 16;
      while (notFinish) {
        String tmpData;
        if(data.length() >= block * blockSizeHex + blockSizeHex){
          tmpData = data.substring(0 + block * blockSizeHex, block * blockSizeHex + blockSizeHex);
        } else {
          tmpData = data.substring(0 + block * blockSizeHex);
          while(tmpData.length() < blockSizeHex){
            tmpData += "00";
          }
          notFinish = false;
        }
        for (int i = 0; i < 2; i++) {
          try {
            System.out.println(String.format("Write Block %02d: %s", block, tmpData));
            reader.setTagData(block, tmpData, tagId);
            block++;
            break;
          } catch (RFIDReaderException e) {
            System.out.println(String.format("Error write block %d: %s", block, e.toString()));
            if(i==2){
              notFinish = false;
            }
          }
        }
      }



    } finally {
      if (null != reader) {
        reader.stop();
      }
    }

  }

  /**
   * @return test tag data
   */
  private String getTestData01() {
    // @formatter:off
    return "001E0001000000000000000000001388" + "000B000141344C394132313034303235" + "30304153303353313531383035303031"
        + "00000000000000010001000000000000" + "0000044C150505083827000200020000" + "0000000000000F000000000000000001"
        + "00010000000000000000012C15050411" + "06230002000200000000000000000000" + "00000000000000010001000000000000"
        + "00000190150413140300020000000000" + "00000000000000000000000000000003" + "0001000000000000000001F415050413"
        + "14030002000200000000000000000000" + "00000000000000010001000000000000" + "00000258150504131403000200020000"
        + "00000000000000000000000000000001" + "0001000000000000000002BC15050415" + "30430002000200000000032000000000"
        + "00000000000000010001000000000000" + "00000320150505090727000200000000" + "00000000000000000000000000000002"
        + "00010000000000000000038415050509" + "07270002000200000000000000000000" + "00000000000000010001000000000000"
        + "0000044C150505083827000200020000" + "0000000000000F000000";
    // @formatter:on
  }

  /**
   * @throws CommConnectionException if an error occurs
   * 
   */
  private void firstTest() throws Exception {
    reader =
        new QuasarMX("QuasarMx", "/dev/ttyUSB0", 115200, 8, 1, 0, 0, MODE.ISO15693, SRI.SingleSubcarrier_100percentASK);
    reader.setReaderEventListener(this);
    try {
      // reader.connect(5000);
      reader.start();
      while (!isStarted) {
        Thread.sleep(10);
      }
      // reader.setPower(200);
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
    System.out.println("Input changed: " + event.getIdentifier() + " " + event.getPin() + " " + event.getState());

  }

  @Override
  public void connectionState(RfidReaderConnectionState event) {
    System.out.println("Reader state: " + event.getIdentifier() + " " + event.isConnected() + " " + event.getMessage());
    isStarted = event.isConnected();
  }

}
