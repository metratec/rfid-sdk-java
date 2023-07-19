package com.metratec.lib.rfidreader.event;


public class RfidTagFound<T> extends RfidTagEvent<T> {

  private static final long serialVersionUID = 5630195390982611902L;

  public RfidTagFound(String identifier, T tag, Long timestamp) {
    super(identifier, tag, timestamp);
  }

}
