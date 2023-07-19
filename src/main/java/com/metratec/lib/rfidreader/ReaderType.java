/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.rfidreader;

/**
 * Enum with available Reader Types
 * 
 * @author man
 *
 */
public enum ReaderType {
  /**
   * <b>DeskID ISO</b><br>
   * HF Reader with USB interface for use on the desktop or other office environment. Reads
   * transponder according to ISO 15693 Protocol as well as custom commands (e.g. KSW VarioSense).
   */
  DESKID_ISO("DESKID_ISO"),
  /**
   * <b>QuasarMX Industrial HF Reader/Writer</b><br>
   * An industrial short range RFID reader for 13.56 MHz with PLC compatible inputs/outputs and a
   * fast read/write rate
   */
  QUASAR_MX("QUASAR_MX"),
  /**
   * <b>QuasarMX Industrial HF Reader/Writer</b><br>
   * An industrial short range RFID reader for 13.56 MHz with PLC compatible inputs/outputs and a
   * fast read/write rate
   */
  QuasarLR("QuasarLR"),
  /**
   * <b>Dwarf15 HF SMD Module</b><br>
   * One of the smallest HF RFID modules in the world for direct SMD soldering
   */
  DWARF15("DWARF15"),
  /**
   * <b>QR15 Plug-In Module</b><br>
   * Compact HF Plug-In Modul with integrated antenna and a read range of up to 70 mm
   */
  QR15("QR15"),
  /**
   * <b>DeskID UHF</b><br>
   * RFID UHF Reader with USB interface for use on a desktop or other office environment. Reads tags
   * according to the EPC Class 1 Gen 2 Standard at 868 MHz (ETSI, EU).
   */
  DESKID_UHF("DESKID_UHF"),
  /**
   * <b>PulsarMX UHF Mid Range Reader</b><br>
   * The PulsarMX UHF Mid Range Rader is a compact and cost-effective UHF RFID Reader for a medium
   * read range of up to 5 m
   */
  PULSAR_MX("PULSAR_MX"),
  /**
   * <b>PulsarLR UHF Long Range Reader</b><br>
   * . The PulsarLR UHF Long Range Reader is a compact and cost-effective UHF RFID Reader.
   */
  PULSAR_LR("PULSARLR"),
  /**
   * <b>DwarfG2 UHF SMD Module</b><br>
   * A compact but powerful short-range UFH module for direct SMD soldering and external antenna
   * connector
   */
  DWARFG2("DWARFG2"),
  /**
   * <b>DwarfG2 Mini UHF SMD Module</b><br>
   * A compact but powerful short-range UFH module for direct SMD soldering and external antenna
   * connector
   */
  DWARFG2_MINI("DWARFG2_MINI"),
  /**
   * <b>DeskID Mifare</b><br>
   * Mifare Reader with USB interface for use on the desktop or other office environment. Reads
   * transponders according to the Mifare Standard (Mifare Classic, Mifare Ultralight, Mifare Plus)
   */
  DESKID_MIFARE("DESKID_MIFARE"),
  /**
   * <b>QuasarMF Mifare Reader</b><br>
   * The only RFID Mifare Reader in the world with Ethernet connection and optically isolated
   * Inputs/Outputs to control a multiplexer or other industrial equipment
   */
  QUASAR_MIFARE("QUASAR_MIFARE"),
  /**
   * <b>Dwarf14 HF SMD Module</b><br>
   * One of the smallest Mifare RFID modules in the world for direct SMD soldering
   */
  DWARF14("DWARF_14"),
  /**
   * <b>QR14 Plug-In Module</b><br>
   * Compact Mifare Plug-In Module with integrated Antenna and a read range of up to 50 mm
   */
  QR14("QR14"),
  /**
   * the reader type is unknown
   */
  UNKNOWN("unknown");

  private int firmwareRevision;
  private String firmwareName;

  ReaderType(String firmwareName) {
    this.firmwareName = firmwareName;
  }

  /**
   * @return the firmwareName
   */
  public String getFirmwareName() {
    return firmwareName;
  }

  /**
   * @return the firmware revision
   */
  public int getFirmwareRevision() {
    return firmwareRevision;
  }

  /**
   * Set the firmware revision
   * 
   * @param firmwareRevision firmware revision
   */
  public void setFirmwareRevision(int firmwareRevision) {
    this.firmwareRevision = firmwareRevision;
  }
}
