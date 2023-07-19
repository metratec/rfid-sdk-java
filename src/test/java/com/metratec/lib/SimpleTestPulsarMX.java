/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib;

import java.io.IOException;
import java.util.List;
import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.rfidreader.event.RfidReaderConnectionState;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.event.RfidReaderInputChange;
import com.metratec.lib.rfidreader.uhf.PulsarMX;
import com.metratec.lib.rfidreader.uhf.UHFReader;
import com.metratec.lib.tag.UhfTag;

/**
 * @author mn
 *
 */
public class SimpleTestPulsarMX implements RfidReaderEventListener {
  private PulsarMX reader;

  public SimpleTestPulsarMX() {
    reader = new PulsarMX("P204", "192.168.2.221", 10001, UHFReader.READER_MODE.ETS);
    reader.setReaderEventListener(new RfidReaderEventListener() {

      @Override
      public void inputChange(RfidReaderInputChange event) {
        System.out.println("Input changed: " + event.getIdentifier() + " " + event.getPin() + " "
            + event.getState());

      }

      @Override
      public void connectionState(RfidReaderConnectionState event) {
        System.out.println("Reader state: " + event.getIdentifier() + " " + event.isConnected()
            + " " + event.getMessage());
        synchronized (reader) {
          System.out.println("Notify All " + this.toString());
          reader.notifyAll();
        }
      }
    });

  }

  /**
   * @param args program argument - not used
   */
  public static void main(String[] args) {
    final SimpleTestPulsarMX test = new SimpleTestPulsarMX();
    try {
      test.firstTest();
      test.firstTest();
    } catch (Exception e) {
      e.printStackTrace();
    }

    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          test.firstTest();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }).start();


  }

  /**
   * @throws CommConnectionException if an error occurs
   * 
   */
  private void firstTest() throws Exception {

    try {
      synchronized (reader) {
        System.err.println("start " + this.toString());
        reader.start();
        while (!reader.isConnected()) {
          reader.wait();
        }
        System.out.println("Reader started");
      }
      List<UhfTag> inv;


      reader.standby();
      waitForUserEnterKey();
      reader.wakeUp();
      waitForUserEnterKey();

      while ((inv = reader.getInventory()).isEmpty()) {
        System.out.println("Wait for Tag");
        Thread.sleep(1000);
      }
      System.out.println("Tags found: " + inv);
      System.out.println("Power: " + reader.getPower());
    } finally {
      if (null != reader) {
        reader.stop();
      }
    }
  }

  /**
   * 
   */
  private void waitForUserEnterKey() {
    try {
      while ('\n' != System.in.read()) {

      }
    } catch (IOException e) {
      e.printStackTrace();
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
    reader.notifyAll();
  }

}
