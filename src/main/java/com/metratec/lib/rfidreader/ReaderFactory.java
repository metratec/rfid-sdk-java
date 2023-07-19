/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH
 * All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

import java.util.ArrayList;
import java.util.List;

import com.metratec.lib.connection.CommConnectionException;
import com.metratec.lib.connection.ICommConnection;
import com.metratec.lib.rfidreader.event.RfidReaderEventListener;
import com.metratec.lib.rfidreader.iso.DeskID_ISO;
import com.metratec.lib.rfidreader.iso.Dwarf15;
import com.metratec.lib.rfidreader.iso.QR15;
import com.metratec.lib.rfidreader.iso.QuasarLR;
import com.metratec.lib.rfidreader.iso.QuasarMX;
import com.metratec.lib.rfidreader.mf.DeskID_MF;
import com.metratec.lib.rfidreader.mf.Dwarf14;
import com.metratec.lib.rfidreader.mf.QR14;
import com.metratec.lib.rfidreader.uhf.DeskID_UHF;
import com.metratec.lib.rfidreader.uhf.DwarfG2;
import com.metratec.lib.rfidreader.uhf.DwarfG2Mini;
import com.metratec.lib.rfidreader.uhf.PulsarLR;
import com.metratec.lib.rfidreader.uhf.PulsarMX;
import com.metratec.lib.tag.RfidTag;

/**
 * @author mn
 *
 */
public class ReaderFactory {
  /**
   * @param identifier reader identifier
   * @param connection {@link ICommConnection}
   * @return the connected reader
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an rfid reader exception occurs
   */
  public static MetratecReader<?> connectReader(String identifier, ICommConnection connection)
      throws CommConnectionException, RFIDReaderException {
    return connectReader(identifier, connection, null);
  }

  /**
   * @param identifier reader identifier
   * @param connection {@link ICommConnection}
   * @param listener {@link RfidReaderEventListener}
   * @return the connected reader
   * @throws CommConnectionException if an communication error occurs
   * @throws RFIDReaderException if an rfid reader exception occurs
   */
  @SuppressWarnings("PMD.EmptyCatchBlock")
  public static MetratecReader<?> connectReader(String identifier, ICommConnection connection,
      RfidReaderEventListener listener) throws CommConnectionException, RFIDReaderException {
    MetratecReader<?> reader = new MetratecReaderGen1<RfidTag>(identifier, connection) {
      @Override
      public List<RfidTag> getInventory()
          throws RFIDReaderException, CommConnectionException {
        return new ArrayList<>();
      }

      @Override
      protected void handleInventory(String inventory) {

      }

      @Override
      public void scanInventory() throws CommConnectionException, RFIDReaderException {

      }

      @Override
      public void setAntennaPort(int port) throws CommConnectionException, RFIDReaderException {

      }

      @Override
      public void setPower(int power) throws CommConnectionException, RFIDReaderException {
        
      }

      @Override
      public void setMultiplexAntennas(int numberOfAntennas) {
        
      }
    };
    try {
      reader.startAndWait();
    } catch (CommConnectionException e) {
      reader.stop();
      throw e;
    }
    ReaderType type;
    try {
      type = reader.getReaderType();
    } catch (RFIDReaderException e) {
      throw e;
    } finally {
      try {
        reader.stop();
      } catch (CommConnectionException e) {
        // ignore
      }
    }
    switch (type) {
      case DESKID_ISO:
        reader = new DeskID_ISO(identifier, connection);
        break;
      case DESKID_MIFARE:
        reader = new DeskID_MF(identifier, connection);
        break;
      case DESKID_UHF:
        reader = new DeskID_UHF(identifier, connection);
        break;
      case DWARF14:
        reader = new Dwarf14(identifier, connection);
        break;
      case DWARF15:
        reader = new Dwarf15(identifier, connection);
        break;
      case DWARFG2:
        reader = new DwarfG2(identifier, connection);
        break;
      case DWARFG2_MINI:
        reader = new DwarfG2Mini(identifier, connection);
        break;
      case PULSAR_LR:
        reader = new PulsarLR(identifier, connection);
        break;
      case PULSAR_MX:
        reader = new PulsarMX(identifier, connection);
        break;
      case QR14:
        reader = new QR14(identifier, connection);
        break;
      case QR15:
        reader = new QR15(identifier, connection);
        break;
      case QUASAR_MIFARE:
        reader = new QuasarMX(identifier, connection);
        break;
      case QUASAR_MX:
        reader = new QuasarMX(identifier, connection);
        break;
      case QuasarLR:
        reader = new QuasarLR(identifier, connection);
        break;
      case UNKNOWN:
        throw new RFIDReaderException("Unknow rfid reader");
    }
    if (null != listener) {
      reader.setReaderEventListener(listener);
    }
    try {
      reader.startAndWait();
    } catch (CommConnectionException | RFIDReaderException e) {
      try {
        reader.stop();
      } catch (CommConnectionException e1) {
      }
      throw e;
    }
    return reader;
  }


}
